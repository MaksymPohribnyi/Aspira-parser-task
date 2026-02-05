package com.pohribnyi.parser;

import com.pohribnyi.config.ParserConfig;
import com.pohribnyi.http.LeonApiClient;
import com.pohribnyi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class LeonPrematchParser {

    private static final Logger log = LoggerFactory.getLogger(LeonPrematchParser.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'");

    private final LeonApiClient apiClient;
    private final ExecutorService executorService;

    /*
     WITHOUT INVERSION OF CONTROL OVERHEAD
    */
    public LeonPrematchParser() {
        this.apiClient = new LeonApiClient();
        this.executorService = Executors.newFixedThreadPool(ParserConfig.MAX_PARALLEL_REQUESTS);
    }

    public void parse() {

        try {
            log.info("=== LEON PREMATCH PARSER STARTED ===");
            log.debug("Target sports: {}", ParserConfig.TARGET_SPORTS);
            long start = System.currentTimeMillis();

            processTargetSports().join();

            log.info("$$$ PARSING COMPLETED SUCCESSFULLY (in {} ms) $$$", System.currentTimeMillis() - start);

        } catch (Exception e) {
            log.error("Fatal error during parsing", e);

            throw new RuntimeException("Parsing failed", e);
        } finally {
            shutdownExecutor();
        }

    }

    private CompletableFuture<Void> processTargetSports() {
        return fetchTargetSports().thenCompose(this::processSportsList);
    }

    private CompletableFuture<List<Sport>> fetchTargetSports() {
        log.info("Fetching sports list...");
        return apiClient.getAsync(ParserConfig.SPORTS_ENDPOINT, executorService)
                .thenApply(json -> objectMapper.readValue(json, new TypeReference<List<Sport>>() {
                }))
                .thenApply(allSports -> allSports.stream()
                        .filter(s -> ParserConfig.TARGET_SPORTS.contains(s.name()))
                        .toList())
                .exceptionally(e -> {
                    log.error("Failed to fetch sports list", e);
                    return Collections.emptyList();
                });
    }

    private CompletableFuture<Void> processSportsList(List<Sport> sports) {
        log.info("Found {} target sports. Start processing...", sports.size());

        List<CompletableFuture<Void>> futures = sports.stream()
                .map(this::processEachSport)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<Void> processEachSport(Sport sport) {
        List<League> topLeagues = sport.regionList().stream()
                .flatMap(r -> r.leagues().stream())
                .filter(League::top)
                .filter(l -> l.prematch() > 0)
                .toList();

        log.info("Sport: {} -> Found {} top leagues", sport.name(), topLeagues.size());
        if (topLeagues.isEmpty()) return CompletableFuture.completedFuture(null);

        List<CompletableFuture<Void>> leagueFutures = topLeagues.stream()
                .map(league -> processLeague(sport, league))
                .toList();

        return CompletableFuture.allOf(leagueFutures.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<Void> processLeague(Sport sport, League league) {
        String url = String.format("%s?ctag=en-US&league_id=%d&hideClosed=true&flags=reg,urlv2,orn2,mm2,rrc,nodup",
                ParserConfig.EVENTS_ENDPOINT, league.id());

        return fetchAndProcess(url, EventResponse.class, response -> {
            List<Event> events = response.events().stream()
                    .limit(ParserConfig.MAX_MATCHES_PER_LEAGUE)
                    .toList();

            if (events.isEmpty()) {
                log.debug("League {}: No matches found", league.name());
                return CompletableFuture.completedFuture(null);
            }

            log.info("League {}: Processing {} matches", league.name(), events.size());

            List<CompletableFuture<Void>> eventFutures = events.stream()
                    .map(event -> processEvent(sport, league, event))
                    .toList();

            return CompletableFuture.allOf(eventFutures.toArray(new CompletableFuture[0]));
        }, "League: " + league.name());
    }

    private CompletableFuture<Void> processEvent(Sport sport, League league, Event eventStub) {
        String url = String.format("%s?eventId=%d&ctag=en-US&hideClosed=true&flags=reg,urlv2,orn2,mm2,rrc,nodup",
                ParserConfig.MARKETS_ENDPOINT, eventStub.id());

        return fetchAndProcess(url, EventResponse.class, response -> {
            if (response.events().isEmpty()) return CompletableFuture.completedFuture(null);

            Event fullEvent = response.events().getFirst();
            if (fullEvent.markets() != null && !fullEvent.markets().isEmpty()) {
                printEvent(sport, league, fullEvent);
            }
            return CompletableFuture.completedFuture(null);
        }, "Event: " + eventStub.name());
    }

    private <T> CompletableFuture<Void> fetchAndProcess(
            String url,
            Class<T> clazz,
            Function<T, CompletableFuture<Void>> processor,
            String contextInfo) {

        return apiClient.getAsync(url, executorService)
                .thenApply(json -> parseJson(json, clazz))
                .thenCompose(processor)
                .exceptionally(e -> {
                    log.error("Failed to process {}: {}", contextInfo, e.getMessage());
                    return null;
                });
    }

    private synchronized void printEvent(Sport sport, League league, Event event) {
        StringBuilder sb = new StringBuilder();

        String kickoffTime = Instant.ofEpochMilli(event.kickoff())
                .atZone(ZoneOffset.UTC)
                .format(DATE_FORMATTER);

        // Header
        sb.append(String.format("%n%s, %s%n", sport.name(), league.name()));
        sb.append(String.format("\t%s, %s, %d%n",
                event.name(), kickoffTime, event.id()));

        // Markets
        for (Market market : event.markets()) {
            sb.append(String.format("\t\t%s%n", market.name()));

            // Runners
            for (Runner runner : market.runners()) {
                sb.append(String.format("\t\t\t%s, %.2f, %d%n",
                        runner.name(),
                        runner.price(),
                        runner.id()));
            }
        }

        System.out.print(sb);
    }

    private void shutdownExecutor() {
        try {
            executorService.shutdown();
        } catch (Exception e) {
            executorService.shutdownNow();
        }
    }

    private <T> T parseJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("JSON parsing failed for class: {}", clazz.getName(), e);
            throw new RuntimeException("JSON parsing failed", e);
        }
    }

}

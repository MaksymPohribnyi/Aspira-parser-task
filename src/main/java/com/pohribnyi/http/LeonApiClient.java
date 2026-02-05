package com.pohribnyi.http;


import com.pohribnyi.config.ParserConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class LeonApiClient {

    private static final Logger log = LoggerFactory.getLogger(LeonApiClient.class);
    private static final Random random = new Random();

    private final HttpClient httpClient;

    public LeonApiClient() {
        httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(ParserConfig.REQUEST_TIMEOUT_SECONDS))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public CompletableFuture<String> getAsync(String url, Executor executor) {
        return CompletableFuture.supplyAsync(() -> getRequestWithDelay(url), executor);
    }

    private String getRequestWithDelay(String url) {
        try {

            int delay = computeRandDelay();
            Thread.sleep(delay);

            HttpRequest request = getHttpRequest(url);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode == 429) {
                log.error("Rate limit exceeded (429) for URL: {}", url);
                throw new RuntimeException("Rate limit exceeded");
            }
            if (statusCode != 200) {
                log.warn("Unexpected status code: {} for URL: {}", statusCode, url);
                throw new RuntimeException("HTTP request failed with status: " + statusCode);
            }

            return response.body();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Request interrupted: {}", url, e);

            throw new RuntimeException("Request interrupted", e);
        } catch (Exception e) {
            log.error("Request failed: {}", url, e);

            throw new RuntimeException("Request failed: " + url, e);
        }
    }

    private HttpRequest getHttpRequest(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(ParserConfig.REQUEST_TIMEOUT_SECONDS))
                .header("User-Agent", ParserConfig.USER_AGENT)
                .header("Accept", ParserConfig.ACCEPT)
                .header("Accept-Language", ParserConfig.ACCEPT_LANGUAGE)
                .header("Cookie", ParserConfig.EUA_COOKIE)
                .build();
        return request;
    }

    private static int computeRandDelay() {
        return random.nextInt(
                ParserConfig.MAX_DELAY_MS - ParserConfig.MIN_DELAY_MS) + ParserConfig.MIN_DELAY_MS;
    }

}



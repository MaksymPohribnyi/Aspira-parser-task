package com.pohribnyi.config;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParserConfig {

    public static final String BASE_URL = "https://leonbets.com";
    public static final String API_BASE = BASE_URL + "/api-2/betline";

    // Endpoints
    public static final String SPORTS_ENDPOINT = API_BASE + "/sports?ctag=en-US";
    public static final String EVENTS_ENDPOINT = API_BASE + "/events/all";
    public static final String MARKETS_ENDPOINT = API_BASE + "/related/all";

    public static final List<String> TARGET_SPORTS = List.of(
            "Football",
            "Tennis",
            "Ice Hockey",
            "Basketball"
    );

    public static final int MAX_MATCHES_PER_LEAGUE = 2;
    public static final int MAX_PARALLEL_REQUESTS = 3;

    public static final int MIN_DELAY_MS = 500;
    public static final int MAX_DELAY_MS = 2000;

    // HTTP Headers to mimic browser
    public static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 " +
                    "Safari/537.36";

    public static final String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp," +
            "image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7";
    public static final String ACCEPT_LANGUAGE = "uk,ru;q=0.9,en-US;q=0.8,en;q=0.7";

    public static final int REQUEST_TIMEOUT_SECONDS = 30;

    public static final String EUA_COOKIE = "";

}


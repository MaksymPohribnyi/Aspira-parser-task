package com.pohribnyi;


import com.pohribnyi.config.ParserConfig;
import com.pohribnyi.parser.LeonPrematchParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (ParserConfig.EUA_COOKIE.isBlank())
            throw new IllegalArgumentException("You should setup ParserConfig.EUA_COOKIE constant");

        try {
            LeonPrematchParser parser = new LeonPrematchParser();
            parser.parse();
        } catch (Exception e) {
            log.error("Application failed", e);
            System.exit(1);
        }
    }

}
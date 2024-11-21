package edu.bbte.kripto.lbim2260.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

import static java.lang.System.exit;

@Slf4j
public class ConfigProvider {
    private static final String CONFIG_FILE_NAME = "config.json";
    private static final Config config;

    static {
        Config config1;
        try (InputStream inputStream = Thread
                .currentThread()
                .getContextClassLoader()
                .getResourceAsStream(CONFIG_FILE_NAME)) {
            ObjectMapper objectMapper = new ObjectMapper();
            config1 = objectMapper.readValue(inputStream, Config.class);
        } catch (IOException e) {
            log.error("Error loading config, exiting...", e);
            config1 = new Config();
            exit(0);
        }
        config = config1;
    }

    public static Config.Profile getProfile(String profile) {
        return config.getProfiles().get(profile);
    }
}

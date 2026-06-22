package org.example;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class App 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static final String SERVICE_NAME = env("APP_NAME", "Microservices-Observability");
    private static final String LOGSTASH_HOST = env("LOGSTASH_HOST", "localhost");
    private static final String LOGSTASH_PORT = env("LOGSTASH_PORT", "5000");
    private static final long HEARTBEAT_SECONDS = positiveLong("HEARTBEAT_SECONDS", 10L);

    public static void main( String[] args )
    {
        MDC.put("service", SERVICE_NAME);
        MDC.put("logstash_host", LOGSTASH_HOST);
        MDC.put("logstash_port", LOGSTASH_PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> LOGGER.info("Shutdown requested"), "shutdown-hook"));

        LOGGER.info("Starting application");
        LOGGER.info("Publishing logs to {}:{}", LOGSTASH_HOST, LOGSTASH_PORT);

        try {
            while (!Thread.currentThread().isInterrupted()) {
                LOGGER.info("Heartbeat");
                TimeUnit.SECONDS.sleep(HEARTBEAT_SECONDS);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOGGER.info("Interrupted, stopping application");
        } finally {
            MDC.clear();
        }
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private static long positiveLong(String name, long defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            long parsed = Long.parseLong(value);
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}

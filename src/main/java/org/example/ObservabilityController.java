package org.example;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ObservabilityController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObservabilityController.class);
    private static final long MAX_DELAY_MS = 5_000L;
    private final ObservationRegistry observationRegistry;

    public ObservabilityController(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        LOGGER.info("hello endpoint invoked");
        return response("Hello from Microservices-Observability", 0L, 0L);
    }

    @GetMapping("/slow")
    public Map<String, Object> slow(@RequestParam(defaultValue = "250") long delayMs) {
        long sanitizedDelay = clamp(delayMs, 0L, MAX_DELAY_MS);
        LOGGER.info("slow endpoint invoked with delayMs={}", sanitizedDelay);
        observe("slow-work", () -> sleep(sanitizedDelay));
        return response("slow", sanitizedDelay, 1L);
    }

    @GetMapping("/busy")
    public Map<String, Object> busy(@RequestParam(defaultValue = "250") long durationMs) {
        long sanitizedDuration = clamp(durationMs, 10L, MAX_DELAY_MS);
        LOGGER.info("busy endpoint invoked with durationMs={}", sanitizedDuration);
        long iterations = observe("busy-work", () -> burnCpu(sanitizedDuration));
        return response("busy", sanitizedDuration, iterations);
    }

    @GetMapping("/error")
    public void error() {
        LOGGER.warn("error endpoint invoked");
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Simulated failure");
    }

    private Map<String, Object> response(String mode, long durationMs, long iterations) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", "ok");
        payload.put("mode", mode);
        payload.put("durationMs", durationMs);
        payload.put("iterations", iterations);
        return payload;
    }

    private long burnCpu(long durationMs) {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(durationMs);
        long iterations = 0L;
        double sink = 0.0d;

        while (System.nanoTime() < deadline) {
            sink += Math.sqrt(iterations + 1.0d);
            iterations++;
        }

        LOGGER.debug("busy endpoint completed with sink={}", sink);
        return iterations;
    }

    private void sleep(long delayMs) {
        try {
            TimeUnit.MILLISECONDS.sleep(delayMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Interrupted while waiting", ex);
        }
    }

    private long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }

    private void observe(String name, Runnable action) {
        Observation.createNotStarted(name, this.observationRegistry)
            .contextualName(name)
            .observe(action);
    }

    private <T> T observe(String name, Supplier<T> action) {
        return Observation.createNotStarted(name, this.observationRegistry)
            .contextualName(name)
            .observe(action);
    }
}

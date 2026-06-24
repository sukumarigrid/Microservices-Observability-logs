package org.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AppTest {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void prometheusEndpointExposesJvmAndHttpMetrics() throws IOException, InterruptedException {
        assertThat(get("/api/hello").statusCode()).isEqualTo(200);

        String metrics = get("/actuator/prometheus").body();
        assertThat(metrics).isNotBlank();
        assertThat(metrics).contains("jvm_memory_used_bytes");
        assertThat(metrics).contains("http_server_requests_seconds_count");
    }

    @Test
    void errorEndpointReturnsServerError() throws IOException, InterruptedException {
        assertThat(get("/api/error").statusCode()).isEqualTo(500);
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
            .GET()
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}

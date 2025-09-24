package com.ut.crawler.utils;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
@Component
public class HttpClientHelper {

    private static final Logger log = LoggerFactory.getLogger(HttpClientHelper.class);

    private final HttpClient client;

    public HttpClientHelper() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public String get(String url) throws IOException, InterruptedException {
        return get(url, 3); // 3 retries
    }

    public String get(String url, int retries) throws IOException, InterruptedException {
        for (int i = 0; i < retries; i++) {
            try {
                log.debug("Fetching URL: {}", url);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .header("User-Agent", "Mozilla/5.0 (compatible; MyCrawler/1.0)")
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                	  log.info("OK HTTP status {}",response.body());

                	return response.body();
                                  } else {
                    log.warn("Non-OK HTTP status {} for {}", response.statusCode(), url);
                }
            } catch (IOException | InterruptedException e) {
                log.error("Error fetching {} (attempt {}/{}): {}", url, i + 1, retries, e.getMessage());
                if (i == retries - 1) throw e;
            }
        }
        return null;
    }
}

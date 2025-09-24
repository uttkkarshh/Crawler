package com.ut.crawler.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ut.crawler.models.Comment;
import com.ut.crawler.models.Post;
import com.ut.crawler.models.Scroll;
import com.ut.crawler.models.Snip;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class NetworkBasedHelper implements WebHelper {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void visitUrl(String url, int waitSeconds) {
        // For network-based scraping, this might just store base URL
        System.out.println("🌐 Preparing network requests for " + url);
    }

    @Override
    public String takeScreenshot(Object driver) {
        // Not applicable in API mode
        return null;
    }

    @Override
    public Comment analyzePostContentAndComments(String mainContentSelector, String commentSelector,
                                                 String platformName) {
        try {
            // Example: GET request to fake API endpoint
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://example.com/api/comments"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(response.body());

            Comment comment = new Comment();
            comment.setScreenshotPath(null); // no screenshot in API mode
          //  comment.setText(root.get(0).get("text").asText());

            return comment;

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to fetch comments", e);
        }
    }

    @Override
    public Scroll scrollAndCaptureSnips(int count, int skip, String itemSelector, String titleSelector,
                                        String authorSelector, String platform, Map<String, List<String>> elementsToRemove) {
        Scroll scroll = new Scroll();
        try {
            // Example: API call to get posts
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://example.com/api/posts?limit=" + count))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(response.body());

            int i = 0;
            for (JsonNode node : root) {
                if (i++ >= count) break;

                Post post = new Post(
                        node.get("title").asText(),
                        node.get("url").asText(),
                        node.get("author").asText()
                );

                Snip snip = new Snip();
                snip.addPost(post);
                snip.setScroll(scroll);

                String existingDesc = scroll.getDescription() != null ? scroll.getDescription() : "";
                String postInfo = "\n- " + post.getTitle() + " (by " + post.getAuthor() + ")";
                scroll.setDescription(existingDesc + postInfo);

                scroll.addSnip(snip);
            }

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to fetch posts", e);
        }

        return scroll;
    }

    @Override
    public List<String> getTextByCssSelector(String cssSelector, int limit) {
        throw new UnsupportedOperationException("❌ CSS selector not available in network mode");
    }

    @Override
    public String getCurrentUrl() {
        return null;
    }

    @Override
    public void waitForElement(Object driver, String cssSelector, int timeoutSeconds) {
        // Not relevant in network mode
    }

    @Override
    public void resizeWindow() {
        // Not relevant
    }

    @Override
    public void quit() {
        // Nothing to quit
    }

    @Override
    public void executeJs(String js) {
        // Not applicable
    }

    @Override
    public void login(Object driver, String email, String password) {
        // Could hit a login API instead
    }
}

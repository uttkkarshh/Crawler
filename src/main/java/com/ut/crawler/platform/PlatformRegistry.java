package com.ut.crawler.platform;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlatformRegistry {

    private final Map<PlatformType, PlatformContext> platformMap = new ConcurrentHashMap<>();

    public PlatformRegistry() {
        registerDefaultPlatforms();
    }

    // Register a new platform
    public void registerPlatform(PlatformType type, PlatformContext platform) {
        platformMap.put(type, platform);
        System.out.println("Registered platform: " + type);
    }

    // Unregister a platform
    public void unregisterPlatform(PlatformType type) {
        platformMap.remove(type);
        System.out.println("Unregistered platform: " + type);
    }

    // Get all registered platforms
    public Map<PlatformType, PlatformContext> getAllPlatforms() {
        return platformMap;
    }

    // Enable/disable dynamically
    public void enablePlatform(PlatformType type, boolean enabled) {
        PlatformContext context = platformMap.get(type);
        if (context != null) {
            context.setEnabled(enabled);
            System.out.println("Platform " + type + " set enabled=" + enabled);
        }
    }

    private void registerDefaultPlatforms() {
        // YouTube config
        PlatformContext youtube = new PlatformContext();
        youtube.setName("YouTube");
        youtube.setEnabled(true);
        youtube.setRequiresLogin(false);
        youtube.setSearchUrlPattern("https://www.youtube.com/results?search_query={query}");
        youtube.setScrollSelector("ytd-video-renderer");
        youtube.setTitleSelector("a#video-title");
        youtube.setAuthorSelector(".ytd-channel-name a");
        youtube.setCommentSelector("ytd-comments");
        youtube.setPostContentSelector("div#player-container");
        youtube.setElementsToRemove(
                Map.of("id", List.of("masthead-container", "header"), 
                       "tag", List.of("tp-yt-app-drawer")));

        // Reddit config
        PlatformContext reddit = new PlatformContext();
        reddit.setName("Reddit");
        reddit.setEnabled(true);
        reddit.setRequiresLogin(false);
        reddit.setSearchUrlPattern("https://www.reddit.com/search/?q={query}");
        reddit.setScrollSelector("div[data-testid='search-post-unit']");
        reddit.setTitleSelector("a[data-testid='post-title']");
        reddit.setAuthorSelector("a:has(span:nth-of-type(2)) > span:nth-of-type(2)");
        reddit.setCommentSelector("shreddit-comment");
        reddit.setElementsToRemove(
                Map.of("id", List.of("SHORTCUT_FOCUSABLE_DIV"), 
                       "tag", List.of("header")));

        // Put into map
        platformMap.put(PlatformType.YOUTUBE, youtube);
        platformMap.put(PlatformType.REDDIT, reddit);
    }
}

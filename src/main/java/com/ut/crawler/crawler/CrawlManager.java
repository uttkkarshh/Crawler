package com.ut.crawler.crawler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ut.crawler.models.Topic;
import com.ut.crawler.platform.PlatformContext;
import com.ut.crawler.platform.PlatformType;
import com.ut.crawler.queue.BackQueueManager;

@Component
public class CrawlManager {

    private final Map<PlatformType, PlatformContext> platformMap = new HashMap<>();
    @Autowired
    private BackQueueManager backqueue;
    public CrawlManager() {
        // Register platform contexts here or via method
        registerDefaultPlatforms();
    }

    private void registerDefaultPlatforms() {
        PlatformContext youtube = new PlatformContext();
        youtube.setName("YouTube");
        youtube.setEnabled(true);
        youtube.setRequiresLogin(false);
        youtube.setSearchUrlPattern("https://www.youtube.com/results?search_query={query}");
        youtube.setScrollSelector("ytd-video-renderer");
        youtube.setTitleSelector("a#video-title");
        youtube.setAuthorSelector(".ytd-channel-name a");
        youtube.setElementsToRemove(Map.of(
            "id", List.of("masthead-container", "header"),
            "tag", List.of("tp-yt-app-drawer")
        ));

        PlatformContext reddit = new PlatformContext();
        reddit.setName("Reddit");
        reddit.setEnabled(true);
        reddit.setRequiresLogin(false);
        reddit.setSearchUrlPattern("https://www.reddit.com/search/?q={query}");
        reddit.setScrollSelector("div[data-testid='search-post-unit']");
        reddit.setTitleSelector("a[data-testid='post-title']");
        reddit.setAuthorSelector("a:has(span:nth-of-type(2)) > span:nth-of-type(2)");
        reddit.setElementsToRemove(Map.of(
            "id", List.of("SHORTCUT_FOCUSABLE_DIV"),
            "tag", List.of("header")
        ));

        platformMap.put(PlatformType.YOUTUBE, youtube);
        platformMap.put(PlatformType.REDDIT, reddit);
    }

    public void startCrawlingForTopic(Topic topic) {
    	ExecutorService executor = Executors.newFixedThreadPool(platformMap.size());

    	for (PlatformContext context : platformMap.values()) {
    	    if (context.isEnabled()) {
    	        executor.submit(new Crawler(context, topic, backqueue));
    	    }
    	}

    	executor.shutdown(); // No more tasks will be submitted

    	try {
    	    executor.awaitTermination(10, TimeUnit.MINUTES); // Wait for all to finish
    	} catch (InterruptedException e) {
    	    Thread.currentThread().interrupt();
    	    System.err.println("Execution interrupted");
    	}

    	

    }

    public void addPlatform(PlatformType type, PlatformContext context) {
        platformMap.put(type, context);
    }
}

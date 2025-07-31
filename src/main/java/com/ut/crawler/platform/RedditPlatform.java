package com.ut.crawler.platform;

import com.ut.crawler.models.Scroll;
import com.ut.crawler.models.Topic;
import com.ut.crawler.utils.BrowsingHelper;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RedditPlatform implements Platform {

    private static final Logger log = LoggerFactory.getLogger(RedditPlatform.class);
    private final BrowsingHelper helper = new BrowsingHelper();

    @Override
    public PlatformType getPlatformType() {
        return PlatformType.REDDIT;
    }

    @Override
    public Scroll crawl(Topic topic) {
        System.out.println("📥 Crawling topic: " + topic.getName());
        return searchKeyword(topic.getName());
    }

    public Scroll searchKeyword(String keyword) {
        log.info("🔍 Searching Reddit for keyword: {}", keyword);

        WebDriver driver = helper.createHeadlessDriver();
        Scroll scroll = null;

        try {
            String url = "https://www.reddit.com/search/?q=" + keyword.replace(" ", "+");
            helper.visitUrl(url, 3);
            helper.resizeWindow();

            Map<String, List<String>> elementsToRemove = new HashMap<>();
            elementsToRemove.put("id", Arrays.asList("SHORTCUT_FOCUSABLE_DIV")); // Reddit header container
            elementsToRemove.put("tag", Arrays.asList("header")); // Additional headers

            scroll = helper.scrollAndCaptureSnips(
                driver,
                15,                              // count
                2,                               // skip
                "div[data-testid='search-post-unit']", // itemSelector
                "a[data-testid='post-title']",                            // titleSelector
                "a:has(span:nth-of-type(2)) > span:nth-of-type(2)",       // authorSelector
                "Reddit",                        // platform name
                elementsToRemove
            );

        } catch (Exception e) {
            log.error("❌ Error searching Reddit for keyword '{}': {}", keyword, e.getMessage(), e);
        } finally {
            helper.quit();
        }

        return scroll;
    }

    public boolean isAvailable() {
        return true; // optional ping check
    }
}

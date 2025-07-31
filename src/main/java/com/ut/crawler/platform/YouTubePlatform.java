package com.ut.crawler.platform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;

import com.ut.crawler.models.Scroll;
import com.ut.crawler.models.Topic;
import com.ut.crawler.utils.BrowsingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class YouTubePlatform implements Platform {

    private final BrowsingHelper helper = new BrowsingHelper();
    @Override
    public PlatformType getPlatformType() {
        return PlatformType.YOUTUBE;
    }

    @Override
    public Scroll crawl(Topic topic) {
        System.out.println("📥 Crawling topic: " + topic.getName());

        Scroll scroll= searchKeyword(topic.getName());
       
        return scroll;
    }

   
    private static final Logger log = LoggerFactory.getLogger(YouTubePlatform.class);

    public Scroll searchKeyword(String keyword) {
        log.info("🔍 Searching YouTube for keyword: {}", keyword);

       
        WebDriver driver = helper.createHeadlessDriver();
        Scroll scroll=null;
        try {
            String url = "https://www.youtube.com/results?search_query=" + keyword.replace(" ", "+");
            helper.visitUrl( url, 2);
            helper.resizeWindow();
            Map<String, List<String>> elementsToRemove = new HashMap<>();
            elementsToRemove.put("id", Arrays.asList("masthead-container", "header"));
            elementsToRemove.put("tag", Arrays.asList("tp-yt-app-drawer"));

            scroll = helper.scrollAndCaptureSnips(
            	    driver,
            	    10,                              // count: how many posts to process
            	    3,                               // skip: how many to skip after each
            	    "ytd-video-renderer",            // itemSelector: selector for post container
            	    "a#video-title",                 // titleSelector: selector for title link
            	    ".ytd-channel-name a",            // authorSelector: selector for author link
            	    "YouTube"    
            	    ,elementsToRemove
            	);


        } catch (Exception e) {
            log.error("❌ Error searching YouTube for keyword '{}': {}", keyword, e.getMessage(), e);
        } finally {
            helper.quit();
        }

        return  scroll;
    }
    
    
    
    
    
    
    
    // Optional: add more helper methods
    public boolean isAvailable() {
        // ping https://youtube.com or check basic page load
        return true;
    }
}

package com.ut.crawler.crawler;

import com.ut.crawler.models.Topic;
import com.ut.crawler.platform.PlatformContext;
import com.ut.crawler.queue.BackQueueManager;
import com.ut.crawler.models.CrawlUrl;
import com.ut.crawler.models.Post;
import com.ut.crawler.models.PriorityLevel;
import com.ut.crawler.models.Scroll;
import com.ut.crawler.models.Snip;
import com.ut.crawler.utils.BrowsingHelper;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Crawler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Crawler.class);
    private final PlatformContext platform;
    private final Topic topic;
    private final BrowsingHelper helper = new BrowsingHelper();
    private final BackQueueManager backqueue;

    public Crawler(PlatformContext platform, Topic topic,BackQueueManager backqueue) {
        this.platform = platform;
        this.topic = topic;
        this.backqueue=backqueue;
    }

    @Override
    public void run() {
        if (!platform.isEnabled()) {
            log.info("❌ Platform '{}' is disabled.", platform.getName());
            return;
        }

        if (platform.isRequiresLogin()) {
            log.info("🔐 Platform '{}' requires login. Login logic not implemented yet.", platform.getName());
            return;
        }

        try {
            Scroll scroll = crawl();
            if (scroll != null) {
                log.info("✅ Crawled {} snips from {} for topic '{}'", 
                    scroll.getSnips().size(), platform.getName(), topic.getName());
            }
        } catch (Exception e) {
            log.error("❌ Error crawling {}: {}", platform.getName(), e.getMessage(), e);
        }
    }
    
    private Scroll crawl() {
        WebDriver driver = helper.createHeadlessDriver();
        Scroll scroll = null;

        try {
            String searchUrl = platform.buildSearchUrl(topic.getName());
            helper.visitUrl( searchUrl, 2);
            helper.resizeWindow();

            scroll = helper.scrollAndCaptureSnips(
                driver,
                10, // max scrolls
                3,  // delay
                platform.getScrollSelector(),
                platform.getTitleSelector(),
                platform.getAuthorSelector(),
                platform.getName(),
                platform.getElementsToRemove()
            );
            
            scroll.setTopic(topic);
            topic.getScrolls().add(scroll);
            for(Snip snips:scroll.getSnips()) {           	
            	for(Post post:snips.getPosts()) {
            		backqueue.enqueueByDomain(new CrawlUrl(post.getUrl(),PriorityLevel.HIGH,"YOUTUBE"));
            	}
            }
           Scroll postScroll= recurse(0);
           postScroll.setTopic(topic);
           topic.getScrolls().add(postScroll);
            
        } catch(Exception e){
        	
        }finally {
        
            helper.quit();
        }

        return scroll;
    }
    
    Scroll recurse(int level) throws Exception {
        Scroll scroll = new Scroll();
        
        log.info("🔁 Entering recurse() at level {}", level);
        log.info("📦 Dumping all domain queues in BackQueueManager:");
       
        Map<String, Queue<CrawlUrl>> allQueues = backqueue.getAllQueues();

        System.out.println("📦 Dumping all domain queues in BackQueueManager:");
        for (Map.Entry<String, Queue<CrawlUrl>> entry : allQueues.entrySet()) {
            String domain = entry.getKey();
            Queue<CrawlUrl> queue = entry.getValue();

            System.out.println("🔹 Domain: " + domain + " | Queue Size: " + queue.size());

            for (CrawlUrl url : queue) {
                System.out.println("    🌐 " + url.getUrl());
            }
        }
        while (level < 3) {
            CrawlUrl url = backqueue.dequeueFromDomain("YOUTUBE");
            
            if (url == null) {
                log.warn("⚠️ No URL found in domain queue at level {}. Exiting loop.", level);
                break;
            }

            String nextUrl = url.getUrl();
            log.info("🌐 Processing URL at level {}: {}", level, nextUrl);

            try {
                log.debug("🧭 Visiting URL: {}", nextUrl);
                helper.visitUrl(nextUrl, 2); // open page
                helper.resizeWindow();

                Scroll scrolltemp = helper.analyzePostContentAndComments(
                    "div#player-container",           // Post content selector
                    "ytd-comments", // Comment section selector
                    "YOUTUBE"
                );

                if (scrolltemp != null) {
                    log.info("✅ Scroll created for URL at level {}: {}", level, nextUrl);
                    scroll.merge(scrolltemp);
                } else {
                    log.warn("⚠️ Scroll returned null for URL: {}", nextUrl);
                }
               
                // Optional: log some scroll content to verify
                // log.debug("Scroll content snippet: {}", scroll.getSomeField());

            } catch (Exception e) {
                log.error("❌ Error crawling URL at level {}: {}", level, nextUrl, e);
            } 
            level++;
            log.info("↪️ Moving to next level: {}", level);
        }

        log.info("🔚 Exiting recurse(). Final scroll: {}", scroll != null ? "created" : "null");
        return scroll;
    }

}

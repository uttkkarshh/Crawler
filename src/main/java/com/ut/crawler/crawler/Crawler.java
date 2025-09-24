package com.ut.crawler.crawler;

import com.ut.crawler.models.Topic;
import com.ut.crawler.models.TypeOfUrl;
import com.ut.crawler.platform.PlatformContext;
import com.ut.crawler.platform.PlatformRegistry;
import com.ut.crawler.queue.BackQueueManager;
import com.ut.crawler.repository.TopicRepository;
import com.ut.crawler.service.UrlProcessingService;
import com.ut.crawler.models.CrawlUrl;
import com.ut.crawler.utils.BrowsingHelper;
import com.ut.crawler.utils.WebDriverPool;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Crawler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Crawler.class);

    private final PlatformContext platform;
    private Topic topic;

    private final BackQueueManager backqueue;
    private volatile boolean running = true;
    private final UrlProcessingService urlProcessingService;
    private final WebDriverPool driverPool;
    private final PlatformRegistry platformRegistry;
    public Crawler(PlatformContext platform,
                   BackQueueManager backqueue,
                   UrlProcessingService urlProcessingService,
                   WebDriverPool driverPool, PlatformRegistry platformRegistry) {
        this.platform = platform;
        this.backqueue = backqueue;
        this.urlProcessingService = urlProcessingService;
        this.driverPool = driverPool;
		this.platformRegistry = platformRegistry;
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
            crawl();
        } catch (Exception e) {
            log.error("❌ Error crawling {}: {}", platform.getName(), e.getMessage(), e);
        }
    }

    private void crawl() {
        WebDriver driver = null;
        BrowsingHelper helper = null;
        int level = 0;

        try {
            driver = driverPool.getDriver();
            helper = new BrowsingHelper(driver);

            while (running) {
                CrawlUrl url = null;
                try {
                    url = backqueue.takeFromDomain(platform.getName().toUpperCase());
                } catch (InterruptedException ie) {
                    log.info("🛑 Crawler interrupted, stopping gracefully.");
                    Thread.currentThread().interrupt(); // reset interrupt flag
                    running = false; // exit loop
                    break;
                }

                if (url == null) {
                    log.warn("⚠️ No URL found in domain queue. Exiting loop.");
                    break;
                }

                log.info("🔍 Processing URL: {} [{}]", url.getUrl(), url.getType());

                try {
                    if (url.getType() == TypeOfUrl.SEED) {
                        topic = new Topic();
                        topic.setName(url.getUrl());
                        urlProcessingService.processSeed(platform, topic, helper);
                        log.info("✅ Successfully processed SEED URL.");
                    } else if (url.getType() == TypeOfUrl.POST) {
                        urlProcessingService.processPost(platform, url.getUrl(), helper);
                        log.info("🗨️ Successfully processed POST for comments.");
                    } else {
                        log.warn("❓ Unknown TypeOfUrl: {}", url.getType());
                    }
                } catch (Exception e) {
                    log.error("❌ Error processing URL {}: {}", url.getUrl(), e.getMessage(), e);
                }

                level++;
            }

            if (topic != null) {
                log.info("✅ Finished crawling {} for topic '{}'.", platform.getName(), topic.getName());
            } else {
                log.info("✅ Finished crawling {} (no topic found).", platform.getName());
            }

        } catch (InterruptedException e) {
            log.info("🛑 Interrupted while waiting for WebDriver, stopping gracefully.");
            Thread.currentThread().interrupt();
            running = false;
        } catch (Exception e) {
            log.error("❌ Unexpected error during crawl: {}", e.getMessage(), e);
        } finally {
            if (driver != null) {
                driverPool.releaseDriver(driver);
                log.info("📥 Returned WebDriver to pool.");
            }
        }
    }


    public void stop() {
        this.running = false;
    }
}

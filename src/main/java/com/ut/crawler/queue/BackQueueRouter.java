package com.ut.crawler.queue;



import com.ut.crawler.models.CrawlUrl;
import com.ut.crawler.models.PriorityLevel;
import com.ut.crawler.models.TypeOfUrl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class BackQueueRouter  {

    @Autowired
    private BackQueueManager backqueue;
    private volatile boolean running = true;
    @Autowired
    private FrontQueueManager frontqueue;
    private static final Logger log = LoggerFactory.getLogger(BackQueueRouter.class); 

    public void stop() {
        running = false;
    }

    @Async("crawlerExecutor")
    public void run() {
        log.info("🚀 Router thread starting...");

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                CrawlUrl url = frontqueue.dequeueHighestPriority();

                if (url == null) {
                    log.debug("No URLs found in FRONT queue. Sleeping for 1 second...");
                    Thread.sleep(1000);
                    continue;
                }

                log.info("📦 Routing URL from FRONT to BACK: {}", url);
                backqueue.enqueueByDomain(url);
                log.debug("✅ Successfully moved {} to BACK queue.", url);

            } catch (InterruptedException e) {
                // Preserve interrupt status and stop the loop
                Thread.currentThread().interrupt();
                log.warn("⚠️ Router thread interrupted. Shutting down gracefully...");
                break;
            } catch (Exception e) {
                log.error("❌ Unexpected error in router loop", e);
            }
        }

        log.info("🛑 Router thread stopped.");
    }
    

    }

package com.ut.crawler.queue;

import com.ut.crawler.models.CrawlUrl;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

@Component
public class BackQueueManager {

    // Thread-safe map for domain-specific blocking queues
    private final ConcurrentMap<String, BlockingQueue<CrawlUrl>> domainQueues = new ConcurrentHashMap<>();

    // Add URL to the queue for its domain (creates queue if not exists)
    public void enqueueByDomain(CrawlUrl url) {
        String domain = url.getPlatform().toUpperCase();
        domainQueues
            .computeIfAbsent(domain, k -> new LinkedBlockingQueue<>())
            .offer(url);
    }

    /**
     * Blocking take from a specific domain queue
     * Waits until an item is available
     */
    public CrawlUrl takeFromDomain(String domain) throws InterruptedException {
        BlockingQueue<CrawlUrl> queue = domainQueues
            .computeIfAbsent(domain.toUpperCase(), k -> new LinkedBlockingQueue<>());
        return queue.take(); // waits if empty
    }

    /**
     * Non-blocking poll from a domain queue
     */
    public CrawlUrl pollFromDomain(String domain) {
        BlockingQueue<CrawlUrl> queue = domainQueues.get(domain.toUpperCase());
        return (queue != null) ? queue.poll() : null;
    }

    public Map<String, BlockingQueue<CrawlUrl>> getAllQueues() {
        return domainQueues;
    }
}

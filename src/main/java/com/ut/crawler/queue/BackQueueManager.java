package com.ut.crawler.queue;




import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.Queue;
import java.util.Map;

import com.ut.crawler.models.CrawlUrl;
import org.springframework.stereotype.Component;

@Component
public class BackQueueManager {

    // Thread-safe map for domain-specific queues
    private final ConcurrentMap<String, Queue<CrawlUrl>> domainQueues = new ConcurrentHashMap<>();

    // Enqueue URL to the appropriate domain queue
    public void enqueueByDomain(CrawlUrl url) {
        String domain = url.getPlatform();
        domainQueues.computeIfAbsent(domain, k -> new ConcurrentLinkedQueue<>()).add(url);
    }

    // Dequeue from a specific domain queue
    public CrawlUrl dequeueFromDomain(String domain) {
        Queue<CrawlUrl> queue = domainQueues.get(domain);
        return (queue != null) ? queue.poll() : null;
    }

    // Expose a read-only view (you can deep copy if full isolation is needed)
    public Map<String, Queue<CrawlUrl>> getAllQueues() {
        return domainQueues;
    }
}

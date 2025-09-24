package com.ut.crawler.queue;



import com.ut.crawler.models.CrawlUrl;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class HeapScheduler {

    private final BackQueueManager backQueueManager;

    // store per-domain rate limit in ms
    private final Map<String, Long> domainRateLimits = new ConcurrentHashMap<>();

    // heap entry
    private static class DomainEntry implements Comparable<DomainEntry> {
        final String domain;
        long nextAllowedTime; // epoch ms

        DomainEntry(String domain, long nextAllowedTime) {
            this.domain = domain;
            this.nextAllowedTime = nextAllowedTime;
        }

        @Override
        public int compareTo(DomainEntry other) {
            return Long.compare(this.nextAllowedTime, other.nextAllowedTime);
        }
    }

    private final PriorityBlockingQueue<DomainEntry> domainHeap = new PriorityBlockingQueue<>();
    private final Map<String, DomainEntry> domainEntries = new ConcurrentHashMap<>();

    public HeapScheduler(BackQueueManager backQueueManager) {
        this.backQueueManager = backQueueManager;
    }

    public void registerDomain(String domain, long rateLimitMs) {
        domainRateLimits.put(domain.toUpperCase(), rateLimitMs);
        DomainEntry entry = new DomainEntry(domain.toUpperCase(), 0);
        domainHeap.offer(entry);
        domainEntries.put(domain.toUpperCase(), entry);
    }

    /**
     * Fetch the next available URL, respecting domain rate limits.
     */
    public CrawlUrl take() throws InterruptedException {
        while (true) {
            DomainEntry entry = domainHeap.take(); // earliest domain
            long now = System.currentTimeMillis();

            if (now < entry.nextAllowedTime) {
                // too early → wait until allowed
                long sleepMs = entry.nextAllowedTime - now;
                Thread.sleep(sleepMs);
            }

            CrawlUrl url = backQueueManager.takeFromDomain(entry.domain);
            if (url != null) {
                // update next allowed time
                long delay = domainRateLimits.getOrDefault(entry.domain, 1000L);
                entry.nextAllowedTime = System.currentTimeMillis() + delay;
            }

            // reinsert domain into heap (always put it back)
            domainHeap.offer(entry);

            if (url != null) {
                return url;
            }
            // If queue empty, loop again and check another domain
        }
    }
}

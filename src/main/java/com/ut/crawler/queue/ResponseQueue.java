package com.ut.crawler.queue;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.ut.crawler.models.CrawlUrl;



public class ResponseQueue {

    private Queue<CrawlUrl> queue = new ConcurrentLinkedQueue<>();

    // Add a URL to the queue
    public void enqueue(CrawlUrl url) {
        if (url != null) {
            queue.add(url);
        }
    }

    // Remove and return the next URL from the queue
    public CrawlUrl dequeue() {
        return queue.poll(); // returns null if queue is empty
    }

    // Peek at the next URL without removing it
    public CrawlUrl peek() {
        return queue.peek();
    }

    // Check if the queue is empty
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    // Get the size of the queue
    public int size() {
        return queue.size();
    }

    // Clear the queue
    public void clear() {
        queue.clear();
    }

    // Check if the queue contains a specific URL
    public boolean contains(CrawlUrl url) {
        return queue.contains(url);
    }
}

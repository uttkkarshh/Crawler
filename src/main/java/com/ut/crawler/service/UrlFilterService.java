package com.ut.crawler.service;

import org.springframework.stereotype.Service;

import com.ut.crawler.repository.PostRepository;

@Service
public class UrlFilterService {
	private final PostRepository postRepository;

    public UrlFilterService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * Check if URL already exists in DB, return false if duplicate.
     */
    public boolean allowUrl(String url) {
        boolean exists = postRepository.existsByUrl(url);
        return !exists;
    }
}

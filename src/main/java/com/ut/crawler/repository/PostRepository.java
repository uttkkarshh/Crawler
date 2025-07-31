package com.ut.crawler.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ut.crawler.models.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

}

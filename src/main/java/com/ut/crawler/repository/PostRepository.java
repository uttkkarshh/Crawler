package com.ut.crawler.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ut.crawler.models.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
	 Optional<Post> findByUrl(String url);  // 🔍 Add this line

	boolean existsByUrl(String url);
}

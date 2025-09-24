package com.ut.crawler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ut.crawler.models.Comment;
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
      
}

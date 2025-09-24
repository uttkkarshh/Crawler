package com.ut.crawler.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ut.crawler.models.Comment;
import com.ut.crawler.models.Post;
import com.ut.crawler.repository.CommentRepository;
import com.ut.crawler.repository.PostRepository;

@Service
public class PostService { 
	 @Autowired
	 PostRepository postRepository;
     @Autowired
     CommentRepository commentRepository;
     
     private static final Logger logger = LoggerFactory.getLogger(PostService.class);
     @Transactional
     public void addCommentToPost(String url, Comment comment) {
         logger.info("Attempting to add comment to post with URL: {}", url);
         
         Optional<Post> post = postRepository.findByUrl(url);
         if (post.isPresent()) {
             logger.debug("Post found with URL: {}", url);

             post.get().addComment(comment); // modifies the post entity
             comment.setPost(post.get());

             postRepository.save(post.get()); // persists changes in same transaction

             logger.info("Comment added successfully to post with URL: {}", url);
         } else {
             logger.warn("No post found with URL: {}", url);
         }
     }
}


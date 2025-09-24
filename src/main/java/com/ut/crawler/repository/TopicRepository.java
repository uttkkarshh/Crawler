package com.ut.crawler.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ut.crawler.models.Topic;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
	@Query(value = "SELECT * FROM topic WHERE search_vector @@ plainto_tsquery('english', :query)", 
		       nativeQuery = true)
		List<Topic> searchTopics(@Param("query") String query);

}

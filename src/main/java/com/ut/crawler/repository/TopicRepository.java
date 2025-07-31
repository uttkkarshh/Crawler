package com.ut.crawler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ut.crawler.models.Topic;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

}

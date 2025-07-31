package com.ut.crawler.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ut.crawler.models.Snip;

public interface SnipRepository extends JpaRepository<Snip, Long> {}
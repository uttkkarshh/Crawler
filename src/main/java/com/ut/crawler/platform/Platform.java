package com.ut.crawler.platform;

import java.util.List;

import com.ut.crawler.models.Scroll;
import com.ut.crawler.models.Topic;

public interface Platform {

	PlatformType getPlatformType();

	Scroll crawl(Topic topic);



}

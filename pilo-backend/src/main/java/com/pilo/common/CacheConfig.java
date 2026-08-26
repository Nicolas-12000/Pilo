package com.pilo.common;

import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

	@Bean
	CacheManager cacheManager() {
		return new ConcurrentMapCacheManager(List.of(CacheNames.PROCEDURE_CATALOG).toArray(String[]::new));
	}
}

package com.aplana.sbrf.taxaccounting.cache;

import org.springframework.cache.support.SimpleCacheManager;

public class ExtendedSimpleCacheManager extends SimpleCacheManager implements CacheManagerDecorator{

	@Override
	public void clearAll() {
		for (String cacheName : getCacheNames()) {
			getCache(cacheName).clear();
		}
	}

}

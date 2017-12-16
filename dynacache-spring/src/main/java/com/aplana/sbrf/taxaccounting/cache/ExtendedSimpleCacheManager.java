package com.aplana.sbrf.taxaccounting.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleCacheManager;

public class ExtendedSimpleCacheManager extends SimpleCacheManager implements CacheManagerDecorator {
    private static final Log LOG = LogFactory.getLog(ExtendedSimpleCacheManager.class);

    @Override
    public void clearAll() {
        for (String cacheName : getCacheNames()) {
            LOG.info("Clear cache: " + cacheName);
            getCache(cacheName).clear();
        }
    }

    @Override
    public void evict(String cacheName, Object key) {
        getCache(cacheName).evict(key);
    }

    @Override
    public void evictAll(String cacheName) {
        getCache(cacheName).clear();
    }
}

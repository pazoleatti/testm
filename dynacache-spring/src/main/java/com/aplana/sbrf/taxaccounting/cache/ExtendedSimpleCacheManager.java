package com.aplana.sbrf.taxaccounting.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.support.SimpleCacheManager;

public class ExtendedSimpleCacheManager extends SimpleCacheManager implements CacheManagerDecorator {
    private static final Log LOG = LogFactory.getLog(ExtendedSimpleCacheManager.class);

    @Override
    public void clearAll() {
        for (String cacheName : getCacheNames()) {
            getCache(cacheName).clear();
        }
        LOG.info("Cache was cleared");
    }
}

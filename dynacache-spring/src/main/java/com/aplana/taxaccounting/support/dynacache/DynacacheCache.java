package com.aplana.taxaccounting.support.dynacache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.Cache;

import com.ibm.websphere.cache.DistributedObjectCache;

/**
 * Реализация интерфейса Cache для работы с IBM Dynacache 
 * @author DSultanbekov
 */
public class DynacacheCache implements Cache {
	private Log logger = LogFactory.getLog(getClass());
	
	private DistributedObjectCache cache;
	private String name;
	
	public DynacacheCache(String name, DistributedObjectCache cache) {
		this.name = name;
		this.cache = cache;
	}

	@Override
	public void clear() {
		cache.clear();
	}

	@Override
	public void evict(Object key) {
		if (logger.isDebugEnabled()) {
			logger.debug("Removing element with key = " + key + " from cache '" + name + "'");
		}
		cache.remove(key);
	}

	@Override
	public ValueWrapper get(Object key) {
		final Object value = cache.get(key);
		
		if (value == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Value with key = " + key + " not found in cache '" + name + "'");
			}
			return null;
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Value with key = " + key + " found in cache '" + name + "'");
			}
			return new ValueWrapper() {
				@Override
				public Object get() {
					return value;
				}
			};
		}
		
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getNativeCache() {
		return cache;
	}

	@Override
	public void put(Object key, Object value) {
		if (logger.isDebugEnabled()) {
			logger.debug("Saving element with key = " + key + " to cache '" + name + "'");
		}
		cache.put(key, value);
	}
}

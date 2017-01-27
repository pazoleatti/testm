package com.aplana.sbrf.taxaccounting.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.io.Serializable;
import java.util.Map;

public class MapCache implements Cache {

	private static final Log LOG = LogFactory.getLog(MapCache.class);
	private static final Object NULL_HOLDER = new NullHolder();
	private final String name;
	private final Map<Object, Object> store;

	public MapCache(String name, Object store) {
		this.name = name;
		this.store = (Map<Object, Object>) store;
		this.store.clear();
		LOG.info("Cache '" + name + "' is created, store: " + this.store.getClass());
	}

	@Override
    public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.cache.Cache#getNativeCache()
	 */
	@Override
	public Map<Object, Object> getNativeCache() {
		return store;
	}

	@Override
    public ValueWrapper get(Object key) {
		Object value = store.get(key);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Get element with key = " + key + " from cache '" + name
					+ "'. Value present: " + (value != null));
		}
		return (value != null ? new SimpleValueWrapper(fromStoreValue(value))
				: null);
	}

    @Override
    public <T> T get(Object key, Class<T> type) {
        return (T) fromStoreValue(store.get(key));
    }

    @Override
    public void put(Object key, Object value) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Put element with key = " + key + " to cache '" + name + "'");
		}
		store.put(key, toStoreValue(value));
	}

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
		Object existingValue = fromStoreValue(store.get(key));
		if (existingValue == null) {
			store.put(key, toStoreValue(value));
			return null;
		} else {
			return new SimpleValueWrapper(existingValue);
		}
    }

    @Override
	public void evict(Object key) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Remove element with key = " + key + " from cache '" + name + "'");
		}
		this.store.remove(key);
	}

	@Override
	public void clear() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Clear cache '" + name + "'");
		}
		this.store.clear();
	}

	/**
	 * Конвертирует полученое из хранилища значение для использования
	 * 
	 * @param storeValue
	 *            значение из хранилища
	 * @return the value to return to the user
	 */
	protected Object fromStoreValue(Object storeValue) {
		if (storeValue == null || storeValue instanceof NullHolder) {
			return null;
		}
		return storeValue;
	}

	protected Object toStoreValue(Object userValue) {
		if (userValue == null) {
			return NULL_HOLDER;
		}
		return userValue;
	}

	private static class NullHolder implements Serializable {
		private static final long serialVersionUID = 5124311109167803547L;
	}
	
}
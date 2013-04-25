package com.aplana.sbrf.taxaccounting.cache;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

public class MapCache implements Cache {

	final Log log = LogFactory.getLog(getClass());

	private static final Object NULL_HOLDER = new NullHolder();

	private final String name;

	private final Map<Object, Object> store;

	private final boolean allowNullValues;

	@SuppressWarnings("unchecked")
	public MapCache(String name, Object store,
			boolean allowNullValues, boolean needClear) {
		this.name = name;
		this.store = (Map<Object, Object>) store;
		this.allowNullValues = allowNullValues;
		if (needClear) {
			this.store.clear();
		}
		log.info("Cache '" + name + "' is created, store: " + this.store.getClass()
				+ ", store size: " + this.store.size());
	}

	public MapCache(String name, Object store) {
		this(name, store, true, true);
	}

	public String getName() {
		return this.name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.cache.Cache#getNativeCache()
	 */
	@Override
	public Map<Object, Object> getNativeCache() {
		return this.store;
	}

	public boolean isAllowNullValues() {
		return this.allowNullValues;
	}

	public ValueWrapper get(Object key) {
		if (log.isDebugEnabled()) {
			log.debug("Get element with key = " + key + " from cache '" + name
					+ "'");
		}
		Object value = this.store.get(new KeyWrapper(this.name, key));
		return (value != null ? new SimpleValueWrapper(fromStoreValue(value))
				: null);
	}

	public void put(Object key, Object value) {
		if (log.isDebugEnabled()) {
			log.debug("Put element with key = " + key + " to cache '" + name
					+ "'");
		}
		this.store.put(new KeyWrapper(this.name, key), toStoreValue(value));
	}

	@Override
	public void evict(Object key) {
		if (log.isDebugEnabled()) {
			log.debug("Remove element with key = " + key + " from cache '"
					+ name + "'");
		}
		this.store.remove(new KeyWrapper(this.name, key));
	}

	@Override
	public void clear() {
		if (log.isDebugEnabled()) {
			log.debug("Clear cache '" + name + "'");
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
		if (this.allowNullValues && NULL_HOLDER.equals(storeValue)) {
			return null;
		}
		return storeValue;
	}

	protected Object toStoreValue(Object userValue) {
		if (this.allowNullValues && userValue == null) {
			return NULL_HOLDER;
		}
		return userValue;
	}

	private static class NullHolder implements Serializable {
		private static final long serialVersionUID = 5124311109167803547L;
	}
	
}

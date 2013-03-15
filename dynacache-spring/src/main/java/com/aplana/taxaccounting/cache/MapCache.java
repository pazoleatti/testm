package com.aplana.taxaccounting.cache;

import java.io.Serializable;
import java.util.Map;

import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

public class MapCache implements Cache {

	private static final Object NULL_HOLDER = new NullHolder();

	private final String name;

	private final Map<Object, Object> store;

	private final boolean allowNullValues;

	/**
	 * Create a new ConcurrentMapCache with the specified name and the given
	 * internal ConcurrentMap to use.
	 * 
	 * @param name
	 *            the name of the cache
	 * @param store
	 *            the ConcurrentMap to use as an internal store
	 * @param allowNullValues
	 *            whether to allow <code>null</code> values (adapting them to an
	 *            internal null holder value)
	 */
	public MapCache(String name, Map<Object, Object> store,
			boolean allowNullValues) {
		this.name = name;
		this.store = store;
		this.allowNullValues = allowNullValues;
	}

	public String getName() {
		return this.name;
	}

	/* (non-Javadoc)
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
		Object value = this.store.get(key);
		return (value != null ? new SimpleValueWrapper(fromStoreValue(value))
				: null);
	}

	public void put(Object key, Object value) {
		this.store.put(key, toStoreValue(value));
	}

	@Override
	public void evict(Object key) {
		this.store.remove(key);
	}

	@Override
	public void clear() {
		this.store.clear();
	}

	/**
	 * Convert the given value from the internal store to a user value returned
	 * from the get method (adapting <code>null</code>).
	 * 
	 * @param storeValue
	 *            the store value
	 * @return the value to return to the user
	 */
	protected Object fromStoreValue(Object storeValue) {
		if (this.allowNullValues && storeValue == NULL_HOLDER) {
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

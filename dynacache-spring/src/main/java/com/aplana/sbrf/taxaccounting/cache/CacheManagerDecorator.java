package com.aplana.sbrf.taxaccounting.cache;

import org.springframework.cache.Cache;

public interface CacheManagerDecorator {

	/**
	 * Очищает все кэши
	 */
	void clearAll();

	/**
	 * Очищает определенное значение в определенном кэше
	 * @param cacheName название кэша
	 * @param key ключ объекта, который будет удален из кэша
	 */
	void evict(String cacheName, Object key);

	/**
	 * Очищает все значения в указанном кэше
	 * @param cacheName название кэша
	 */
	void evictAll(String cacheName);

	/**
	 * Возвращает кэш по его имени
	 * @param name название кэша
	 * @return кэш
	 */
	Cache getCache(String name);
}

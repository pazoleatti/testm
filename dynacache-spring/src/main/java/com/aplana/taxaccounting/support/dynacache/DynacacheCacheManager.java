package com.aplana.taxaccounting.support.dynacache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;

import com.ibm.websphere.cache.DistributedObjectCache;

public class DynacacheCacheManager extends AbstractCacheManager {
	private Map<String, String> caches;

	/**
	 * Задать настройки кэшей в виде Map
	 * @param caches Map, где ключи - это имена кэшей (для использования в аннотациях Spring Cache),
	 * 		а значения - это имена JNDI, под которыми развёрнуты соответствующие объекты Dynacache
	 */
	public void setCaches(Map<String, String> caches) {
		this.caches = caches;
	}
	
	@Override
	protected Collection<? extends Cache> loadCaches() {
		if (caches == null) {
			throw new IllegalStateException("cacheMap property is not initialized");
		}
		List<DynacacheCache> result = new ArrayList<DynacacheCache>(caches.size());
		
		try {
			InitialContext ic = new InitialContext();
			for (Map.Entry<String, String> entry: caches.entrySet()) {
				DistributedObjectCache cache = (DistributedObjectCache)ic.lookup(entry.getValue());
				result.add(new DynacacheCache(entry.getKey(), cache));
			}
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
		
		return result;
	}
}

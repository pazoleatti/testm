package com.aplana.sbrf.taxaccounting.cache;

import java.io.Serializable;

/**
 * @author sgoryachkin
 *
 */
public class CacheKey implements Serializable{
	private static final long serialVersionUID = -1548607026613495003L;
	
	public CacheKey(String keyspace, Object key) {
		super();
		this.keyspace = keyspace;
		this.key = key;
	}
	
	public static CacheKey create(String keyspace, Object key){
		return new CacheKey(keyspace, key);
	}

	/**
	 * Тэг 
	 */
	String keyspace;
	
	/**
	 * Нативный ключ
	 */
	Object key;
	
	
	public String getKeyspace() {
		return keyspace;
	}

	public void setKeyspace(String keyspace) {
		this.keyspace = keyspace;
	}
	
	public Object getKey() {
		return key;
	}

	public void setKey(Object key) {
		this.key = key;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result
				+ ((keyspace == null) ? 0 : keyspace.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CacheKey))
			return false;
		CacheKey other = (CacheKey) obj;
		
		if (keyspace == null) {
			if (other.keyspace != null)
				return false;
		} else if (!keyspace.equals(other.keyspace))
			return false;
		
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		
		return true;
	}




}

package com.aplana.sbrf.taxaccounting.cache;


/**
 * Обертка для ключа. Нужна для того чтобы можно было использовать 
 * один и тот же сторедж для разных кэшей
 * 
 * @author sgoryachkin
 *
 */
public class KeyWrapper{
	
	private String cacheName; 
	private Object key;
	
	public KeyWrapper(String cacheName, Object key) {
		super();
		this.cacheName = cacheName;
		this.key = key;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((cacheName == null) ? 0 : cacheName.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof KeyWrapper))
			return false;
		KeyWrapper other = (KeyWrapper) obj;
		if (cacheName == null) {
			if (other.cacheName != null)
				return false;
		} else if (!cacheName.equals(other.cacheName))
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("KeyWrapper [cacheName=");
		builder.append(cacheName);
		builder.append(", key=");
		builder.append(key);
		builder.append("]");
		return builder.toString();
	}
	
}
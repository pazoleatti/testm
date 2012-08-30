package com.aplana.sbrf.taxaccounting.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Строка данных отчётной формы
 * @author dsultanbekov
 */
public class DataRow {
	/**
	 * Обёртка над обычным HashMap, основная цель которой - предотвратить заполнение строки данными неверного типа
	 * Поддерживаются строки, даты, BigDecimal, для некоторых числовых типов реализовано автоматическое приведение к BigDecimal
	 * Также не позволяет добавлять в строку данные по столбцам, которые отсутствуют в определении формы 
	 */
	private class DataRowValuesMap implements Map<String, Object> {
		final Map<String, Object> store;
		final Form form;
		
		public DataRowValuesMap(Form form) {
			this.form = form;
			this.store = new HashMap<String, Object>(form.getColumns().size());
		}
		
		public void clear() {
			store.clear();
		}

		@Override
		public boolean containsKey(Object key) {
			return store.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return store.containsValue(value);
		}

		@Override
		public Set<java.util.Map.Entry<String, Object>> entrySet() {
			return store.entrySet();
		}

		@Override
		public Object get(Object key) {
			return store.get(key);
		}

		@Override
		public boolean isEmpty() {
			return store.isEmpty();
		}

		@Override
		public Set<String> keySet() {
			return store.keySet();
		}

		@Override
		public Object put(String key, Object value) {
			// Если столбец не удастся найти, то получим исключение
			// Это нормально			
			Column col = form.getColumn(key);
			
			if (value instanceof Integer) {
				value = new BigDecimal((Integer)value);
			} else if (value instanceof Double) {
				value = new BigDecimal((Double)value);
			} else if (value instanceof Long) {
				value = new BigDecimal((Long)value);
			}
			
			if (col instanceof NumericColumn) {
				value = ((BigDecimal)value).setScale(((NumericColumn) col).getPrecision(), RoundingMode.HALF_UP); 
			}
			
			if (value == null || value instanceof BigDecimal || value instanceof String || value instanceof Date) {
				return store.put(key, value);				
			} else {
				throw new IllegalArgumentException("Values of type " + value.getClass().getName() + " are not supported");
			}
		}

		@Override
		public void putAll(Map<? extends String, ? extends Object> map) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object remove(Object key) {
			return store.remove(key);
		}

		@Override
		public int size() {
			return store.size();
		}

		@Override
		public Collection<Object> values() {
			return store.values();
		}
	}
	
	private final Map<String, Object> data;
	private String alias;

	public DataRow(String alias, Form form) {
		this.alias = alias;
		data = new DataRowValuesMap(form);
	}

	public Map<String, Object> getData() {
		return data;
	}

	public Object getColumnValue(String columnAlias) {
		return data.get(columnAlias);
	}

	public void setColumnValue(String columnAlias, Object value) {
		if (value == null) {
			data.remove(columnAlias);
		} else {
			data.put(columnAlias, value);
		}
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String code) {
		this.alias = code;
	}
}

package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Строка данных отчётной формы
 * Для упрощения скриптинга, класс реализует интерфейс Map<String, Object>, чтобы в скриптах можно было писать
 * конструкции вида <code>row.property = anotherRow.property</code>
 * Можно считать что строка данных - это Map, в котором ключи - {@link Column#getAlias алиасы столбцов}, а значение -
 * значения содержащиеся в соответствующих столбцах.
 * 
 * Обращаю внимание, что часть методов интерфейса не реализована, при их вызове будет возникать UnsupportedOperationException.
 *
 * Фактически класс является обёрткой над обычным HashMap, но при этом содержит ряд дополнительных атрибутов,
 * содержащих информацию о строке в отчётной форме, а также операции по работе с Map реализованы таким образом,
 * чтобы предотвратить заполнение строки данными неверного типа. При использовании метода put проводится проверка данных
 * на соответствие типу соответствующего столбца, поддерживаются строки, даты, BigDecimal, кроме того метод put не позволяет 
 * добавлять в строку данные по столбцам, которые отсутствуют в определении формы
 * Для некоторых числовых типов реализовано автоматическое приведение к BigDecimal.
 *
 * Для облегчения идентификации нужной строки среди строк данных по форме, можно использовать строковые алиасы. Их стоит использовать
 * для строк, несущих особую смысловую нагрузку (например "Итого" и т.п.).
 * 
 * У объекта данного класса обязательно должно быть проинициализировано значение поля form. Этого можно достичь либо 
 * создав объект вызовом конструктора, принимающего объект {@link Form}, либо использовать конструктор по-умолчанию и после этого
 * вызвать метод {@linkplain #setForm}.
 */
public class DataRow implements Map<String, Object>, Ordered, Serializable {
	private static final long serialVersionUID = 1L;
	private Form form;
	private Map<String, Object> data;
	private String alias;
	private int order;

	public DataRow() {
		
	}
	
	public DataRow(String alias, Form form) {
		this(form);
		this.alias = alias;
	}
	
	public DataRow(Form form) {
		this.form = form;
		List<Column> columns = form.getColumns();
		data = new HashMap<String, Object>(columns.size());
		for (Column col: columns) {
			data.put(col.getAlias(), null);
		}
	}

	public void setForm(Form form) {
		this.form = form;
	}
	
	public String getAlias() {
		return alias;
	}

	public void setAlias(String code) {
		this.alias = code;
	}
	
	/**
	 * Методы, реализующие интефрейс Map<String, Object>
	 */
	public void clear() {
		data.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return data.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return data.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return data.entrySet();
	}

	@Override
	public Object get(Object key) {
		// Проверяем, что такой столбец есть, если нет, то получим IllegalArgumentException
		form.getColumn((String)key);
		return data.get(key);
	}

	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		return data.keySet();
	}

	@Override
	public Object put(String key, Object value) {
		// Если столбец не удастся найти, то получим исключение
		// Это нормально - пользователь поймёт, что в скрипте ошибка
		Column col = form.getColumn(key);
		
		if (value instanceof Integer) {
			value = new BigDecimal((Integer)value);
		} else if (value instanceof Double) {
			value = new BigDecimal((Double)value);
		} else if (value instanceof Long) {
			value = new BigDecimal((Long)value);
		}
		
		if (col instanceof NumericColumn && value != null) {
			int precision = ((NumericColumn) col).getPrecision();
			value = ((BigDecimal)value).setScale(precision, RoundingMode.HALF_UP); 
		}
		
		if (value == null || value instanceof BigDecimal || value instanceof String || value instanceof Date) {
			return data.put(key, value);				
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
		return data.remove(key);
	}

	@Override
	public int size() {
		return data.size();
	}

	@Override
	public Collection<Object> values() {
		return data.values();
	}

	@Override
	public int getOrder() {
		return order;
	}

	@Override
	public void setOrder(int order) {
		this.order = order;
	}
}

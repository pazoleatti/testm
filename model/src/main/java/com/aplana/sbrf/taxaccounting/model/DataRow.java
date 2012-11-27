package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Строка данных отчётной формы
 * Для упрощения скриптинга, класс реализует интерфейс Map<String, Object>, чтобы в скриптах можно было писать
 * конструкции вида <code>row.property = anotherRow.property</code>
 * Можно считать что строка данных - это Map, в котором ключи - {@link Column#getAlias алиасы столбцов}, а значение -
 * значения содержащиеся в соответствующих столбцах.
 * <p/>
 * Обращаю внимание, что часть методов интерфейса не реализована, при их вызове будет возникать UnsupportedOperationException.
 * <p/>
 * Фактически класс является обёрткой над обычным HashMap, но при этом содержит ряд дополнительных атрибутов,
 * содержащих информацию о строке в отчётной форме, а также операции по работе с Map реализованы таким образом,
 * чтобы предотвратить заполнение строки данными неверного типа. При использовании метода put проводится проверка данных
 * на соответствие типу соответствующего столбца, поддерживаются строки, даты, BigDecimal, кроме того метод put не позволяет
 * добавлять в строку данные по столбцам, которые отсутствуют в определении формы
 * Для некоторых числовых типов реализовано автоматическое приведение к BigDecimal.
 * <p/>
 * Для облегчения идентификации нужной строки среди строк данных по форме, можно использовать строковые алиасы. Их стоит использовать
 * для строк, несущих особую смысловую нагрузку (например "Итого" и т.п.).
 * <p/>
 * Данный объект обязательно должен обладать сведениями о столбцах формы, к которой принадлежит строка.Этого можно достичь либо
 * создав объект вызовом конструктора, принимающего список {@link Column столбцов}, либо использовать конструктор по-умолчанию и после этого
 * вызвать метод {@linkplain #setFormColumns}.
 */
public class DataRow implements Map<String, Object>, Ordered, Serializable {
	private static final long serialVersionUID = 1L;
	private Map<String, CellValue> data;
	private String alias;
	private int order;

	public DataRow() {

	}

	static class MapEntry implements Map.Entry<String, Object> {

		private Map.Entry<String, CellValue> sourceEntry;

		private MapEntry(Map.Entry<String, CellValue> sourceEntry) {
			this.sourceEntry = sourceEntry;
		}

		@Override
		public String getKey() {
			return sourceEntry.getKey();
		}

		@Override
		public Object getValue() {
			CellValue value = sourceEntry.getValue();
			return value == null ? null : value.getValue();
		}

		@Override
		public Object setValue(Object value) {
			throw new UnsupportedOperationException("not implemented yet");
		}

	}

	public DataRow(String alias, List<Column> formColumns) {
		this(formColumns);
		this.alias = alias;
	}

	public DataRow(List<Column> formColumns) {
		setFormColumns(formColumns);
	}

	/**
	 * Задать информацию о столбцах, допустимых в строке
	 * Внимание: использование данного метода сбрасывает значение всех столбцов в строке в null
	 *
	 * @param formColumns список столбцов
	 */
	public void setFormColumns(List<Column> formColumns) {
		data = new HashMap<String, CellValue>(formColumns.size());
		for (Column col : formColumns) {
			CellValue cellValue = new CellValue();
			cellValue.setColumn(col);
			data.put(col.getAlias(), cellValue);
		}
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
	public Set<Map.Entry<String, Object>> entrySet() {
		Set<Map.Entry<String, Object>> entries = new HashSet<Map.Entry<String, Object>>();
		if (data != null) {
			for (Map.Entry<String, CellValue> entry : data.entrySet()) {
				entries.add(new MapEntry(entry));
			}
		}
		return entries;
	}

	private CellValue getCellValue(String columnAlias) {
		CellValue cellValue = data.get(columnAlias);
		if (cellValue == null) {
			throw new IllegalArgumentException("Wrong column alias: " + columnAlias);
		}
		return cellValue;
	}

	@Override
	public Object get(Object key) {
		CellValue cellValue = getCellValue((String) key);
		return cellValue.getValue();
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
		CellValue cellValue = getCellValue((String) key);
		return cellValue.setValue(value);
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
		List<Object> values = new ArrayList<Object>(data.size());
		for (Map.Entry<String, CellValue> entry : data.entrySet()) {
			values.add(entry.getValue().getValue());
		}
		return values;
	}

	@Override
	public int getOrder() {
		return order;
	}

	@Override
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		DataRow dataRow = (DataRow) o;

		if (order != dataRow.order) {
			return false;
		}
		if (alias != null ? !alias.equals(dataRow.alias) : dataRow.alias != null) {
			return false;
		}
		if (data != null ? !data.equals(dataRow.data) : dataRow.data != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = alias != null ? alias.hashCode() : 0;
		result = 31 * result + order;
		return result;
	}

	@Override
	public String toString() {
		return "DataRow{" +
				"data=" + data +
				", alias='" + alias + '\'' +
				", order=" + order +
				'}';
	}
}

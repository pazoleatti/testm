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
 * Строка данных отчётной формы Для упрощения скриптинга, класс реализует
 * интерфейс Map<String, Object>, чтобы в скриптах можно было писать конструкции
 * вида <code>row.property = anotherRow.property</code> Можно считать что строка
 * данных - это Map, в котором ключи - {@link Column#getAlias алиасы столбцов},
 * а значение - значения содержащиеся в соответствующих столбцах.
 * <p/>
 * Обращаю внимание, что часть методов интерфейса не реализована, при их вызове
 * будет возникать UnsupportedOperationException.
 * <p/>
 * Фактически класс является обёрткой над обычным HashMap, но при этом содержит
 * ряд дополнительных атрибутов, содержащих информацию о строке в отчётной
 * форме, а также операции по работе с Map реализованы таким образом, чтобы
 * предотвратить заполнение строки данными неверного типа. При использовании
 * метода put проводится проверка данных на соответствие типу соответствующего
 * столбца, поддерживаются строки, даты, BigDecimal, кроме того метод put не
 * позволяет добавлять в строку данные по столбцам, которые отсутствуют в
 * определении формы Для некоторых числовых типов реализовано автоматическое
 * приведение к BigDecimal.
 * <p/>
 * Для облегчения идентификации нужной строки среди строк данных по форме, можно
 * использовать строковые алиасы. Их стоит использовать для строк, несущих
 * особую смысловую нагрузку (например "Итого" и т.п.).
 * <p/>
 * Данный объект обязательно должен обладать сведениями о столбцах формы, к
 * которой принадлежит строка.Этого можно достичь либо создав объект вызовом
 * конструктора, принимающего список {@link Column столбцов}, либо использовать
 * конструктор по-умолчанию и после этого вызвать метод
 * {@linkplain #setFormColumns}.
 */
public class DataRow implements Map<String, Object>, Ordered, Serializable {
	private static final long serialVersionUID = 1L;
	private Map<String, Cell> data;
	private String alias;
	private List<FormStyle> formStyleList;
	private int order;

	/**
	 * Конструктор нужен для сериализации
	 */
	public DataRow() {

	}

	static class MapEntry implements Map.Entry<String, Object> {

		private Map.Entry<String, Cell> sourceEntry;

		private MapEntry(Map.Entry<String, Cell> sourceEntry) {
			this.sourceEntry = sourceEntry;
		}

		@Override
		public String getKey() {
			return sourceEntry.getKey();
		}

		@Override
		public Object getValue() {
			Cell value = sourceEntry.getValue();
			return value == null ? null : value.getValue();
		}

		@Override
		public Object setValue(Object value) {
			throw new UnsupportedOperationException("not implemented yet");
		}

	}

	public DataRow(String alias, List<Column> formColumns, List<FormStyle> formStyleList) {
		this(formColumns, formStyleList);
		this.alias = alias;
		
	}

	public DataRow(List<Column> formColumns, List<FormStyle> formStyleList) {
		this.formStyleList = formStyleList;
		setFormColumns(formColumns);
	}

	/**
	 * Задать информацию о столбцах, допустимых в строке Внимание: использование
	 * данного метода сбрасывает значение всех столбцов в строке в null
	 * 
	 * @param formColumns
	 *            список столбцов
	 */
	public void setFormColumns(List<Column> formColumns) {
		data = new HashMap<String, Cell>(formColumns.size());
		for (Column col : formColumns) {
			addColumn(col);
		}
	}

	/**
	 * Исправляет ошибки связанные с изменением данных столбца.
	 */
	void fixAliases() {
		Map<String, Cell> fixedData = new HashMap<String, Cell>();
		for (Map.Entry<String, Cell> entry : data.entrySet()) {
			Cell cellValue = entry.getValue();
			fixedData.put(cellValue.getColumn().getAlias(), cellValue);
		}
		if (data.size() != fixedData.size()) {
			throw new IllegalStateException("Существувуют дубликаты алиасов");
		}
		data = fixedData;
	}

	/**
	 * Добавить столбец в существующую мапу Этот метод нужен для админки
	 * 
	 * @param col
	 *            столбец
	 */
	public void addColumn(Column col) {

		Cell oldValue = data.get(col.getAlias());
		if (oldValue == null) {
			Cell cellValue = new Cell(col, formStyleList);
			// --------------------------------------------
			// TODO: удалить, когда будет сделана установка флага для каждой ячейки на основе таблицы CELL_EDITABLE
			cellValue.setEditable(col.isEditable());
			// --------------------------------------------
			data.put(col.getAlias(), cellValue);
		} else {
			throw new IllegalArgumentException("Алиас столбца + '"
					+ col.getAlias() + "' уже существует в шаблоне");
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
			for (Map.Entry<String, Cell> entry : data.entrySet()) {
				entries.add(new MapEntry(entry));
			}
		}
		return entries;
	}

	public Cell getCell(String columnAlias) {
		Cell cell = data.get(columnAlias);
		if (cell == null) {
			throw new IllegalArgumentException("Wrong column alias: "
					+ columnAlias);
		}
		return cell;
	}

	@Override
	public Object get(Object key) {
		Cell cellValue = getCell((String) key);
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
		Cell cellValue = getCell((String) key);
		return cellValue.setValue(value);
	}

	@Override
	public void putAll(Map<? extends String, ?> map) {
		for (Map.Entry<? extends String, ?> entry : map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
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
		for (Map.Entry<String, Cell> entry : data.entrySet()) {
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
	public String toString() {
		return "DataRow{" + "data=" + data + ", alias='" + alias + '\''
				+ ", order=" + order + '}';
	}
}

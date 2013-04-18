package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
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
 * Фактически класс является Map, в основе которой лежит List<Cell>, 
 * но при этом содержит ряд дополнительных атрибутов, содержащих информацию о 
 * строке в отчётной форме, а также операции по работе с Map реализованы таким 
 * образом, чтобы предотвратить заполнение строки данными неверного типа.
 * При использовании метода put проводится проверка данных на соответствие типу 
 * соответствующего столбца, поддерживаются строки, даты, BigDecimal, кроме того
 * метод put непозволяет добавлять в строку данные по столбцам, которые 
 * отсутствуют в определении формы. Для некоторых (часто используемых) числовых
 * типов реализовано автоматическое приведение к BigDecimal.
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
	private List<Cell> data;
	private String alias;
	private List<FormStyle> formStyleList;
	private int order;

	/**
	 * Конструктор нужен для сериализации
	 */
	public DataRow() {

	}

	public static final class MapEntry implements Map.Entry<String, Object> {

		private Cell cell;

		private MapEntry(Cell cell) {
			this.cell = cell;
		}
		
		public Cell getCell(){
			return cell;
		}

		@Override
		public String getKey() {
			return cell.getColumn().getAlias();
		}

		@Override
		public Object getValue() {
			return cell.getValue();
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
	public final void setFormColumns(List<Column> formColumns) {
		data = new ArrayList<Cell>(formColumns.size());
		for (Column col : formColumns) {
			addColumn(col);
		}
	}

	/**
	 * Добавить столбец в существующую мапу Этот метод нужен для админки
	 * @param position позиция вставки
	 * @param col столбец
	 */
	public void addColumn(int position, Column col) {
		Cell oldValue = getCell(col.getAlias(), false);
		if (oldValue == null) {
			Cell cellValue = new Cell(col, formStyleList);
			data.add(position, cellValue);
		} else {
			throw new IllegalArgumentException("Алиас столбца + '"
					+ col.getAlias() + "' уже существует в шаблоне");
		}
	}

	/**
	 * Добавить столбец в существующую мапу Этот метод нужен для админки
	 * @param col столбец
	 */
	public void addColumn(Column col) {
		Cell oldValue = getCell(col.getAlias(), false);
		if (oldValue == null) {
			Cell cellValue = new Cell(col, formStyleList);
			data.add(cellValue);
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
	@Override
	public void clear() {
		data.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return getCell((String)key) != null;
	}

	@Override
	public boolean containsValue(Object value) {
		for (Cell cell: data) {
			if (cell.getValue() == null && value == null) {
				return true;
			} else if (cell.getValue() != null && cell.getValue().equals(value)) {
				return true;
			}
		}	
		return false;
	}

	@Override
	public Set<Map.Entry<String, Object>> entrySet() {
		Set<Map.Entry<String, Object>> entries = new LinkedHashSet<Map.Entry<String, Object>>();
		if (data != null) {
			for (Cell cell: data) {
				entries.add(new MapEntry(cell));
			}
		}
		return Collections.unmodifiableSet(entries);
	}

	public Cell getCell(String columnAlias) {
		return getCell(columnAlias, true);
	}
	
	private Cell getCell(String columnAlias, boolean throwIfNotFound) {
		for (Cell cell: data) {
			if (cell.getColumn().getAlias().equals(columnAlias)) {
				return cell;
			}
		}
		if (throwIfNotFound) {
			throw new IllegalArgumentException("Wrong column alias: " + columnAlias);		
		} else {
			return null;
		}
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
		Set<String> keySet = new LinkedHashSet<String>(data.size());
		for (Cell cell: data) {
			keySet.add(cell.getColumn().getAlias());
		}
		return Collections.unmodifiableSet(keySet);
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

	public void removeColumn(Column column) {
		Cell cell = getCell(column.getAlias(), false);
		if (cell != null) {
			data.remove(cell);
		}
	}

	@Override
	public int size() {
		return data.size();
	}

	@Override
	public Collection<Object> values() {
		List<Object> values = new ArrayList<Object>(data.size());
		for (Cell cell : data) {
			values.add(cell.getValue());
		}
		return Collections.unmodifiableList(values);
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

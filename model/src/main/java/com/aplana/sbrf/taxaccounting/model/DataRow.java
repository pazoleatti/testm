package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.formdata.AbstractCell;

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
public class DataRow<C extends AbstractCell> extends IdentityObject<Long> implements Map<String, Object>, Serializable {
	private static final long serialVersionUID = 1L;
	private List<C> data;
	private String alias;
	private Integer index;

	/**
	 * Конструктор нужен для сериализации
	 */
	public DataRow() {

	}

	public static final class MapEntry<C extends AbstractCell> implements Map.Entry<String, Object> {

		private C cell;

		private MapEntry(C cell) {
			this.cell = cell;
		}
		
		public C getCell(){
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

	public DataRow(String alias, List<C> cells) {
		this(cells);
		this.alias = alias;
		
	}

	public DataRow(List<C> cells) {
		setFormColumns(cells);
	}

	/**
	 * Задать информацию о столбцах, допустимых в строке Внимание: использование
	 * данного метода сбрасывает значение всех столбцов в строке в null
	 * 
	 * @param formColumns
	 *            список столбцов
	 */
	public final void setFormColumns(List<C> cells) {
		data = new ArrayList<C>(cells.size());
		for (C col : cells) {
			addColumn(col);
		}
	}

	/**
	 * Добавить столбец в существующую мапу Этот метод нужен для админки
	 * @param position позиция вставки
	 * @param col столбец
	 */
	public void addColumn(int position, C col) {
		C oldValue = getCell(col.getColumn().getAlias(), false);
		if (oldValue == null) {
			 C cellValue = col;
			data.add(position, cellValue);
		} else {
			throw new IllegalArgumentException("Алиас столбца + '"
					+ col.getColumn().getAlias() + "' уже существует в шаблоне");
		}
	}

	/**
	 * Добавить столбец в существующую мапу Этот метод нужен для админки
	 * @param col столбец
	 */
	public void addColumn( C col) {
		 C oldValue = getCell(col.getColumn().getAlias(), false);
		if (oldValue == null) {
			 C cellValue = col;
			data.add(cellValue);
		} else {
			throw new IllegalArgumentException("Алиас столбца + '"
					+ col.getColumn().getAlias() + "' уже существует в шаблоне");
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
		for ( C cell: data) {
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
			for ( C cell: data) {
				entries.add(new MapEntry<C>(cell));
			}
		}
		return Collections.unmodifiableSet(entries);
	}

	public C getCell(String columnAlias) {
		return getCell(columnAlias, true);
	}
	
	private C getCell(String columnAlias, boolean throwIfNotFound) {
		for (C cell: data) {
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
		C cellValue = getCell((String) key);
		return cellValue.getValue();
	}

	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		Set<String> keySet = new LinkedHashSet<String>(data.size());
		for (C cell: data) {
			keySet.add(cell.getColumn().getAlias());
		}
		return Collections.unmodifiableSet(keySet);
	}

	@Override
	public Object put(String key, Object value) {
		C cellValue = getCell((String) key);
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
		C cell = getCell(column.getAlias(), false);
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
		for (C cell : data) {
			values.add(cell.getValue());
		}
		return Collections.unmodifiableList(values);
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	@Override
	public String toString() {
		return "DataRow [data=" + data + ", alias=" + alias + ", index="
				+ index + "]";
	}


}

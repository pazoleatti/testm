package com.aplana.sbrf.taxaccounting.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Описание налоговой формы
 * @author dsultanbekov
 */
public class Form {
	private Integer id;
	private final List<Column> columns = new ArrayList<Column>();
	private FormType type;
	private String version;	
	private boolean predefinedRows;
	
	public Integer getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public List<Column> getColumns() {
		return columns;
	}
	public void setType(FormType type) {
		this.type = type;
	}
	public FormType getType() {
		return type;
	}
	/**
	 * Получить определение столбца по числовому идентификатору
	 * @param columnId идентификатор столбца
	 * @return определение столбца
	 * @throws IllegalArgumentException если в определении формы отсутствует столбец с указанным <code>id</code>
	 */
	public Column getColumn(int columnId) {
		for (Column col: columns) {
			if (col.getId() == columnId) {
				return col;
			}
		}
		throw new IllegalArgumentException("Wrong columnId: " + columnId);
	}
	
	/**
	 * Получить определение столбца налоговой формы по алиасу
	 * @param columnAlias
	 * @return определение столбца
	 * @throws NullPointerException если <code>alias == null</code>
	 * @throws IllegalArgumentException если указан алиас, отсутствующий в определении формы
	 */
	public Column getColumn(String columnAlias) {
		if (columnAlias == null) {
			throw new NullPointerException("Column alias cannot be null");
		}
		for (Column col: columns) {
			if (columnAlias.equals(col.getAlias())) {
				return col;
			}
		}
		throw new IllegalArgumentException("Wrong columnAlias: " + columnAlias);
	}
	
	/**
	 * Значение признака, определяющего, используется ли в форме предопределённый набор строк, или же
	 * строки могут добавляться пользователем
	 * @return true - если набор строк предопределён, false - в противном случае
	 */
	public boolean isPredefinedRows() {
		return predefinedRows;
	}
	
	/**
	 * Установить значение флага об использовании предопределённого набора строк
	 * @param predefinedRows желаемое значение флага
	 */
	public void setPredefinedRows(boolean predefinedRows) {
		this.predefinedRows = predefinedRows;
	}
	
	/**
	 * Получить версию формы: для каждого типа формы может существовать несколько версий
	 * @return версия формы
	 */
	public String getVersion() {
		return version;
	}
	
	/**
	 * Установить версию для формы
	 * @param version номер версии
	 */
	public void setVersion(String version) {
		this.version = version;
	}
}

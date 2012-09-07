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
	private Script calcScript;
	private Script createScript;
	private final List<RowScript> rowScripts = new ArrayList<RowScript>();
	private final List<DataRow> predefinedRows = new ArrayList<DataRow>();
	
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
	public Script getCalcScript() {
		return calcScript;
	}
	public void setCalcScript(Script calcScript) {
		this.calcScript = calcScript;
	}
	public Script getCreateScript() {
		return createScript;
	}
	public void setCreateScript(Script createScript) {
		this.createScript = createScript;
	}
	public List<RowScript> getRowScripts() {
		return rowScripts;
	}
	public List<DataRow> getPredefinedRows() {
		return predefinedRows;
	}
}

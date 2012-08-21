package com.aplana.sbrf.taxaccounting.model;

/**
 * Столбец таблицы в объявлении налоговой формы
 * @author dsultanbekov
 */
public class Column {
	public static enum Types {
		NUMBER('N'),
		STRING('S'),
		DATE('D');
		private char code;
		Types(char code) {
			this.code = code;
		}
		public char getCode() {
			return code;
		}
		public static Types getType(char code) {
			for(Types t: Types.values()) {
				if (code == t.getCode()) {
					return t;
				}
			}
			throw new IllegalArgumentException("Unknown column type: " + code);
		}
	}
	private Integer id;
	private String name;
	private int formId;
	private int order;
	private Types type;
	private String alias;
	
	public Integer getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getFormId() {
		return formId;
	}
	public void setFormId(int formId) {
		this.formId = formId;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public Types getType() {
		return type;
	}
	public void setType(Types type) {
		this.type = type;
	}
}

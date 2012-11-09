package com.aplana.sbrf.taxaccounting.model.security;

import java.io.Serializable;

public class TARole implements Serializable {
	/**
	 * Алиас роли "Контролёр"
	 */
	public static final String ROLE_CONTROL = "ROLE_CONTROL";
	/**
	 * Алиас роли "Оператор"
	 */
	public static final String ROLE_OPERATOR = "ROLE_OPERATOR";
	
	
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String alias;
	private String name;
	
	public int getId() {
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
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	@Override
	public boolean equals(Object obj) {
		TARole role = (TARole)obj;
		return role.id == id;
	}
	@Override
	public int hashCode() {
		return id;
	}
}

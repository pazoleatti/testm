package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

public class TARole implements Serializable {
	/**
	 * Алиас роли "Контролёр"
	 */
	public static final String ROLE_CONTROL = "ROLE_CONTROL";
	/**
	 * Алиас роли "Оператор"
	 */
	public static final String ROLE_OPER = "ROLE_OPER";
	/**
	 * Алиас роли "Контролёр УНП"
	 */
	public static final String ROLE_CONTROL_UNP = "ROLE_CONTROL_UNP";
    /**
     * Алиас роли "Контролёр НС"
     */
    public static final String ROLE_CONTROL_NS = "ROLE_CONTROL_NS";
	/**
	 * Алиас роли "Настройщик"
	 */
	public static final String ROLE_CONF = "ROLE_CONF";
	/**
	 * Алиас роли "Администратор"
	 */
	public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /**
     * Алиас роли "Пользователь модуля Гарантий"
     */
    public static final String ROLE_GARANT = "ROLE_GARANT";

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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TARole taRole = (TARole) o;

		if (id != taRole.id) return false;
		if (!alias.equals(taRole.alias)) return false;
		if (!name.equals(taRole.name)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return 0;
	}

    @Override
    public String toString() {
        return "TARole{" +
                "id: " + id +
                ",alias: " + alias +
                ",name: " + name +
                '}';
    }
}

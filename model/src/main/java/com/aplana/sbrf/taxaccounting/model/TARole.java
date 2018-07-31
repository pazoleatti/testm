package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

public class TARole implements Serializable {
    /**
     * Алиас роли "Оператор (НДФЛ)"
     */
    public static final String N_ROLE_OPER = "N_ROLE_OPER";
    /**
     * Алиас роли "Все АСНУ (НДФЛ)"
     */
    public static final String N_ROLE_OPER_ALL = "N_ROLE_OPER_ALL";
    /**
     * Алиас роли "Контролёр УНП (НДФЛ)"
     */
    public static final String N_ROLE_CONTROL_UNP = "N_ROLE_CONTROL_UNP";
    /**
     * Алиас роли "Контролёр НС (НДФЛ)"
     */
    public static final String N_ROLE_CONTROL_NS = "N_ROLE_CONTROL_NS";
    /**
     * Алиас роли "Настройщик (НДФЛ)"
     */
    public static final String N_ROLE_CONF = "N_ROLE_CONF";
    /**
     * Алиас роли "Администратор"
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

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
		return alias.hashCode();
	}

    @Override
    public String toString() {
        return "TARole{" +
                "id: " + id +
                ", alias: " + alias +
                ", name: " + name + '}';
    }
}

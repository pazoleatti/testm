package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

public class TARole implements Serializable {
    /**
     * Алиас роли "Оператор (НДФЛ)"
     */
    public static final String N_ROLE_OPER = "N_ROLE_OPER";
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
     * Алиас роли "Администратор (НДФЛ)"
     */
    public static final String N_ROLE_ADMIN = "N_ROLE_ADMIN";

    /**
     * Алиас роли "Оператор (Сборы)"
     */
    public static final String F_ROLE_OPER = "F_ROLE_OPER";
    /**
     * Алиас роли "Контролёр УНП (Сборы)"
     */
    public static final String F_ROLE_CONTROL_UNP = "F_ROLE_CONTROL_UNP";
    /**
     * Алиас роли "Контролёр НС (Сборы)"
     */
    public static final String F_ROLE_CONTROL_NS = "F_ROLE_CONTROL_NS";
    /**
     * Алиас роли "Настройщик (Сборы)"
     */
    public static final String F_ROLE_CONF = "F_ROLE_CONF";

	private static final long serialVersionUID = 1L;
	
	private int id;
	private String alias;
	private String name;
    private TaxType taxType;

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

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    @Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TARole taRole = (TARole) o;

		if (id != taRole.id) return false;
		if (!alias.equals(taRole.alias)) return false;
		if (!name.equals(taRole.name)) return false;
        if (!taxType.equals(taRole.taxType)) return false;

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
                ", alias: " + alias +
                ", name: " + name +
                (taxType != null ? (", taxType: " + taxType.getCode()) : "") +
                '}';
    }
}

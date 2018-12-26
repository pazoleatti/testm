package com.aplana.sbrf.taxaccounting.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
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
	 * Алиас роли "Оператор формирования Уведомлений о неудержанном налоге (НДФЛ)"
	 */
	public static final String N_ROLE_OPER_NOTICE = "N_ROLE_OPER_NOTICE";
	/**
	 * Алиас роли "Редактор Реестра физических лиц (НДФЛ)"
	 */
	public static final String N_ROLE_EDITOR_FL = "N_ROLE_EDITOR_FL";
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
}

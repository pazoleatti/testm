package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Типы подразделений банка 
 */
public enum DepartmentType implements Serializable {
	ROOT_BANK("Банк", 1),
	TERR_BANK("Территориальный банк", 2),
	CSKO_PCP("ЦСКО, ПЦП", 3),
    MANAGEMENT("Управление", 4),
    INTERNAL("", 5);

	private static final long serialVersionUID = 1L;	

	private final String label;
	private final int code;

	private DepartmentType(String label, int code) {
		this.label = label;
		this.code = code;
	}

	public static DepartmentType fromCode(int code) {
		for (DepartmentType t: values()) {
			if (t.code == code) {
				return t;
			}
		}
		throw new IllegalArgumentException("Unknown department type: " + code);
	}

	/**
	 * Описание типа подразделения
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Числовое представление типа, используется для хранения в БД
	 */
	public int getCode() {
		return code;
	}		
}

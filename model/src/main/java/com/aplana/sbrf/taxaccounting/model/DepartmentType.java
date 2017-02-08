package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;

import java.io.Serializable;

/**
 * Типы подразделений банка 
 */
public enum DepartmentType implements Serializable {
	/** 1 Банк - ROOT_BANK */
	ROOT_BANK("Банк", 1),
	/** 2 Территориальный банк - TERR_BANK*/
	TERR_BANK("Территориальный банк", 2),
	/** 3 ЦСКО, ПЦП - CSKO_PCP*/
	CSKO_PCP("ЦСКО, ПЦП", 3),
	/** 4 Управление - MANAGEMENT*/
    MANAGEMENT("Управление", 4),
	/** 5 INTERNAL - не передается в СУДИР */
    INTERNAL("", 5);

	private static final long serialVersionUID = 1L;	

	private final String label;
	private final int code;

	private DepartmentType(String label, int code) {
		this.label = label;
		this.code = code;
	}

	public static DepartmentType fromCode(int code) {
		if (RefBook.FAKE_ID == code) {
			return null;
		}
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

package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;


public class DictionaryTaxPeriod implements Serializable{

	private int code;
	private String name;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMonths() {
		switch (code) {
			case 21:
			case 22:
			case 23:
			case 24:
			case 51:
			case 54:
			case 55:
			case 56:
				return 3;
			case 31:
			case 52:
				return 6;
			case 33:
			case 53:
				return 9;
			case 34:
			case 50:
				return 12;

			default:
				return 0;
		}
	}
}

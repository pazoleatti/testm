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
}

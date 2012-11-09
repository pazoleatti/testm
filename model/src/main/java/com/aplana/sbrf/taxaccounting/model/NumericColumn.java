package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;


public class NumericColumn extends Column implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int precision = 0;

	public int getPrecision() {
		return precision;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}
}

package com.aplana.sbrf.taxaccounting.model;

import java.math.BigDecimal;

public class NumericColumn extends Column<BigDecimal> {
	private int precision;

	public int getPrecision() {
		return precision;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}
}

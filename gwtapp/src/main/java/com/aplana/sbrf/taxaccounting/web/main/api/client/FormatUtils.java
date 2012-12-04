package com.aplana.sbrf.taxaccounting.web.main.api.client;

import com.google.gwt.i18n.client.NumberFormat;

import java.math.BigDecimal;

/**
 * @author Vitalii Samolovskikh
 */
public final class FormatUtils {
	private FormatUtils() {
	}

	public static NumberFormat getSimpleNumberFormat(){
		return NumberFormat.getFormat("0.################");
	}
}

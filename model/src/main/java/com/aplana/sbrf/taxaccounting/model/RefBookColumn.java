package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Реализация {@link Column}, предназначенная для хранения значений
 * справочников.
 * 
 * @author sgoryachkin
 */
public class RefBookColumn extends Column implements Serializable {
	private static final long serialVersionUID = 1L;

	private long refBookAttributeId;

	private static Formatter formatter;
	
	public long getRefBookAttributeId() {
		return refBookAttributeId;
	}

	public void setRefBookAttributeId(long refBookAttributeId) {
		this.refBookAttributeId = refBookAttributeId;
	}

	@Override
	public Formatter getFormatter() {
		return formatter != null ? formatter : new Formatter() {
			@Override
			public String format(String valueToFormat) {
				return String.valueOf(valueToFormat);
			}
		};
	}

	@Override
	public ValidationStrategy getValidationStrategy() {
		return new ValidationStrategy() {
			@Override
			public boolean matches(String valueToCheck) {
				return true;
			}
		};
	}


}

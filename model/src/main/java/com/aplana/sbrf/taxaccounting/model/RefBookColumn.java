package com.aplana.sbrf.taxaccounting.model;


/**
 * Реализация {@link Column}, предназначенная для хранения значений
 * справочников.
 * 
 * @author sgoryachkin
 */
public class RefBookColumn extends Column {
	private static final long serialVersionUID = -6969365681036598158L;

	private long refBookAttributeId;

	private String filter;

	private static Formatter formatter = new Formatter() {
		@Override
		public String format(String valueToFormat) {
			return String.valueOf(valueToFormat);
		}
	};

	private static ValidationStrategy validationStrategy = new ValidationStrategy() {
		@Override
		public boolean matches(String valueToCheck) {
			return true;
		}
	};

	public long getRefBookAttributeId() {
		return refBookAttributeId;
	}

	public void setRefBookAttributeId(long refBookAttributeId) {
		this.refBookAttributeId = refBookAttributeId;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	@Override
	public Formatter getFormatter() {
		return formatter;
	}

	@Override
	public ValidationStrategy getValidationStrategy() {
		return validationStrategy;
	}

}

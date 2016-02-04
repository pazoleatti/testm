package com.aplana.sbrf.taxaccounting.model;


/**
 * Реализация {@link Column}, предназначенная для хранения значений
 * справочников.
 * 
 * @author sgoryachkin
 */
public class RefBookColumn extends Column {
	private static final long serialVersionUID = -6969365681036598158L;

	private Long refBookAttributeId;

    private Long refBookAttributeId2;

    private long nameAttributeId;

	private String filter;

    private boolean searchEnabled;

    private boolean isHierarchical = false;

	private static ValidationStrategy validationStrategy = new ValidationStrategy() {
		@Override
		public boolean matches(String valueToCheck) {
			return true;
		}
	};

    public RefBookColumn() {
		setColumnType(ColumnType.REFBOOK);
        searchEnabled = true;
    }

	public Long getRefBookAttributeId() {
		return refBookAttributeId;
	}

	public void setRefBookAttributeId(Long refBookAttributeId) {
		this.refBookAttributeId = refBookAttributeId;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

    public boolean isHierarchical() {
        return isHierarchical;
    }

    public void setHierarchical(boolean isHierarchical) {
        this.isHierarchical = isHierarchical;
    }

    public long getNameAttributeId() {
        return nameAttributeId;
    }

    public void setNameAttributeId(long nameAttributeId) {
        this.nameAttributeId = nameAttributeId;
    }

	@Override
	public Formatter getFormatter() {
		return new Formatter() {
			@Override
			public String format(String valueToFormat) {
				try {
					Long val = Long.parseLong(valueToFormat);
					String plainString = val.toString();

					int length = plainString.length();
					StringBuilder stringBuilder = new StringBuilder(plainString.substring(0, length));
					for (int i = 3; i < length; i += 3) {
						if (i < length) {
							stringBuilder.insert(length - i, " ");
						}
					}
					return stringBuilder.toString();
				} catch (NumberFormatException e) {
					return String.valueOf(valueToFormat);
				}

			}
		};
	}

	@Override
	public ValidationStrategy getValidationStrategy() {
		return validationStrategy;
	}


    public Long getRefBookAttributeId2() {
        return refBookAttributeId2;
    }

    public void setRefBookAttributeId2(Long refBookAttributeId2) {
        this.refBookAttributeId2 = refBookAttributeId2;
    }

    public boolean isSearchEnabled() {
        return searchEnabled;
    }

    public void setSearchEnabled(boolean isSearchEnabled) {
        this.searchEnabled = isSearchEnabled;
    }
}

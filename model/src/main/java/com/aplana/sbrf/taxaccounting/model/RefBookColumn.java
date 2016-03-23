package com.aplana.sbrf.taxaccounting.model;


import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;

/**
 * Реализация {@link Column}, предназначенная для хранения значений
 * справочников.
 * 
 * @author sgoryachkin
 */
public class RefBookColumn extends FilterColumn {
	private static final long serialVersionUID = -6969365681036598158L;

	private Long refBookAttributeId;

    private Long refBookAttributeId2;

    private long nameAttributeId;

    private boolean searchEnabled;

    private boolean isHierarchical = false;

    private RefBookAttribute refBookAttribute;

    transient private ColumnFormatter formatter;

    private static ValidationStrategy validationStrategy = new ValidationStrategy() {
		@Override
		public boolean matches(String valueToCheck) {
			return true;
		}
	};

    public RefBookColumn() {
		columnType = ColumnType.REFBOOK;
        searchEnabled = true;
    }

	public Long getRefBookAttributeId() {
		return refBookAttributeId;
	}

	public void setRefBookAttributeId1(Long refBookAttributeId) {
		this.refBookAttributeId = refBookAttributeId;
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
	public ColumnFormatter getFormatter() {
        if (formatter != null) return formatter;
        if (refBookAttribute != null && refBookAttribute.getAttributeType() != null
                && refBookAttribute.getAttributeType().equals(RefBookAttributeType.NUMBER)) {
            return formatter = new NumericColumnFormatter(refBookAttribute.getPrecision(), refBookAttribute.getMaxLength());
        } else {
            return formatter = new ColumnFormatter();
        }
	}

	@Override
	public ValidationStrategy getValidationStrategy() {
		return validationStrategy;
	}

    public RefBookAttribute getRefBookAttribute() {
        return refBookAttribute;
    }

    public void setRefBookAttribute(RefBookAttribute refBookAttribute) {
        formatter = null;
        this.refBookAttribute = refBookAttribute;
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

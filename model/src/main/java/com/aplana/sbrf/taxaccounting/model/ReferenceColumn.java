package com.aplana.sbrf.taxaccounting.model;


import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;

import javax.xml.bind.annotation.XmlTransient;

/**
 * Зависимая графа
 *
 * @author Dmitriy Levykin
 */
public class ReferenceColumn extends Column {

	// ссылка на родительскую графу
	private int parentId;

	/**
	 *  только для экспорта/импорта
	 */
    private String parentAlias;

    private long refBookAttributeId;

    private Long refBookAttributeId2;

    private RefBookAttribute refBookAttribute;

    transient private ColumnFormatter formatter;

	public ReferenceColumn() {
		columnType = ColumnType.REFERENCE;
	}

	/**
	 * Возвращает код родительской графы
	 */
    @XmlTransient
    public int getParentId() {
        return parentId;
    }

	/**
	 * Устанавливает ссылку на родительскую графу
	 * @param parentId код родительской графы
	 */
    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

	/**
	 *  только для экспорта/импорта
	 */
    public String getParentAlias() {
        return parentAlias;
    }

	/**
	 *  только для экспорта/импорта
	 */
    public void setParentAlias(String parentAlias){
        this.parentAlias = parentAlias;
    }

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

    public Long getRefBookAttributeId2() {
        return refBookAttributeId2;
    }

    public void setRefBookAttributeId2(Long refBookAttributeId2) {
        this.refBookAttributeId2 = refBookAttributeId2;
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

    public RefBookAttribute getRefBookAttribute() {
        return refBookAttribute;
    }

    public void setRefBookAttribute(RefBookAttribute refBookAttribute) {
        this.refBookAttribute = refBookAttribute;
    }

    @Override
    public ValidationStrategy getValidationStrategy() {
        return validationStrategy;
    }
}

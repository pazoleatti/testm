package com.aplana.sbrf.taxaccounting.model;


import javax.xml.bind.annotation.XmlTransient;

/**
 * Зависимая графа
 *
 * @author Dmitriy Levykin
 */
public class ReferenceColumn extends Column {

	private int parentId;

    // только для экспорта/импорта
    private String parentAlias;

    private long refBookAttributeId;

    private Long refBookAttributeId2;

    private static Formatter formatter = new Formatter() {
        @Override
        public String format(String valueToFormat) {
            return String.valueOf(valueToFormat);
        }
    };

    @XmlTransient
    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getParentAlias() {
        return parentAlias;
    }

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
    public Formatter getFormatter() {
        return formatter;
    }

    @Override
    public ValidationStrategy getValidationStrategy() {
        return validationStrategy;
    }
}

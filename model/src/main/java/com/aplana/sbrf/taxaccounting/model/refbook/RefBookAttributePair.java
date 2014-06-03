package com.aplana.sbrf.taxaccounting.model.refbook;

import java.io.Serializable;

/**
 * Связка атрибута и записи справочника для массовой обработки
 * @author Denis Loshkarev
 */
public class RefBookAttributePair implements Serializable {
    private static final long serialVersionUID = 4762288582203512929L;

    /** Идентификатор атрибута */
    private Long attributeId;
    /** Уникальный идентификатор записи справочника */
    private Long uniqueRecordId;

    public RefBookAttributePair() {
    }

    public RefBookAttributePair(Long attributeId, Long uniqueRecordId) {
        this.attributeId = attributeId;
        this.uniqueRecordId = uniqueRecordId;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }

    public Long getUniqueRecordId() {
        return uniqueRecordId;
    }

    public void setUniqueRecordId(Long uniqueRecordId) {
        this.uniqueRecordId = uniqueRecordId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RefBookAttributePair that = (RefBookAttributePair) o;

        if (attributeId != null ? !attributeId.equals(that.attributeId) : that.attributeId != null) return false;
        if (uniqueRecordId != null ? !uniqueRecordId.equals(that.uniqueRecordId) : that.uniqueRecordId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = attributeId != null ? attributeId.hashCode() : 0;
        result = 31 * result + (uniqueRecordId != null ? uniqueRecordId.hashCode() : 0);
        return result;
    }
}

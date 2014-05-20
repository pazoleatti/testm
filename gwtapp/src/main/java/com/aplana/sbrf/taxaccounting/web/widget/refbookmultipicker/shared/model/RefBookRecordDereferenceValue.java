package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Модель разименованных атрибутов записи справочника
 *
 * @author aivanov
 */
public class RefBookRecordDereferenceValue implements Comparable<RefBookRecordDereferenceValue>, Serializable {
    private static final long serialVersionUID = 6686089787337922344L;

    /* Разименованое значение атрибута*/
    private String dereferenceValue;
    /* Идентификатор атрибута*/
    private Long valueAttrId;
    /* Алиас атрибута*/
    private String valueAttrAlias;
    /* Мапа для хранения разменованных значений по атрибуту второго уровня */
    private Map<Long, String> attrId2DerefValueMap  = new HashMap<Long, String>();

    public RefBookRecordDereferenceValue() {
    }

    public RefBookRecordDereferenceValue(Long valueAttrId, String valueAttrAlias, String dereferenceValue) {
        this.dereferenceValue = dereferenceValue;
        this.valueAttrId = valueAttrId;
        this.valueAttrAlias = valueAttrAlias;
    }

    public String getDereferenceValue() {
        return dereferenceValue;
    }

    public void setDereferenceValue(String dereferenceValue) {
        this.dereferenceValue = dereferenceValue;
    }

    public Long getValueAttrId() {
        return valueAttrId;
    }

    public void setValueAttrId(Long valueAttrId) {
        this.valueAttrId = valueAttrId;
    }

    public String getValueAttrAlias() {
        return valueAttrAlias;
    }

    public void setValueAttrAlias(String valueAttrAlias) {
        this.valueAttrAlias = valueAttrAlias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RefBookRecordDereferenceValue)) return false;

        RefBookRecordDereferenceValue that = (RefBookRecordDereferenceValue) o;

        if (dereferenceValue != null ? !dereferenceValue.equals(that.dereferenceValue) : that.dereferenceValue != null)
            return false;
        if (!valueAttrAlias.equals(that.valueAttrAlias)) return false;
        if (!valueAttrId.equals(that.valueAttrId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dereferenceValue != null ? dereferenceValue.hashCode() : 0;
        result = 31 * result + valueAttrId.hashCode();
        result = 31 * result + valueAttrAlias.hashCode();
        return result;
    }

    public Map<Long, String> getAttrId2DerefValueMap() {
        return attrId2DerefValueMap;
    }

    public void setAttrId2DerefValueMap(Map<Long, String> attrId2DerefValueMap) {
        this.attrId2DerefValueMap = attrId2DerefValueMap;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RefBookRecordDereferenceValue{");
        sb.append("dereferenceValue='").append(dereferenceValue).append('\'');
        sb.append(", valueAttrId=").append(valueAttrId);
        sb.append(", valueAttrAlias='").append(valueAttrAlias).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(RefBookRecordDereferenceValue o) {
        return valueAttrId.compareTo(o.getValueAttrId());
    }
}

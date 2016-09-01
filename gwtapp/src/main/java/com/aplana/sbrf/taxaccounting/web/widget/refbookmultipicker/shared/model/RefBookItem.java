package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * GUI модель для строки справочника
 *
 * @author sgoryachkin
 */
public class RefBookItem implements Serializable {
    private static final long serialVersionUID = 6686089751137927944L;

    private Long id;
    private String dereferenceValue;
    private List<RefBookRecordDereferenceValue> refBookRecordDereferenceValues;

    public RefBookItem() {
    }

    public RefBookItem(Long id, String dereferenceValue) {
        this.id = id;
        this.dereferenceValue = dereferenceValue;
    }

    public RefBookItem(Long id, String dereferenceValue, List<RefBookRecordDereferenceValue> refBookRecordDereferenceValues) {
        this.id = id;
        this.dereferenceValue = dereferenceValue;
        this.refBookRecordDereferenceValues = refBookRecordDereferenceValues;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDereferenceValue() {
        return dereferenceValue;
    }

    public void setDereferenceValue(String dereferenceValue) {
        this.dereferenceValue = dereferenceValue;
    }

    public List<RefBookRecordDereferenceValue> getRefBookRecordDereferenceValues() {
        return refBookRecordDereferenceValues;
    }

    public void setRefBookRecordDereferenceValues(List<RefBookRecordDereferenceValue> refBookRecordDereferenceValues) {
        this.refBookRecordDereferenceValues = refBookRecordDereferenceValues;
    }

    public void addRecordValues(String dereferenceValue, String valueAttrAlias, Long valueAttrId) {
        if (dereferenceValue == null) {
            dereferenceValue = "";
        }
        if (refBookRecordDereferenceValues == null) {
            refBookRecordDereferenceValues = new LinkedList<RefBookRecordDereferenceValue>();
        }
        refBookRecordDereferenceValues.add(new RefBookRecordDereferenceValue(valueAttrId, valueAttrAlias, dereferenceValue));
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RefBookItem{");
        sb.append("id=").append(id);
        sb.append(", dereferenceValue='").append(dereferenceValue).append('\'');
        sb.append(", refBookRecordDereferenceValues=").append(refBookRecordDereferenceValues);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RefBookItem item = (RefBookItem) o;
        return this.getDereferenceValue().equals(item.getDereferenceValue());
    }
}

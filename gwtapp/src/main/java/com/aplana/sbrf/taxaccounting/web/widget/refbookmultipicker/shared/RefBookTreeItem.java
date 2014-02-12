package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared;

import java.io.Serializable;
import java.util.List;

/**
 * GUI модель для строки древо-справочника
 * TODO (aivanov) нужно заимплементить от RefBookItem, внеся туда List<RefBookRecordDereferenceValue> refBookRecordDereferenceValues;
 *
 * @author aivanov
 */
public class RefBookTreeItem implements Comparable<RefBookTreeItem>, Serializable {
    private static final long serialVersionUID = 6686089751337922344L;

    private Long id;

    private RefBookTreeItem parent;

    private String dereferenceValue;
    private List<RefBookRecordDereferenceValue> refBookRecordDereferenceValues;

    private boolean hasChild = false;
    private boolean canBeSelected;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RefBookTreeItem getParent() {
        return parent;
    }

    public void setParent(RefBookTreeItem parent) {
        this.parent = parent;
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

    public boolean isHasChild() {
        return hasChild;
    }

    public void setHasChild(boolean hasChild) {
        this.hasChild = hasChild;
    }

    public boolean isCanBeSelected() {
        return canBeSelected;
    }

    public void setCanBeSelected(boolean canBeSelected) {
        this.canBeSelected = canBeSelected;
    }

    @Override
    public int compareTo(RefBookTreeItem o) {
        return (o == null || o.id == null) ? -1 : -o.id.compareTo(id);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RefBookTreeItem{");
        sb.append("id=").append(id);
        sb.append(", parent=").append(parent);
        sb.append(", hasChild=").append(hasChild);
        sb.append(", canBeSelected=").append(canBeSelected);
        sb.append(", refBookRecordDereferenceValues=").append(refBookRecordDereferenceValues);
        sb.append(", dereferenceValue='").append(dereferenceValue).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

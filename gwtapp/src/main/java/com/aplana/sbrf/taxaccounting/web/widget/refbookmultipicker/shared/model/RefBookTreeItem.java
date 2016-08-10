package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model;

/**
 * GUI модель для строки древо-справочника
 *
 * @author aivanov
 */
public class RefBookTreeItem extends RefBookItem implements Comparable<RefBookTreeItem> {

    private RefBookTreeItem parent;
    private boolean hasChild = true;
    private boolean canBeSelected;
    /* флаг для обозначения что при загрузке элемента его надо открыть для загрузки его чилдов */
    private boolean needToOpen = false;

    public RefBookTreeItem() {
    }

    public RefBookTreeItem(Long id, String dereferenceValue) {
        super(id, dereferenceValue);
    }

    public RefBookTreeItem getParent() {
        return parent;
    }

    public void setParent(RefBookTreeItem parent) {
        this.parent = parent;
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

    public boolean isNeedToOpen() {
        return needToOpen;
    }

    public void setNeedToOpen(boolean needToOpen) {
        this.needToOpen = needToOpen;
    }

    @Override
    public int compareTo(RefBookTreeItem o) {
        Long thisId = this.getId();
        Long anotherId = o == null ? null : o.getId();
        return anotherId == null ? -1 : thisId.compareTo(anotherId);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RefBookTreeItem{");
        sb.append("id=").append(getId());
        sb.append(", parent=").append(parent);
        sb.append(", hasChild=").append(hasChild);
        sb.append(", canBeSelected=").append(canBeSelected);
        sb.append(", needToOpen=").append(needToOpen);
        sb.append(", refBookRecordDereferenceValues=").append(getRefBookRecordDereferenceValues());
        sb.append(", dereferenceValue='").append(getDereferenceValue()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

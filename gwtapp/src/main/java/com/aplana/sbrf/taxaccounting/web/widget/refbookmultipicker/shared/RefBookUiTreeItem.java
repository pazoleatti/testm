package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared;

import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.LazyTreeItem;

/**
 * GUI элемент для дерева с ленивой загрузкой с логическим элементов дерева справочника
 *
 * @author aivanov
 */
public class RefBookUiTreeItem extends LazyTreeItem {

    private RefBookTreeItem refBookTreeItem;

    public RefBookUiTreeItem(RefBookTreeItem refBookTreeItem) {
        super(refBookTreeItem.getId(), refBookTreeItem.getDereferenceValue());
        this.refBookTreeItem = refBookTreeItem;
    }

    public RefBookUiTreeItem(RefBookTreeItem refBookTreeItem, Boolean multiSelection) {
        super(refBookTreeItem.getId(), refBookTreeItem.getDereferenceValue(), multiSelection);
        this.refBookTreeItem = refBookTreeItem;
    }

    public RefBookTreeItem getRefBookTreeItem() {
        return refBookTreeItem;
    }

    public void setRefBookTreeItem(RefBookTreeItem refBookTreeItem) {
        this.refBookTreeItem = refBookTreeItem;
    }
}

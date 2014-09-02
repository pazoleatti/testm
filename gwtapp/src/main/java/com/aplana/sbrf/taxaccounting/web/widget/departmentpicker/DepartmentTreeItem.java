package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.MultiSelectTreeItem;
import com.google.gwt.user.client.DOM;

/**
 * Элемент дерева подразделений. Необходим для обработки события выбора элемента дерева
 * @author dloshkarev
 */
public class DepartmentTreeItem extends MultiSelectTreeItem {

    /** Идентификатор подразделения */
    private DepartmentPair itemValue;
    private boolean isActive;

    /**
     * Элемент дерева подразделений.
     *
     * @param department информация подразделении банка
     * @param multiSelection true - выбрать несколько элементов, false - выбрать один элемент
     */
    public DepartmentTreeItem(Department department, boolean multiSelection) {
        super(department.getId(), department.getName(), multiSelection);
        isActive = department.isActive();
        this.itemValue = new DepartmentPair(department.getId(), department.getParentId(), department.getName());
    }

    /** Получить информацию о подразделении банка. */
    public DepartmentPair getItemValue() {
        return itemValue;
    }

    public boolean isActive() {
        return isActive;
    }

    /**
     * Добавляет звездочку к названию неактивного итема
     */
    public void deactivate() {
        String markedName = DepartmentTreeWidget.RED_STAR_SPAN + getName();
        label.getElement().setInnerHTML(markedName);
        DOM.getChild(checkBox.getElement(), 1).setInnerHTML(markedName);
        DOM.getChild(radioButton.getElement(), 1).setInnerHTML(markedName);
    }
}

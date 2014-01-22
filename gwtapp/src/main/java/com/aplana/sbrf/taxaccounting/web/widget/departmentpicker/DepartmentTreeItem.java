package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.MultiSelectTreeItem;

/**
 * Элемент дерева подразделений. Необходим для обработки события выбора элемента дерева
 * @author dloshkarev
 */
public class DepartmentTreeItem extends MultiSelectTreeItem {

    /** Идентификатор подразделения */
    private DepartmentPair itemValue;

    /**
     * Элемент дерева подразделений.
     *
     * @param department информация подразделении банка
     * @param multiSelection true - выбрать несколько элементов, false - выбрать один элемент
     */
    public DepartmentTreeItem(Department department, boolean multiSelection) {
        super(department.getId(), department.getName(), multiSelection);
        this.itemValue = new DepartmentPair(department.getId(), department.getParentId(), department.getName());
    }

    /** Получить информацию о подразделении банка. */
    public DepartmentPair getItemValue() {
        return itemValue;
    }
}

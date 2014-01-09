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

    public DepartmentTreeItem(Department department, boolean multiSelection) {
        super(department.getId(), department.getName(), multiSelection);
        this.itemValue = new DepartmentPair(department.getId(), department.getParentId(), department.getName());
    }

    public DepartmentPair getItemValue() {
        return itemValue;
    }
}

package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

/**
 * Интерфейс описывающий действие, которое можно выполнить над элементами при обходе дерева
 * @author dloshkarev
 */
public interface TreeElementAction {
    void apply(DepartmentTreeItem treeItem);
}

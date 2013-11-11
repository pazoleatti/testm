package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.google.gwt.user.client.ui.HasConstrainedValue;

import java.util.List;

/**
 * Интерфейс виджета для выбора ребра графа подразделений
 * @author dloshkarev
 */
public interface PairDepartmentPicker extends HasConstrainedValue<List<DepartmentPair>> {

    /**
     * Устанавливает заголовок для дерева подразделений
     * @param header заголовок
     */
    void setHeader(String header);

    /**
     * Устанавливает список подразделений отображаемых в дереве
     * @param departments список подразделений
     */
    void setAvailableValues(List<Department> departments);

    /**
     * Проверяет есть ли у выбранного элемента дочерние подразделения
     * @return есть?
     */
    boolean isSelectedItemHasChildren();

    /**
     * Возвращает все дочерние подразделения + выбранное подразделение
     * @return список подразделений
     */
    List<DepartmentPair> getSelectedChildren();
}

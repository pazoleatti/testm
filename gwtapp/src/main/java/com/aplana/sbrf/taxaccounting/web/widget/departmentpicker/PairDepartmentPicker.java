package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.google.gwt.user.client.ui.HasConstrainedValue;

import java.util.List;
import java.util.Set;

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

    List<Integer> getAvalibleValues();

    /**
     * Устанавливает список подразделений отображаемых в дереве
     * @param departments список подразделений
     */
    void setAvailableValues(List<Department> departments);

    /**
     * Устанавливает список подразделений отображаемых в дереве.
     *
     * @param departments список подразделений
     * @param availableDepartments список доступных подразделений
     */
    void setAvailableValues(List<Department> departments, Set<Integer> availableDepartments);

    /**
     * Проверяет есть ли у выбранного элемента дочерние подразделения
     * @return есть?
     */
    boolean isSelectedItemHasChildren();

    /**
     * Возвращает выбранное подразделение + все дочерние подразделения
     * @return список подразделений
     */
    List<DepartmentPair> getSelectedChildren();
}

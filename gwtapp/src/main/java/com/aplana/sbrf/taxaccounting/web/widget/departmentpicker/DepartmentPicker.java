package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.google.gwt.user.client.ui.HasValue;

import java.util.List;
import java.util.Set;

/**
 * @author Eugene Stetsenko
 */
public interface DepartmentPicker extends HasValue<List<Integer>> {
    /**
     * Установка списка подразделений для дерева и подразделений, доступных для выбора
     * @param departments Подразделения для построения дерева
     * @param availableDepartments Доступные для выбора подразделения
     */
	void setAvalibleValues(List<Department> departments, Set<Integer> availableDepartments);

    /**
     * Список подразделений доступных для выбора
     */
    List<Integer> getAvalibleValues();
	
	List<String> getValueDereference();
	
	void setHeader(String header);
}

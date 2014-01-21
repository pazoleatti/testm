package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

import java.util.List;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.google.gwt.user.client.ui.HasValue;

/**
 * @author Eugene Stetsenko
 */
public interface DepartmentPicker extends HasValue<List<Integer>> {

	void setAvalibleValues(List<Department> departments, Set<Integer> availableDepartments);

    /** Получить список названий выбранных подразделений. */
	List<String> getValueDereference();

    /** Установить заголовок в окне выбора подразделений. */
	void setHeader(String header);

    void setSelectButtonFocus(boolean focused);
}

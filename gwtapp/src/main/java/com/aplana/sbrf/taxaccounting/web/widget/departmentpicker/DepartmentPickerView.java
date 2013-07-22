package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

import com.aplana.sbrf.taxaccounting.model.Department;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Eugene Stetsenko
 */
public interface DepartmentPickerView {

	Map<String, Integer> getSelectedItems();

	void setSelectedItems(Map<String, Integer> items);

	void setTreeValues(List<Department> departments, Set<Integer> availableDepartments);

	void setWidth(int width);

}

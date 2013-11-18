package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

import java.util.List;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.web.widget.titlepanel.HasTitlePanel;
import com.google.gwt.user.client.ui.HasValue;

/**
 * @author Eugene Stetsenko
 */
public interface DepartmentPicker extends HasValue<List<Integer>>, HasTitlePanel {

	void setAvalibleValues(List<Department> departments, Set<Integer> availableDepartments);
	
	List<String> getValueDereference();
	
	void setHeader(String header);

}

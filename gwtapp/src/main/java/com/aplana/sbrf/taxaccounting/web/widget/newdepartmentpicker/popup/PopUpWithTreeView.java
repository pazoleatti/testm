package com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.popup;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.SelectDepartmentsEventHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import java.util.List;
import java.util.Set;

/**
 * @author Eugene Stetsenko
 */
public interface PopUpWithTreeView {

	public void setItems(List<Department> departments, Set<Integer> availableDepartments);

	public HandlerRegistration addDepartmentsReceivedEventHandler(
			SelectDepartmentsEventHandler handler);

}

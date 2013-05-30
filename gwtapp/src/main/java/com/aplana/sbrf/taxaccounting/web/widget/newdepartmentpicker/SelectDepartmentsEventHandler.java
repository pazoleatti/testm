package com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker;

import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.popup.SelectDepartmentsEvent;
import com.google.gwt.event.shared.EventHandler;

/**
 * @author Eugene Stetsenko
 */
public interface SelectDepartmentsEventHandler extends EventHandler {
	void onDepartmentsReceived(SelectDepartmentsEvent event);
}

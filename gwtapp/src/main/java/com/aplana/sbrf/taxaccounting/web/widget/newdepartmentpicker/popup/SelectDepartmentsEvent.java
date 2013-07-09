package com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.popup;

import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.SelectDepartmentsEventHandler;
import com.google.gwt.event.shared.GwtEvent;

import java.util.Map;

/**
 * @author Eugene Stetsenko
 */

public class SelectDepartmentsEvent extends GwtEvent<SelectDepartmentsEventHandler> {

	public static final Type<SelectDepartmentsEventHandler> TYPE = new Type<SelectDepartmentsEventHandler>();

	private final Map<String, Integer> selectedItems;

	public SelectDepartmentsEvent(Map<String, Integer> selectedItems) {
		this.selectedItems = selectedItems;
	}

	@Override
	public Type<SelectDepartmentsEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(SelectDepartmentsEventHandler handler) {
		handler.onDepartmentsReceived(this);
	}

	public Map<String, Integer> getItems() {
		return selectedItems;
	}
}
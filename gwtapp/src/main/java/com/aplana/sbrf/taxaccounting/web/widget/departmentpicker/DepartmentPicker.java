package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.popup.PopUpWithTree;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.popup.PopUpWithTreeView;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.popup.SelectDepartmentsEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

import java.util.*;

/**
 * Виджет для выбора подразделений
 * @author Eugene Stetsenko
 */
public class DepartmentPicker extends Composite implements HasEnabled, DepartmentPickerView, SelectDepartmentsEventHandler {

	private PopupPanel popup;

	private PopUpWithTreeView popUpWithTreeView;

	private Map<String, Integer> selectedItems = new HashMap<String, Integer>();

	@UiField
	public TextBox selected;

	@UiField
	public Button selectButton;

	@Override
	public boolean isEnabled() {
		return (selectButton.isEnabled() && selected.isEnabled());
	}

	@Override
	public void setEnabled(boolean enabled) {
		selectButton.setEnabled(enabled);
		selected.setEnabled(enabled);
	}

	interface SelectionUiBinder extends UiBinder<HTMLPanel, DepartmentPicker> {
	}

	private static SelectionUiBinder uiBinder = GWT.create(SelectionUiBinder.class);

	@UiConstructor
	public DepartmentPicker(String header, boolean withCheckBox) {
		initWidget(uiBinder.createAndBindUi(this));
		// TODO move to ui.xml
		this.popup = new PopupPanel(true, true);
		popup.setPixelSize(300, 370);
		popUpWithTreeView = new PopUpWithTree(header, withCheckBox);
		addDepartmentsReceivedEventHandler(this);

		popup.add((PopUpWithTree)popUpWithTreeView);
	}

	public void addDepartmentsReceivedEventHandler(SelectDepartmentsEventHandler handler) {
		popUpWithTreeView.addDepartmentsReceivedEventHandler(handler);
	}

	@Override
	public Map<String, Integer> getSelectedItems() {
		return selectedItems == null ? new HashMap<String, Integer>() : selectedItems;
	}

	@Override
	public void setSelectedItems(Map<String, Integer> items) {
		this.selectedItems = items;
		if (items == null) {
			selected.setText("");
			popUpWithTreeView.selectItems(null);
		} else {
			selected.setText(joinListToString(items.keySet()));
			popUpWithTreeView.selectItems(items.values());
		}
	}

	@Override
	public void setTreeValues(List<Department> departments, Set<Integer> availableDepartments) {
		popUpWithTreeView.setItems(departments, availableDepartments);
	}

	@Override
	public void setWidth(int width) {
		selected.setWidth(new String(width + "px"));
		popup.setPixelSize(width, 370);
	}

	@UiHandler("selectButton")
	void onSelectButtonClicked(ClickEvent event){

		popup.setPopupPosition(selected.getAbsoluteLeft(),
				selected.getAbsoluteTop() + selected.getOffsetHeight());
		popup.show();
	}

	@Override
	public void onDepartmentsReceived(SelectDepartmentsEvent event) {
		selectedItems = event.getItems();
		selected.setText(joinListToString(event.getItems().keySet()));
		popup.hide();
	}

	private String joinListToString(Collection<String> strings) {
		StringBuilder text = new StringBuilder();
		for (String name : strings) {
			text.append(name + "; ");
		}
		return text.toString();
	}
}

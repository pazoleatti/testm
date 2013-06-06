package com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.popup.PopUpWithTree;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.popup.PopUpWithTreeView;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.popup.SelectDepartmentsEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

import java.util.*;

/**
 * Виджет для выбора подразделений
 * @author Eugene Stetsenko
 */
public class NewDepartmentPicker extends Composite implements NewDepartmentPickerView, SelectDepartmentsEventHandler {

	private PopupPanel popup;

	private PopUpWithTreeView popUpWithTreeView;

	private Map<String, Integer> selectedItems = new HashMap<String, Integer>();

	@UiField
	public TextBox selected;

	interface SelectionUiBinder extends UiBinder<HTMLPanel, NewDepartmentPicker> {
	}

	private static SelectionUiBinder uiBinder = GWT.create(SelectionUiBinder.class);

	@UiConstructor
	public NewDepartmentPicker(String header, boolean withCheckBox) {
		initWidget(uiBinder.createAndBindUi(this));
		// TODO move to ui.xml
		this.popup = new PopupPanel(true, true);
		popup.setPixelSize(300, 370);
		popUpWithTreeView = new PopUpWithTree(header, withCheckBox);
		popUpWithTreeView.addDepartmentsReceivedEventHandler(this);

		popup.add((PopUpWithTree)popUpWithTreeView);
	}

	@Override
	public Map<String, Integer> getSelectedItems() {
		return selectedItems == null ? new HashMap<String, Integer>() : selectedItems;
	}

	@Override
	public void setSelectedItems(Map<String, Integer> items) {
		this.selectedItems = items;
		selected.setText(joinListToString(items.keySet()));
		popUpWithTreeView.selectItems(items.values());
	}

	@Override
	public void setTreeValues(List<Department> departments, Set<Integer> availableDepartments) {
		popUpWithTreeView.setItems(departments, availableDepartments);
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

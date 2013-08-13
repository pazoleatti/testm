package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Виджет для выбора подразделений
 * @author Eugene Stetsenko
 */
public class DepartmentPickerPopupWidget extends Composite implements HasEnabled, DepartmentPicker{

	private PopupPanel popup;

	private DepartmentPicker departmentPiker;

	@UiField
	TextBox selected;

	@UiField
	Button selectButton;

	@Override
	public boolean isEnabled() {
		return (selectButton.isEnabled() && selected.isEnabled());
	}

	@Override
	public void setEnabled(boolean enabled) {
		selectButton.setEnabled(enabled);
		selected.setEnabled(enabled);
	}

	interface Binder extends UiBinder<HTMLPanel, DepartmentPickerPopupWidget> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiConstructor
	public DepartmentPickerPopupWidget(String header, boolean multiselection) {
		initWidget(uiBinder.createAndBindUi(this));
		// TODO move to ui.xml
		this.popup = new PopupPanel(true, true);
		popup.setPixelSize(300, 370);
		departmentPiker = new DepartmentPickerWidget(header, multiselection);

		popup.add((DepartmentPickerWidget)departmentPiker);
		
		departmentPiker.addValueChangeHandler(new ValueChangeHandler<List<Integer>>() {
			@Override
			public void onValueChange(ValueChangeEvent<List<Integer>> event) {
				selected.setText(joinListToString(departmentPiker.getValueDereference()));
				ValueChangeEvent.fire(DepartmentPickerPopupWidget.this, event.getValue());
			}
		});
	}

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


	private String joinListToString(Collection<String> strings) {
		StringBuilder text = new StringBuilder();
		for (String name : strings) {
			text.append(name + "; ");
		}
		return text.toString();
	}

	@Override
	public List<Integer> getValue() {
		return departmentPiker.getValue();
	}

	@Override
	public void setValue(List<Integer> value) {
		departmentPiker.setValue(value);
	}

	@Override
	public void setValue(List<Integer> value, boolean fireEvents) {
		departmentPiker.setValue(value, fireEvents);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<List<Integer>> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public void setAvalibleValues(List<Department> departments,
			Set<Integer> availableDepartments) {
		departmentPiker.setAvalibleValues(departments, availableDepartments);
	}

	@Override
	public List<String> getValueDereference() {
		return departmentPiker.getValueDereference();
	}

	@Override
	public void setHeader(String header) {
		departmentPiker.setHeader(header);
	}

	@Override
	public void setMultiselection(boolean multiselection) {
		departmentPiker.setMultiselection(multiselection);
	}
}

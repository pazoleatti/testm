package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.web.widget.closabledialog.ClosableDialogBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Виджет для выбора подразделений
 * @author Eugene Stetsenko
 */
public class DepartmentPickerPopupWidget extends Composite implements HasEnabled, DepartmentPicker {

	private PopupPanel popup;

	private DepartmentPicker departmentPiker;

    @UiField
    SimplePanel wrappingPanel;

	@UiField
	HasText selected;

	@UiField
	Button selectButton;
	
	@UiField
	Button clearButton;
	
	@UiField
	Panel panel;

    /** Признак модальности окна */
    private boolean modal;

	@Override
	public boolean isEnabled() {
		return (selectButton.isEnabled());
	}

	@Override
	public void setEnabled(boolean enabled) {
		selectButton.setEnabled(enabled);
		clearButton.setEnabled(enabled);
	}

	interface Binder extends UiBinder<Widget, DepartmentPickerPopupWidget> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiConstructor
	public DepartmentPickerPopupWidget(String header, boolean multiselection, boolean modal) {
		initWidget(uiBinder.createAndBindUi(this));
		// TODO move to ui.xml
        this.modal = modal;
        if (modal) {
            popup = new ClosableDialogBox(false, true);
            ((ClosableDialogBox) popup).setText(header);
        } else {
            popup = new PopupPanel(true, true);
        }
		popup.setPixelSize(300, 370);
		departmentPiker = new DepartmentPickerWidget(header, multiselection);

		popup.add((DepartmentPickerWidget)departmentPiker);
		
		departmentPiker.addValueChangeHandler(new ValueChangeHandler<List<Integer>>() {
			@Override
			public void onValueChange(ValueChangeEvent<List<Integer>> event) {
                popup.hide();
				selected.setText(joinListToString(departmentPiker.getValueDereference()));
				ValueChangeEvent.fire(DepartmentPickerPopupWidget.this, event.getValue());
			}
		});
	}


	@UiHandler("selectButton")
	void onSelectButtonClicked(ClickEvent event){
        if (!modal) {
            popup.setPopupPosition(panel.getAbsoluteLeft(),
                    panel.getAbsoluteTop() + panel.getOffsetHeight());
            popup.show();
        } else {
            popup.center();
        }
	}
	
	@UiHandler("clearButton")
	void onClearButtonClicked(ClickEvent event){
		this.setValue(null, true);
	}


	private String joinListToString(Collection<String> strings) {
		if ((strings == null) || strings.isEmpty()) {
			return "";
		}
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
		setValue(value, false);
	}

	@Override
	public void setValue(List<Integer> value, boolean fireEvents) {
		departmentPiker.setValue(value, fireEvents);
		if (!fireEvents) {
			selected.setText(joinListToString(departmentPiker.getValueDereference()));
		}
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
    public void setTitle(String title) {
        if (popup instanceof ClosableDialogBox) {
            ((ClosableDialogBox) popup).setText(title);
        }
    }

    public void setWidth(String width){
        wrappingPanel.setWidth(width);
    }

}

package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Виджет для выбора подразделения в модальном окне
 * @author dloshkarev
 */
public class DepartmentPickerModalWidget extends Composite implements
        DepartmentPickerModal, IsEditor<TakesValueEditor<List<DepartmentPair>>> {

    interface Binder extends UiBinder<Widget, DepartmentPickerModalWidget> {
    }

    private static Binder uiBinder = GWT.create(Binder.class);

    @UiField
    TextBox selected;

    @UiField
    Button selectButton;

    @UiField
    Button clearButton;

    @UiField
    Panel panel;

    @UiField
    PopupPanel popupPanel;

    @UiField(provided = true)
    DepartmentTreeWidget departmentTree;

    private TakesValueEditor<List<DepartmentPair>> editor;

    /** Выбранные подразделения */
    private final List<DepartmentPair> value = new ArrayList<DepartmentPair>();

    @UiConstructor
    public DepartmentPickerModalWidget(String header, boolean multiSelection) {
        departmentTree = new DepartmentTreeWidget(header, multiSelection);
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public TakesValueEditor<List<DepartmentPair>> asEditor() {
        if (editor == null) {
            editor = TakesValueEditor.of(this);
        }
        return editor;
    }

    @Override
    public boolean isEnabled() {
        return selected.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        selected.setEnabled(enabled);
        selectButton.setVisible(enabled);
        clearButton.setVisible(enabled);
    }

    @Override
    public void setAcceptableValues(Collection<List<DepartmentPair>> values) {
        this.departmentTree.setAcceptableValues(values);
    }

    @Override
    public List<DepartmentPair> getValue() {
        return value;
    }

    @Override
    public void setValue(List<DepartmentPair> value) {
        setValue(value, false);
    }

    @Override
    public void setValue(List<DepartmentPair> value, boolean fireEvents) {
        this.selected.setValue(value.get(0).getDepartmentName());
        this.value.clear();
        this.value.addAll(value);
        this.departmentTree.setValue(value);
        if (fireEvents){
            ValueChangeEvent.fire(this, value);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<DepartmentPair>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public void setHeader(String header) {
        this.departmentTree.setHeader(header);
    }

    @Override
    public void setAvailableValues(List<Department> departments) {
        this.departmentTree.setAvailableValues(departments);
    }

    @Override
    public boolean isSelectedItemHasChildren() {
        return this.departmentTree.isSelectedItemHasChildren();
    }

    @Override
    public List<DepartmentPair> getSelectedChildren() {
        return this.departmentTree.getSelectedChildren();
    }

    @UiHandler("selectButton")
    public void onSelectClick(ClickEvent event){
        departmentTree.setValue(value);
        popupPanel.center();
        popupPanel.show();
    }

    @UiHandler("okButton")
    public void onOkButtonClick(ClickEvent event){
	    if (!departmentTree.getValue().isEmpty()) {
	        this.selected.setValue(departmentTree.getValue().get(0).getDepartmentName());
	    }
        this.value.clear();
        this.value.addAll(departmentTree.getValue());
        popupPanel.hide();
    }
}

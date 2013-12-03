package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * Элемент дерева подразделений. Необходим для обработки события выбора элемента дерева
 * @author dloshkarev
 */
public class DepartmentTreeItem extends TreeItem implements HasValue<Boolean> {

    private static final String RADIO_BUTTON_GROUP  = "DEPARTMENT_GROUP";

    /** Идентификатор подразделения */
    private DepartmentPair itemValue;

    public DepartmentTreeItem(Department department, boolean multiSelection){
        this.itemValue = new DepartmentPair(department.getId(), department.getParentId(), department.getName());
        CheckBox checkBox;
        if(multiSelection){
            checkBox = new CheckBox(department.getName());
        } else {
            checkBox = new RadioButton(RADIO_BUTTON_GROUP, department.getName());
        }
        setWidget(checkBox);
    }

    @Override
    public Boolean getValue() {
        return ((CheckBox) getWidget()).getValue();
    }

    @Override
    public void setValue(Boolean value) {
        ((CheckBox) getWidget()).setValue(value);
    }

    @Override
    public void setValue(Boolean value, boolean fireEvents) {
        ((CheckBox) getWidget()).setValue(value, fireEvents);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
        return ((CheckBox) getWidget()).addValueChangeHandler(handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        ((CheckBox) getWidget()).fireEvent(event);
    }

    public DepartmentPair getItemValue() {
        return itemValue;
    }
}

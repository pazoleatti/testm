package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.client.DateTimeFormatRenderer;
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
public class DepartmentPickerPopupWidget extends Composite implements HasEnabled, DepartmentPicker {

    @UiField
    FlowPanel wrappingPanel;

    @UiField
    HasText selected;

    @UiField
    Button selectButton;

    @UiField
    Button clearButton;

    @UiField
    Panel panel;

    @UiField
    public DepartmentTreeWidget tree;

    @UiField
    public Button ok;

    @UiField
    ModalWindow popupPanel;

    @UiField
    TextBox filter;

    @UiField
    Button find;

    @UiField
    CheckBox selectChild;

    @UiField(provided=true)
    ValueListBox<Date> version;

    @UiField
    Button cancel;

    @UiField
    HorizontalPanel itemsInfoPanel;

    @UiField
    Label countItems;

    /** Значения id */
    private List<Integer> value = new ArrayList<Integer>();

    /** Разименованные значения. */
    private List<String> valueDereference = new ArrayList<String>();

    boolean multiselection;

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

    /** Виджет для выбора подразделений. */
    @UiConstructor
    public DepartmentPickerPopupWidget(String header, boolean multiselection, boolean modal) {
        version = new ValueListBox<Date>(new DateTimeFormatRenderer());
        initWidget(uiBinder.createAndBindUi(this));
        this.multiselection = multiselection;
        tree.setMultiSelection(multiselection);
        selectChild.setVisible(multiselection);
        itemsInfoPanel.setVisible(multiselection);
        popupPanel.setModal(modal);
        setHeader(header);

        // TODO (Ramil Timerbaev) в "Дата актуальности" пока выставил текущую дату
        setVersion(new Date());

        updateCountItems();
    }

    @UiHandler("selectButton")
    void onSelectButtonClicked(ClickEvent event){
        tree.setValueById(value, false);
        popupPanel.center();
    }

    @UiHandler("clearButton")
    void onClearButtonClicked(ClickEvent event) {
        valueDereference.clear();
        this.setValue(null, true);
    }

    private String joinListToString(Collection<String> strings) {
        if ((strings == null) || strings.isEmpty()) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        for (String name : strings) {
            text.append(name).append("; ");
        }
        return text.toString();
    }

    @Override
    public List<Integer> getValue() {
        return value;
    }

    @Override
    public void setValue(List<Integer> value) {
        setValue(value, false);
    }

    @Override
    public void setValue(List<Integer> value, boolean fireEvents) {
        this.value.clear();
        if(value != null){
            this.value.addAll(value);
        }
        setValueById(this.value);
        selected.setText(joinListToString(valueDereference));
        if (fireEvents) {
            ValueChangeEvent.fire(this, this.value);
        }
    }

    /** Установить выбранными узлы дерева для указанных подразделений. */
    public void setValueByDepartmentPair(List<DepartmentPair> values, boolean fireEvents) {
        List<Integer> list = new ArrayList<Integer>();
        for (DepartmentPair i : values) {
            list.add(i.getDepartmentId());
        }
        setValue(list, fireEvents);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<Integer>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public List<String> getValueDereference() {
        return valueDereference;
    }

    @Override
    public void setHeader(String header) {
        popupPanel.setText(header);
    }

    @Override
    public void setTitle(String title) {
        popupPanel.setText(title);
    }

    @Override
    public void setWidth(String width){
        wrappingPanel.setWidth(width);
    }

    @Override
    public void setAvalibleValues(List<Department> departments, Set<Integer> availableDepartments) {
        tree.setAvailableValues(departments, availableDepartments);
    }

    @Override
    public List<Integer> getAvalibleValues() {
        return tree.getAvalibleValues();
    }

    /** Установить выбранными элементы по идентификаторам. */
    private void setValueById(List<Integer> itemsIdToSelect) {
        tree.setValueById(itemsIdToSelect, false);
        valueDereference.clear();
        for (DepartmentPair item : tree.getValue()) {
            valueDereference.add(item.getDepartmentName());
        }
        countItems.setText(String.valueOf(valueDereference.size()));
        ok.setEnabled(valueDereference.size() > 0);
    }

    @UiHandler("ok")
    void onOkButtonClicked(ClickEvent event) {
        this.value.clear();
        this.valueDereference.clear();
        for (DepartmentPair item : tree.getValue()) {
            this.value.add(item.getDepartmentId());
            this.valueDereference.add(item.getDepartmentName());
        }
        ValueChangeEvent.fire(this, this.value);
        selected.setText(joinListToString(valueDereference));
        popupPanel.hide();
    }

    /** Получить выбранные подразделения. */
    public List<DepartmentPair> getDepartmentPairValues() {
        return tree.getValue();
    }

    @UiHandler("find")
    void onFindButtonClicked(ClickEvent event) {
        tree.filter(filter.getText());
    }

    @UiHandler("cancel")
    void onCancelButtonClicked(ClickEvent event) {
        popupPanel.hide();
        List<Integer> list = new ArrayList<Integer>(value);
        setValue(list);
    }

    @UiHandler("selectChild")
    void onSelectChildValueChange(ValueChangeEvent<Boolean> event) {
        tree.setSelectChild(selectChild.getValue());
    }

    public Date getVersion() {
        return version.getValue();
    }

    public void setVersion(Date versionDate) {
        version.setValue(versionDate);
    }

    public void setVersions(List<Date> versions, Date defaultValue) {
        version.setValue(defaultValue);
        version.setAcceptableValues(versions);
    }

    @Override
    public void setSelectButtonFocus(boolean focused) {
        selectButton.setFocus(focused);
    }

    /** Обновить значение количества выбранных элементов. */
    private void updateCountItems() {
        tree.addValueChangeHandler(new ValueChangeHandler<List<DepartmentPair>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<DepartmentPair>> event) {
                int size = tree.getValue().size();
                ok.setEnabled(size > 0);
                if (multiselection) {
                    countItems.setText(String.valueOf(size));
                }
            }
        });
    }
}
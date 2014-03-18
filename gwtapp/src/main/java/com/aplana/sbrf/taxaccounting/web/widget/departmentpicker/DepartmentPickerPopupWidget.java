package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

import com.aplana.gwt.client.DoubleStateComposite;
import com.aplana.gwt.client.ModalWindow;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.web.widget.utils.TextUtils;
import com.aplana.sbrf.taxaccounting.web.widget.utils.WidgetUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
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
 *
 * @author Eugene Stetsenko
 */
public class DepartmentPickerPopupWidget extends DoubleStateComposite implements DepartmentPicker {

    interface Binder extends UiBinder<Widget, DepartmentPickerPopupWidget> {
    }

    private static Binder uiBinder = GWT.create(Binder.class);
    public static String defaultHeader = "Выбор подразделения";

    @UiField
    HTMLPanel wrappingPanel;

    @UiField
    TextBox selected,
            filter;

    @UiField
    Image selectButton,
            clearButton;

    @UiField
    Button find,
            cancel,
            ok;

    @UiField
    HorizontalPanel
            itemsInfoPanel,
            panel;

    @UiField
    DepartmentTreeWidget tree;

    @UiField
    ModalWindow popupPanel;

    @UiField
    CheckBox selectChild;

    @UiField(provided = true)
    ValueListBox<Date> version;

    @UiField
    Label countItems;

    private boolean multiSelect;
    /* Значения id  */
    private List<Integer> value = new LinkedList<Integer>();
    /* Разименованные значения.   */
    private List<String> valueDereference = new LinkedList<String>();

    @UiConstructor
    public DepartmentPickerPopupWidget(boolean multiselection) {
        version = new ValueListBox<Date>(new DateTimeFormatRenderer());
        initWidget(uiBinder.createAndBindUi(this));
        this.multiSelect = multiselection;
        tree.setMultiSelection(multiselection);
        selectChild.setVisible(multiselection);
        itemsInfoPanel.setVisible(multiselection);
        setHeader(defaultHeader);

        WidgetUtils.setMouseBehavior(clearButton, selected, selectButton);

        // TODO (Ramil Timerbaev) в "Дата актуальности" пока выставил текущую дату
        setVersion(new Date());

        /* Обновить значение количества выбранных элементов. */
        tree.addValueChangeHandler(new ValueChangeHandler<List<DepartmentPair>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<DepartmentPair>> event) {
                int size = tree.getValue().size();
                ok.setEnabled(size > 0);
                if (isMultiSelect()) {
                    countItems.setText(String.valueOf(size));
                }
            }
        });
    }

    @UiHandler("selectButton")
    void onSelectButtonClicked(ClickEvent event) {
        tree.setValueById(value, false);
        popupPanel.center();
    }

    @UiHandler("clearButton")
    void onClearButtonClicked(ClickEvent event) {
        valueDereference.clear();
        this.setValue(null, true);
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

        String text = TextUtils.joinListToString(valueDereference);
        selected.setText(text);
        selected.setTitle(TextUtils.generateTextBoxTitle(text));
        this.setLabelValue(text);

        popupPanel.hide();
    }

    @UiHandler("find")
    void onFindButtonClicked(ClickEvent event) {
        tree.filter(filter.getText());
    }

    @UiHandler("cancel")
    void onCancelButtonClicked(ClickEvent event) {
        popupPanel.hide();
        List<Integer> list = new LinkedList<Integer>(value);
        setValue(list);
    }

    @UiHandler("selectChild")
    void onSelectChildValueChange(ValueChangeEvent<Boolean> event) {
        tree.setSelectChild(selectChild.getValue());
    }

    @Override
    public List<Integer> getValue() {
        Collections.sort(value);
        return new ArrayList<Integer>(value);
    }

    @Override
    public void setValue(List<Integer> value) {
        setValue(value, false);
    }

    @Override
    public void setValue(List<Integer> value, boolean fireEvents) {
        this.value.clear();
        if (value != null) {
            this.value.addAll(value);
        }
        setValueById(this.value);

        String text = TextUtils.joinListToString(valueDereference);
        selected.setText(text);
        selected.setTitle(TextUtils.generateTextBoxTitle(text));

        setLabelValue(text);

        if (fireEvents) {
            ValueChangeEvent.fire(this, this.value);
        }
    }

    /**
     * Обновляет значение Label в состоянии disabled.
     */
    @Override
    protected void updateLabelValue() {
        setLabelValue(TextUtils.joinListToString(valueDereference));
    }

    @Override
    public List<Integer> getAvalibleValues() {
        return tree.getAvalibleValues();
    }

    @Override
    public void setAvalibleValues(List<Department> departments, Set<Integer> availableDepartments) {
        tree.setAvailableValues(departments, availableDepartments);
    }

    /**
     * Установить выбранными узлы дерева для указанных подразделений.
     */
    public void setValueByDepartmentPair(List<DepartmentPair> values, boolean fireEvents) {
        List<Integer> list = new LinkedList<Integer>();
        for (DepartmentPair i : values) {
            list.add(i.getDepartmentId());
        }
        setValue(list, fireEvents);
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
    public void setWidth(String width) {
        wrappingPanel.setWidth(width);
    }

    @Override
    public void setSelectButtonFocus(boolean focused) {
        selected.setFocus(focused);
    }

    @Override
    public void clearFilter() {
        filter.setText("");
    }

    /**
     * Установить выбранными элементы по идентификаторам.
     */
    private void setValueById(List<Integer> itemsIdToSelect) {
        tree.setValueById(itemsIdToSelect, false);
        valueDereference.clear();
        for (DepartmentPair item : tree.getValue()) {
            valueDereference.add(item.getDepartmentName());
        }
        countItems.setText(String.valueOf(valueDereference.size()));
        ok.setEnabled(valueDereference.size() > 0);
    }

    /**
     * Получить выбранные подразделения.
     */
    public List<DepartmentPair> getDepartmentPairValues() {
        return tree.getValue();
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
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<Integer>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }
}
package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

import com.aplana.gwt.client.DoubleStateComposite;
import com.aplana.gwt.client.ModalWindow;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.modal.OpenModalWindowEvent;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.web.widget.utils.TextUtils;
import com.aplana.sbrf.taxaccounting.web.widget.utils.WidgetUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
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

    @UiField
    Label countItems;

    @UiField
    CheckBox exactSearch;

    @UiField
    CheckBox pickAll;

    @UiField
    CheckBox showDisabled;

    private boolean multiSelect;
    private boolean isSetDefaultValue = false;
    private Integer userDepartmentId;

    /* Значения id  */
    private List<Integer> value = new LinkedList<Integer>();
    /* Разименованные значения.   */
    private List<String> valueDereference = new LinkedList<String>();

    @UiConstructor
    public DepartmentPickerPopupWidget(boolean multiselection) {
        initWidget(uiBinder.createAndBindUi(this));

        this.multiSelect = multiselection;

        tree.setMultiSelection(multiselection);
        selectChild.setVisible(multiselection);
        pickAll.setVisible(multiselection);
        itemsInfoPanel.setVisible(multiselection);

        WidgetUtils.setMouseBehavior(clearButton, selected, selectButton);

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

        filter.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    find();
                }
            }
        });

        selected.addDoubleClickHandler(new DoubleClickHandler() {
            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                open();
            }
        });

        showDisabled.setHTML("Отображать " + DepartmentTreeWidget.RED_STAR_SPAN + "недействующие подразделения");
        showDisabled.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                tree.setShowDisabledDepartment(event.getValue());
            }
        });
    }

    @UiHandler("selectButton")
    void onSelectButtonClicked(ClickEvent event) {
        open();
    }

    private void open(){
        if (isSetDefaultValue && (value == null || value.isEmpty())) {
            if (tree.getAvalibleValues().contains(userDepartmentId)) {
                tree.setValueById(Arrays.asList(userDepartmentId), false);
                ok.setEnabled(true);
            }
        } else {
            tree.setValueById(value, false);
        }
        countItems.setText(String.valueOf(tree.getValue().size()));
        popupPanel.center();
    }

    private HandlerRegistration handlerRegistration;

    public void addOpenModalWindowHandler(OpenModalWindowEvent.OpenHandler handler) {
        handlerRegistration = popupPanel.addOpenModalWindowHandler(handler);
    }

    public void removeOpenModalWindowHandler() {
        handlerRegistration.removeHandler();
        handlerRegistration = null;
    }

    @UiHandler("clearButton")
    void onClearButtonClicked(ClickEvent event) {
        pickAll.setValue(false);
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
        find();
    }

    @UiHandler("cancel")
    void onCancelButtonClicked(ClickEvent event) {
        popupPanel.hide();
        setValue(new LinkedList<Integer>(value));
    }

    @UiHandler("selectChild")
    void onSelectChildValueChange(ValueChangeEvent<Boolean> event) {
        tree.setSelectChild(selectChild.getValue());
    }

    @UiHandler("pickAll")
    void onPickAllValueChange(ValueChangeEvent<Boolean> event) {
        if(!event.getValue()){
            tree.unselectAll();
        } else {
            tree.selectAll();
        }
    }

    @UiHandler("exactSearch")
    void onExactSearchValueChange(ValueChangeEvent<Boolean> event) {
        tree.setExactSearch(exactSearch.getValue());
    }

    private void find(){
        tree.filter(filter.getText().trim(), exactSearch.getValue());
        if (tree.getValue().size() >= 100) {
            Dialog.warningMessage("Уточните параметры поиска: найдено слишком много значений");
        } else {
            tree.setShowDisabledDepartment(showDisabled.getValue());
        }
    }

    @Override
    public Integer getSingleValue() {
        if (getValue() != null) {
            Iterator<Integer> depPickIterator = getValue().iterator();
            if (depPickIterator.hasNext()) {
                return depPickIterator.next();
            }
        }
        return null;
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

    @Override
    public String getText() {
        return selected.getText();
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
        ok.setEnabled(!valueDereference.isEmpty());
    }

    /**
     * Получить выбранные подразделения.
     */
    public List<DepartmentPair> getDepartmentPairValues() {
        return tree.getValue();
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<Integer>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public void setUserDepartmentId(Integer userDepartmentId) {
        this.userDepartmentId = userDepartmentId;
    }

    public void setSetDefaultValue(boolean isSetDefaultValue) {
        this.isSetDefaultValue = isSetDefaultValue;
    }
}
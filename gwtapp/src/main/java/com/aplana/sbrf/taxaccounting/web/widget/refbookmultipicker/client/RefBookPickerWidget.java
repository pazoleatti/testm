package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import java.util.*;

import com.aplana.gwt.client.DoubleStateComposite;
import com.aplana.gwt.client.ModalWindow;
import com.aplana.gwt.client.modal.CanHide;
import com.aplana.gwt.client.modal.OnHideHandler;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.PickerState;
import com.aplana.sbrf.taxaccounting.web.widget.utils.TextUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.CalendarUtil;

/**
 * Версионный справочник с выбором значения из линейного или иерархичного опредставления
 *
 * @author Dmitriy Levykin
 */
public class RefBookPickerWidget extends DoubleStateComposite implements RefBookPicker {

    interface Binder extends UiBinder<Widget, RefBookPickerWidget> {
    }

    private static Binder binder = GWT.create(Binder.class);

    @UiField
    TextBox textBox;
    @UiField
    Image pickImageButton;

    @UiField
    HorizontalPanel widgetPanel;
    @UiField
    ModalWindow modalPanel;
    @UiField
    HTMLPanel widgetWrapper;

    @UiField
    Button searchButton;
    @UiField
    Button clearButton;
    @UiField
    Button pickButton;
    @UiField
    Button cancelButton;
    @UiField
    DateMaskBoxPicker versionDateBox;
    @UiField
    TextBox searchTextBox;
    @UiField
    Label selectionCountLabel;
    @UiField
    HorizontalPanel filterPanel;

    /* Вьюха которая принимает параметры, загружает и отображает записи из справочника */
    private RefBookView refBookView;

    /* Даты ограничевающего периода */
    private Date startDate;
    private Date endDate;

    /* Состояние виджета которые было перед нажатием кнопки "Выбрать"*/
    private PickerState prevState = new PickerState();
    /* Текущее состояние виджета */
    private PickerState state = new PickerState();

    /* Признак иерархичности справочника */
    private boolean isHierarchical = false;
    /* Признак - пробрасывать ли изменения из вьюхи */
    private boolean isEnabledFireChangeEvent = false;
    /* Флаг ручного выставления разименнованного значения виджета */
    private boolean isManualUpdate = false;

    @UiConstructor
    public RefBookPickerWidget(boolean isHierarchical, boolean multiSelect) {
        this.isHierarchical = isHierarchical;
        state.setMultiSelect(multiSelect);

        initWidget(binder.createAndBindUi(this));

        refBookView = isHierarchical ? new RefBookTreePickerView(multiSelect) : new RefBookMultiPickerView(multiSelect);

        widgetWrapper.add(refBookView);

        versionDateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(final ValueChangeEvent<Date> dateValueChangeEvent) {
                Date d = dateValueChangeEvent.getValue();
                if (RefBookPickerUtils.isCorrectDate(startDate, endDate, d)) {
                    versionDateBox.setValue(startDate, false);
                }
                state.setVersionDate(versionDateBox.getValue());
                refBookView.reloadOnDate(versionDateBox.getValue());
            }
        });
        versionDateBox.getDatePicker().addShowRangeHandler(new ShowRangeHandler<Date>() {
            @Override
            public void onShowRange(final ShowRangeEvent<Date> dateShowRangeEvent) {
                Date d = new Date(dateShowRangeEvent.getStart().getTime());
                while (d.before(dateShowRangeEvent.getEnd())) {
                    if (RefBookPickerUtils.isCorrectDate(startDate, endDate, d)) {
                        versionDateBox.getDatePicker().setTransientEnabledOnDates(false, d);
                    }
                    CalendarUtil.addDaysToDate(d, 1);
                }
            }
        });

        refBookView.addValueChangeHandler(new ValueChangeHandler<Set<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Set<Long>> event) {
                selectionCountLabel.setText("Выбрано: " + event.getValue().size());
                if (isEnabledFireChangeEvent) {
                    isEnabledFireChangeEvent = false;
                    clearAndSetValues(event.getValue());
                    updateUIState();
                    ValueChangeEvent.fire(RefBookPickerWidget.this, state.getSetIds());
                }
            }
        });

        searchTextBox.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    refBookView.find(searchTextBox.getText());
                    state.setSearchPattern(searchTextBox.getText());
                }
            }
        });
        // оставлю для примера
//        modalPanel.setOnHideHandler(new OnHideHandler<CanHide>() {
//            @Override
//            public void OnHide(CanHide modalWindow) {
//                Dialog.confirmMessage("Закрыть окно выбора из справочника",
//                        "Закрытие окна приведет к отмене выбора. Продолжить?",
//                        new DialogHandler() {
//                            @Override
//                            public void yes() {
//                                cancelPick();
//                                super.yes();
//                            }
//
//                            @Override
//                            public void no() {
//                                super.no();
//                            }
//                        });
//            }
//        });
    }

    @UiHandler("pickImageButton")
    void onSelectButtonClicked(ClickEvent event) {
        prevState.setValues(state);
        refBookView.load(state);
        modalPanel.center();
    }

    @UiHandler("searchButton")
    void onSearchButtonClicked(ClickEvent event) {
        refBookView.find(searchTextBox.getText());
    }

    @UiHandler("clearButton")
    void onClearButtonClicked(ClickEvent event) {
        state.getSetIds().clear();
        prevState.setValues(state);

        isEnabledFireChangeEvent = true;
        refBookView.load(state);
        modalPanel.hide();
    }

    @UiHandler("pickButton")
    void onPickButtonClicked(ClickEvent event) {
        save();
    }

    @UiHandler("cancelButton")
    void onCancelButtonClicked(ClickEvent event) {
        cancelPick();
    }

    private void cancelPick() {
        state.setValues(prevState);
        refBookView.load(state);
        searchTextBox.setText(state.getSearchPattern());
        versionDateBox.setValue(state.getVersionDate());
        modalPanel.hide();
    }

    private void save() {
        clearAndSetValues(refBookView.getSelectedIds());
        prevState.setValues(state);
        updateUIState();
        ValueChangeEvent.fire(RefBookPickerWidget.this, state.getSetIds());
        modalPanel.hide();
    }

    private void clearAndSetValues(Collection<Long> longs) {
        state.setSetIds(new LinkedList<Long>(longs));
    }

    @Override
    public void setMultiSelect(boolean multiSelect) {
        state.setMultiSelect(multiSelect);
        isEnabledFireChangeEvent = true;
        refBookView.setMultiSelect(multiSelect);
    }

    @Override
    public Boolean getMultiSelect() {
        return state.isMultiSelect();
    }

    @Override
    public List<Long> getValue() {
        return state.getSetIds();
    }

    @Override
    public void setValue(List<Long> value) {
        setValue(value, false);
    }

    @Override
    public Long getSingleValue() {
        return state.getSetIds() != null ? state.getSetIds().iterator().next() : null;
    }

    @Override
    public void setSingleValue(Long value, boolean fireEvents) {
        if (value == null) {
            state.setSetIds(null);
            prevState.setValues(state);
            if (!isManualUpdate) {
                refBookView.load(state);
                updateUIState();
            }
            if (fireEvents) {
                ValueChangeEvent.fire(this, null);
            }
        } else {
            setValue(Arrays.asList(value), fireEvents);
        }
    }

    @Override
    public void setSingleValue(Long value) {
        setSingleValue(value, false);
    }

    @Override
    public void setValue(List<Long> value, boolean fireEvents) {
        prevState.setValues(state);
        if (value == null) {
            state.setSetIds(null);
            prevState.setValues(state);
            if (!isManualUpdate) {
                refBookView.load(state);
                updateUIState();
            }
            if (fireEvents) {
                ValueChangeEvent.fire(this, null);
            }
        } else {
            isEnabledFireChangeEvent = true;
            clearAndSetValues(value);
            if (!isManualUpdate) {
                refBookView.load(state);
            }
        }
    }

    @Override
    public void open() {
        modalPanel.center();
    }

    @Override
    public void load(long attributeId, String filter, Date startDate, Date endDate) {
        setAttributeId(attributeId);
        setFilter(filter);
        setPeriodDates(startDate, endDate);
        refBookView.load(state);
    }

    @Override
    public void load() {
        refBookView.load(state);
    }

    @Override
    public String getOtherDereferenceValue(String alias) {
        return refBookView.getOtherDereferenceValue(alias);
    }

    @Override
    public String getOtherDereferenceValue(Long attrId) {
        return refBookView.getOtherDereferenceValue(attrId);
    }

    private void updateUIState() {
        String defValue = "";
        if (state.getSetIds() != null) {
            defValue = refBookView.getDereferenceValue();
        }
        textBox.setText(defValue);
        textBox.setTitle(TextUtils.generateTextBoxTitle(defValue));
        updateLabelValue();
    }

    @Override
    public String getDereferenceValue() {
        return textBox.getValue();
    }

    @Override
    public void setDereferenceValue(String value) {
        textBox.setValue(value);
        setLabelValue(value);
    }

    @Override
    public Long getAttributeId() {
        return state.getRefBookAttrId();
    }

    @Override
    public void setAttributeId(long attributeId) {
        state.setRefBookAttrId(attributeId);
    }

    /*Для совместимости с UiBinder */
    public void setAttributeIdInt(int attributeId) {
        state.setRefBookAttrId((long) attributeId);
    }

    @Override
    public String getFilter() {
        return state.getFilter();
    }

    @Override
    public void setFilter(String filter) {
        state.setFilter(filter);
    }

    @Override
    public void setPeriodDates(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        state.setVersionDate(endDate != null ? endDate : startDate);
        versionDateBox.setValue(state.getVersionDate());
    }

    @Override
    public boolean getSearchEnabled() {
        return filterPanel.isVisible();
    }

    @Override
    public void setSearchEnabled(boolean isSearchEnabled) {
        filterPanel.setVisible(isSearchEnabled);
    }

    @Override
    public boolean isManualUpdate() {
        return isManualUpdate;
    }

    @Override
    public void setManualUpdate(boolean isManualUpdate) {
        this.isManualUpdate = isManualUpdate;
    }

    @Override
    public void setTitle(String title) {
        if (modalPanel != null) {
            modalPanel.setText(title);
        }
    }

    public boolean isVisible() {
        return widgetPanel.isVisible();
    }

    public void setVisible(boolean visible) {
        widgetPanel.setVisible(visible);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<Long>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public HandlerRegistration addCloseHandler(CloseHandler<ModalWindow> handler) {
        return modalPanel.addHandler(handler, CloseEvent.getType());
    }

    @Override
    public void setOnHideHandler(OnHideHandler<CanHide> hideHandler) {
        if (hideHandler != null) {
            modalPanel.setOnHideHandler(hideHandler);
        }
    }

    protected void updateLabelValue() {
        setLabelValue(getDereferenceValue());
    }

    @Override
    protected void setLabelValue(Object value) {
        String stringValue;
        if (value == null || (value instanceof List && ((List) value).isEmpty())) {
            stringValue = EMPTY_STRING_VALUE;
        } else {
            stringValue = value.toString();
            if (stringValue.trim().isEmpty()) {
                stringValue = EMPTY_STRING_VALUE;
            }
        }
        label.setText(stringValue);
        label.setTitle(stringValue.equals(EMPTY_STRING_VALUE) ? EMPTY_STRING_TITLE : TextUtils.generateTextBoxTitle(stringValue));
    }
}

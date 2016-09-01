package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.gwt.client.DoubleStateComposite;
import com.aplana.gwt.client.ModalWindow;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.modal.CanHide;
import com.aplana.gwt.client.modal.OnHideHandler;
import com.aplana.gwt.client.modal.OpenModalWindowEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.handler.DeferredInvokeHandler;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentTreeWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.event.CheckValuesCountHandler;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.PickerContext;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.PickerState;
import com.aplana.sbrf.taxaccounting.web.widget.style.Tooltip;
import com.aplana.sbrf.taxaccounting.web.widget.utils.TextUtils;
import com.aplana.sbrf.taxaccounting.web.widget.utils.WidgetUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
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
    Image pickImageButton,
            clearIconButton;

    @UiField
    HorizontalPanel widgetPanel;
    @UiField
    ModalWindow modalPanel;
    @UiField
    ResizeLayoutPanel widgetWrapper;

    @UiField
    Button searchButton,
            clearButton,
            pickButton,
            cancelButton;

    @UiField
    DateMaskBoxPicker versionDateBox;
    @UiField
    TextBox searchTextBox;
    @UiField
    Label selectionCountLabel;
    @UiField
    HorizontalPanel filterPanel;
    @UiField
    HorizontalPanel versionPanel;
    @UiField
    HorizontalPanel departmentPanel;
    @UiField
    CheckBox showDisabled;
    @UiField
    CheckBox pickAll;
    @UiField
    CheckBox exactSearch;

    @UiField
    FlowPanel modalBodyWrapper;

    private Tooltip tooltip;

    /* Вьюха которая принимает параметры, загружает и отображает записи из справочника */
    private RefBookView refBookView;

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

    DivElement glass;

    @UiConstructor
    public RefBookPickerWidget(boolean isHierarchical, Boolean multiSelect) {
        this.isHierarchical = isHierarchical;
        state.setMultiSelect(multiSelect != null && multiSelect);

        initWidget(binder.createAndBindUi(this));
        WidgetUtils.setMouseBehavior(clearIconButton, textBox, pickImageButton);
        refBookView = isHierarchical ? new RefBookTreePickerView(multiSelect != null && multiSelect, this) : new RefBookMultiPickerView(multiSelect != null && multiSelect, this);

        widgetWrapper.add(refBookView);

        pickAll.setVisible(multiSelect != null && multiSelect && isHierarchical);

        versionDateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(final ValueChangeEvent<Date> event) {
                state.setVersionDate(versionDateBox.getValue());
                refBookView.reloadOnDate(versionDateBox.getValue());
            }
        });

        refBookView.addValueChangeHandler(new ValueChangeHandler<Set<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Set<Long>> event) {
                selectionCountLabel.setText("Выделено: " + event.getValue().size());
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
                    onSearchButtonClicked(null);
                }
            }
        });

        textBox.addDoubleClickHandler(new DoubleClickHandler() {
            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                prevState.setValues(state);
                modalPanel.center();
            }
        });

        modalPanel.addOpenModalWindowHandler(new OpenModalWindowEvent.OpenHandler() {
            @Override
            public void onOpen(OpenModalWindowEvent event) {
                isEnabledFireChangeEvent = false;
                refBookView.load(state, true);
            }
        });

        tooltip = new Tooltip();
        //установка обработчиков для тестовой строк
        tooltip.addHandlersFor(textBox);
        //установка обработчиков для лейбла который отображается если в режиме прсомотра
        tooltip.addHandlersFor(label);

        glass = Document.get().createDivElement();
        glass.setAttribute("id", "666");

        showDisabled.setHTML("Отображать " + DepartmentTreeWidget.RED_STAR_SPAN + "недействующие подразделения");
        showDisabled.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                refBookView.setShowDisabledDepartment(event.getValue());
            }
        });

        Style style = glass.getStyle();
        style.setProperty("filter", "alpha(opacity=0)");
        style.setBackgroundColor("#ffffff");
        style.setPosition(Style.Position.ABSOLUTE);
        style.setLeft(0, Style.Unit.PX);
        style.setTop(0, Style.Unit.PX);
        style.setRight(0, Style.Unit.PX);
        style.setBottom(0, Style.Unit.PX);
        style.setZIndex(2147483647); // Maximum z-index
    }

    @UiHandler("pickImageButton")
    void onSelectButtonClicked(ClickEvent event) {
        prevState.setValues(state);
        modalPanel.center();
    }

    @UiHandler("searchButton")
    void onSearchButtonClicked(ClickEvent event) {
        final String text = searchTextBox.getText();
        if (text != null && !text.trim().isEmpty()) {
            refBookView.checkCount(text.trim(), versionDateBox.getValue(), state.isExactSearch(), new CheckValuesCountHandler() {
                @Override
                public void onGetValuesCount(Integer count) {
                    if (count != null && count < (isHierarchical() ? 50 : 100)) {
                        state.setSearchPattern(text);
                        refBookView.find(text, state.isExactSearch());
                    } else {
                        Dialog.warningMessage("Уточните параметры поиска: найдено слишком много значений.");
                    }
                }
            });
        } else {
            state.setSearchPattern(text);
            refBookView.find(text, state.isExactSearch());
        }
    }

    @UiHandler("clearIconButton")
    void onClearIconButtonClicked(ClickEvent event) {
        clear();
    }

    @UiHandler("clearButton")
    void onClearButtonClicked(ClickEvent event) {
        clear();
        modalPanel.hide();
    }

    private void clearSelection() {
        if (state.getSetIds() != null) {
            state.getSetIds().clear();
        }
        if (isHierarchical() && getMultiSelect()) {
            // для иерархического мультивыбора галочка "Выбрать все" находится на окне выбора
            pickAll.setValue(false);
        } else {
            // для НЕиерархического мультивыбора галочка "Выбрать все" находится в шапке таблицы
            refBookView.unselectAll(null);
        }
        updateUIState();
    }

    private void clear(){
        clearSelection();
        prevState.setValues(state);

        isEnabledFireChangeEvent = true;
        refBookView.load(state, true);
    }

    @UiHandler("pickButton")
    void onPickButtonClicked(ClickEvent event) {
        clearAndSetValues(refBookView.getSelectedIds());
        prevState.setValues(state);
        updateUIState();
        ValueChangeEvent.fire(RefBookPickerWidget.this, state.getSetIds());
        modalPanel.hide();
    }

    @UiHandler("cancelButton")
    void onCancelButtonClicked(ClickEvent event) {
        cancelPick();
    }

    private void cancelPick() {
        state.setValues(prevState);
        searchTextBox.setText(state.getSearchPattern());
        versionDateBox.setValue(state.getVersionDate());
        modalPanel.hide();
    }

    @UiHandler("pickAll")
    void onPickAllValueChange(ValueChangeEvent<Boolean> event) {
        modalBodyWrapper.getElement().appendChild(glass);

        if (!event.getValue()) {
            refBookView.unselectAll(new DeferredInvokeHandler() {
                @Override
                public void onInvoke() {
                    modalBodyWrapper.getElement().removeChild(glass);
                }
            });
        } else {
            refBookView.selectAll(new DeferredInvokeHandler() {
                @Override
                public void onInvoke() {
                    modalBodyWrapper.getElement().removeChild(glass);
                }
            });
        }
    }

    @UiHandler("exactSearch")
    void onExactSearchValueChange(ValueChangeEvent<Boolean> event) {
        state.setExactSearch(event.getValue());
    }

    private void clearAndSetValues(Collection<Long> longs) {
        state.setSetIds(new LinkedList<Long>(longs));
    }

    @Override
    public void setMultiSelect(boolean multiSelect) {
        state.setMultiSelect(multiSelect);
        isEnabledFireChangeEvent = true;
        refBookView.setMultiSelect(multiSelect);
        pickAll.setVisible(multiSelect);
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
        return ((state.getSetIds() != null) && !state.getSetIds().isEmpty()) ? state.getSetIds().iterator().next() : null;
    }

    @Override
    public void setSingleValue(Long value, boolean fireEvents) {
        setValue(value == null ? null : Arrays.asList(value), fireEvents);
    }

    @Override
    public void setSingleValue(Long value) {
        setSingleValue(value, false);
    }

    @Override
    public void setValue(List<Long> value, boolean fireEvents) {
        prevState.setValues(state);
        if (value == null) {
            isEnabledFireChangeEvent = false;
            state.setSetIds(null);
            prevState.setValues(state);
            clearSearchPattern();
            clearSelection();
            if (!isManualUpdate) {
                refBookView.load(state, false);
                updateUIState();
            }
            if (fireEvents) {
                ValueChangeEvent.fire(this, null);
            }
        } else {
            isEnabledFireChangeEvent = true;
            clearAndSetValues(value);
            clearSearchPattern();
            if (!isManualUpdate) {
                refBookView.load(state, true);
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
        refBookView.load(state, false);
    }

    @Override
    public void load() {
        refBookView.load(state, false);
    }

    @Override
    public void reload() {
        refBookView.reload();
    }

    @Override
    public String getOtherDereferenceValue(Long attrId) {
        return refBookView.getOtherDereferenceValue(attrId);
    }

    @Override
    public String getOtherDereferenceValue(Long attrId, Long attrId2) {
        if (attrId2 != null && attrId2 != 0) {
            return refBookView.getOtherDereferenceValue(attrId, attrId2);
        } else {
            return refBookView.getOtherDereferenceValue(attrId);
        }
    }

    private void clearSearchPattern(){
        state.setSearchPattern(null);
        searchTextBox.setText(null);
    }

    private void updateUIState() {
        String defValue = "";
        String countValue = "Выбрано: 0";
        if (state.getSetIds() != null && !state.getSetIds().isEmpty()) {
            defValue = refBookView.getDereferenceValue();
            countValue = "Выбрано: " + state.getSetIds().size();
        }
        selectionCountLabel.setText(countValue);
        textBox.setText(defValue);
        tooltip.setTextHtml(TextUtils.generateTextBoxHTMLTitle(defValue));
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

    public void setDistinct(boolean isDistinct) {
        state.setDistinct(isDistinct);
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
        versionDateBox.setLimitDates(startDate, endDate);
        state.setVersionDate(startDate == null || endDate == null ? new Date() : endDate);
        versionDateBox.setValue(state.getVersionDate());
    }

    @Override
    public boolean getSearchEnabled() {
        return filterPanel.isVisible();
    }

    @Override
    public boolean getVersionEnabled() {
        return versionPanel.isVisible();
    }

    @Override
    public void setSearchEnabled(boolean isSearchEnabled) {
        filterPanel.setVisible(isSearchEnabled);
        tuneWrapperTopShift(isSearchEnabled, versionPanel.isVisible(), departmentPanel.isVisible());
    }

    @Override
    public void setVersionEnabled(boolean isVersionEnabled) {
        versionPanel.setVisible(isVersionEnabled);
        tuneWrapperTopShift(filterPanel.isVisible(), isVersionEnabled, departmentPanel.isVisible());
    }

    @Override
    public void setDepartmentPanelEnabled(boolean isEnabled) {
        refBookView.setShowDisabledDepartment(false);
        departmentPanel.setVisible(isEnabled);
        tuneWrapperTopShift(filterPanel.isVisible(), versionPanel.isVisible(), isEnabled);
    }

    private void tuneWrapperTopShift(boolean isSearchEnabled, boolean isVersionEnabled, boolean isDepartmentPanelEnabled) {
        int top = isSearchEnabled && isVersionEnabled ? 55 : !isSearchEnabled && !isVersionEnabled ? 5 : 30;
        if (isDepartmentPanelEnabled)
            top += 25;
        widgetWrapper.getElement().getStyle().setTop(top, Style.Unit.PX);
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
    public boolean isHierarchical() {
        return isHierarchical;
    }

    @Override
    public void setTitle(String title) {
        if (modalPanel != null) {
            modalPanel.setText(title);
        }
    }

    @Override
    public boolean isVisible() {
        return widgetPanel.isVisible();
    }

    @Override
    public void setVisible(boolean visible) {
        widgetPanel.setVisible(visible);
    }

    @Override
    public void setSingleColumn(String columnAlias) {
        refBookView.setSingleColumn(columnAlias);
    }

    @Override
    public void showVersionDate(boolean versioned) {
        versionPanel.setVisible(versioned);
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

    @Override
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
        tooltip.setTextHtml(stringValue.equals(EMPTY_STRING_VALUE) ? EMPTY_STRING_TITLE : TextUtils.generateTextBoxHTMLTitle(stringValue));
    }

    public void setPickerContext(PickerContext pickerContext) {
        state.setPickerContext(pickerContext);
    }


}

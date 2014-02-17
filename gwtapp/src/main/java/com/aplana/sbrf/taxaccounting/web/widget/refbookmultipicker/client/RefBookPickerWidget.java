package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.aplana.gwt.client.DoubleStateComposite;
import com.aplana.gwt.client.ModalWindow;
import com.aplana.sbrf.taxaccounting.web.widget.utils.TextUtils;
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
    TextBox text;

    @UiField
    Image selectButton;

    private ModalWindow popupPanel;

    private RefBookView refBookPiker;

    private boolean fireEvents = true;

    /**
     * Признак иерархичности окна
     */
    private boolean isHierarchical;

    @UiConstructor
    public RefBookPickerWidget(boolean isHierarchical, boolean multiSelect) {
        initWidget(binder.createAndBindUi(this));
        this.isHierarchical = isHierarchical;
        popupPanel = new ModalWindow("Выбор значения из справочника");


        // пример внедрения своего датапровайдера
//        List<RefBookItem> refBookItems = new ArrayList<RefBookItem>();
//        for(long i=1; i< 20;i++){
//            List<RefBookRecordDereferenceValue>  values = new ArrayList<RefBookRecordDereferenceValue>();
//            values.add(new RefBookRecordDereferenceValue(835L, "NAME", "NAME"+i));
//            values.add(new RefBookRecordDereferenceValue(836L, "TAX_TYPE", "TAX_TYPE"+i));
//            refBookItems.add(new RefBookItem(i, String.valueOf(i), values));
//        }
//        ListDataProvider<RefBookItem> listDataProvider = new ListDataProvider<RefBookItem>(RefBookPickerUtils.KEY_PROVIDER);
//        listDataProvider.setList(refBookItems);

        refBookPiker = isHierarchical ? new RefBookTreePickerView(multiSelect) : new RefBookMultiPickerView(multiSelect);

        popupPanel.add(refBookPiker);
        refBookPiker.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Long>> event) {
                String defValue = refBookPiker.getDereferenceValue();
                text.setText(defValue);
                text.setTitle(TextUtils.generateTextBoxTitle(defValue));
                updateLabelValue();
                if (fireEvents) {
                    ValueChangeEvent.fire(RefBookPickerWidget.this, event.getValue());
                } else {
                    fireEvents = true;
                }
                popupPanel.hide();
            }
        });
    }

    @UiHandler("selectButton")
    void onSelectButtonClicked(ClickEvent event) {
        refBookPiker.load();
        popupPanel.center();
    }

    @Override
    public List<Long> getValue() {
        return refBookPiker.getValue();
    }

    @Override
    public void setValue(List<Long> value) {
        setValue(value, false);
    }

    @Override
    public void setValue(List<Long> value, boolean fireEvents) {
        this.fireEvents = fireEvents;
        refBookPiker.setValue(value, fireEvents);
    }

    @Override
    public Long getSingleValue() {
        return refBookPiker.getSingleValue();
    }

    @Override
    public void setValue(Long value) {
        setValue(Arrays.asList(value), false);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<Long>> handler) {
        return asWidget().addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public String getDereferenceValue() {
        return text.getValue();
    }

    @Override
    public void setDereferenceValue(String value) {
        text.setValue(value);
        setLabelValue(value);
    }

    @Override
    public Long getAttributeId() {
        return refBookPiker.getAttributeId();
    }

    @Override
    public void setAttributeId(long attributeId) {
        refBookPiker.setAttributeId(attributeId);
    }

    /*Для совместимости с UiBinder */
    public void setAttributeIdInt(int attributeId) {
        refBookPiker.setAttributeId(Long.valueOf(attributeId));
    }

    @Override
    public String getFilter() {
        return refBookPiker.getFilter();
    }

    @Override
    public void setFilter(String filter) {
        refBookPiker.setFilter(filter);
    }

    @Override
    public Date getEndDate() {
        return refBookPiker.getEndDate();
    }

    @Override
    public Date getStartDate() {
        return refBookPiker.getStartDate();
    }

    @Override
    public void setPeriodDates(Date startDate, Date endDate) {
        refBookPiker.setPeriodDates(startDate, endDate);
    }

    @Override
    public void setTitle(String title) {
        if (popupPanel != null) {
            popupPanel.setText(title);
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
        if (stringValue.equals(EMPTY_STRING_VALUE))
            label.setTitle(EMPTY_STRING_TITLE);
        else
            label.setTitle(stringValue);
    }
}

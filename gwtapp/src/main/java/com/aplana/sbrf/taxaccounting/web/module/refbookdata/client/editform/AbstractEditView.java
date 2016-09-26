package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform;

import com.aplana.sbrf.taxaccounting.model.Formats;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception.BadValueException;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookColumn;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookValueSerializable;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.*;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.math.BigDecimal;
import java.util.*;

/**
 * User: avanteev
 */
public abstract class AbstractEditView extends ViewWithUiHandlers<EditFormUiHandlers> implements AbstractEditPresenter.MyView {

    Map<RefBookColumn, HasValue> widgets;
    private Map<String, RefBookValueSerializable> inputRecord;
    private boolean isNeedToReload = false;
    boolean isVersionMode = false;

    private static final String READ_ONLY_FIELD_STYLE = "read-only-field";
    private static final int MAX_STRING_VALUE_LENGTH = 2000;
    public boolean versioned;

    abstract Panel getRootFieldsPanel();

    @Override
    public void cleanFields() {
        for (Map.Entry<RefBookColumn, HasValue> entry : widgets.entrySet()) {
            HasValue widget = entry.getValue();
            widget.setValue(null);
            if (widget instanceof RefBookPickerWidget) {
                if (isNeedToReload) {
                    isNeedToReload = false;
                    ((RefBookPickerWidget) widget).reload();
                }
                ((RefBookPickerWidget) widget).setDereferenceValue("");
            }
        }
    }

    @Override
    public void cleanErrorFields() {
        for (Map.Entry<RefBookColumn, HasValue> widget : widgets.entrySet()){
            if (RefBookAttributeType.STRING.equals(widget.getKey().getAttributeType()) || RefBookAttributeType.NUMBER.equals(widget.getKey().getAttributeType())) {
                Element element = ((Widget) widget.getValue()).getElement().getFirstChildElement().getFirstChildElement();
                if (element!= null && element.getStyle()!=null){
                    ((Widget) widget.getValue()).getElement().getFirstChildElement().getFirstChildElement()
                            .getStyle().setBackgroundColor("");
                }
            }
        }
    }

    @Override
    public Map<RefBookColumn, HasValue> createInputFields(List<RefBookColumn> attributes) {
        Panel editPanel = getRootFieldsPanel();
        editPanel.clear();
        if (widgets != null) widgets.clear();
        Map<RefBookColumn, HasValue> widgets = new LinkedHashMap<RefBookColumn, HasValue>();
        for (final RefBookColumn col : attributes) {

            HorizontalPanel oneField = new HorizontalPanel();
            oneField.setWidth("100%");

            Label label = getArrtibuteLabel(col);
            label.getElement().getStyle().setProperty("lineHeight", "12px");
            label.getElement().getStyle().setProperty("minWidth", "150px");
            label.getElement().getStyle().setProperty("maxWidth", "250px");
            oneField.add(label);
            oneField.setCellWidth(label, "20%");
            oneField.setCellHorizontalAlignment(label, HasHorizontalAlignment.ALIGN_RIGHT);
            oneField.setCellVerticalAlignment(label, HasVerticalAlignment.ALIGN_MIDDLE);

            final Widget widget;
            switch (col.getAttributeType()) {
                case NUMBER:
                    if(Formats.BOOLEAN.equals(col.getFormat())){
                        widget = new CheckBox();
                    } else {
                        widget = new com.aplana.gwt.client.NumberBox(col.getPrecision());
                    }
                    break;
                case STRING:
                    widget = new com.aplana.gwt.client.TextBox();
                    break;
                case DATE:
                    widget = new DateMaskBoxPicker(col.getFormat());
                    if (!col.isRequired()) {
                        ((DateMaskBoxPicker) widget).setCanBeEmpty(true);
                    }
                    break;
                case REFERENCE:
                    RefBookPickerWidget refbookWidget = new RefBookPickerWidget(col.isHierarchical(), false);
                    refbookWidget.setManualUpdate(true);
                    refbookWidget.setAttributeId(col.getRefBookAttributeId());
                    refbookWidget.setTitle(col.getRefBookName());
                    widget = refbookWidget;
                    break;
                default:
                    widget = new com.aplana.gwt.client.TextBox();
                    break;
            }

            ((HasValue)widget).addValueChangeHandler(new ValueChangeHandler() {
                @Override
                public void onValueChange(ValueChangeEvent event) {
                    if (getUiHandlers() != null) {
                        getUiHandlers().valueChanged(col.getAlias(), event.getValue());
                        checkValueChange(col, widget, event.getValue());
                    }
                }
            });

            widget.setWidth("100%");
            // Устанавливаем фиксированную ширину для поля типа DATE
            if (widget instanceof DateMaskBoxPicker) {
                widget.getElement().getStyle().setProperty("width", "110px");
            } else {
                widget.getElement().getStyle().setProperty("minWidth", "100px");
                widget.getElement().getStyle().setProperty("maxWidth", "100%");
            }
            if (col.isReadOnly()) {
                widget.addStyleName(READ_ONLY_FIELD_STYLE);
            }
            oneField.add(widget);
            oneField.setCellWidth(widget, "80%");
            oneField.setCellHorizontalAlignment(widget, HasHorizontalAlignment.ALIGN_LEFT);
            oneField.setCellVerticalAlignment(widget, HasVerticalAlignment.ALIGN_MIDDLE);

            editPanel.add(oneField);
            widgets.put(col, (HasValue)widget);
        }
        this.widgets = widgets;
        //updateRefBookPickerPeriod();
        return widgets;
    }

    void checkValueChange(RefBookColumn col, Widget widget, Object value) {
        switch (col.getAttributeType()) {
            case STRING:
                if (value != null && value.toString().length() > col.getMaxLength()) {
                    widget.getElement().getFirstChildElement().getFirstChildElement()
                            .getStyle().setBackgroundColor("#ffccd2");
                } else {
                    widget.getElement().getFirstChildElement().getFirstChildElement()
                            .getStyle().setBackgroundColor("");
                }
                break;
            case NUMBER:
                if (widget instanceof CheckBox) {
                    return;
                }
                com.aplana.gwt.client.TextBox hasValue = new com.aplana.gwt.client.TextBox();
                hasValue.setText(value.toString());
                try {
                    checkNumber(col, hasValue, true);
                    widget.getElement().getFirstChildElement().getFirstChildElement()
                            .getStyle().setBackgroundColor("");
                } catch (Exception e) {
                    widget.getElement().getFirstChildElement().getFirstChildElement()
                            .getStyle().setBackgroundColor("#ffccd2");
                }
                break;
        }
    }

    /**
     *  Label для input'a редактирования значения справочника
     *  с названием атрибута справочника
     */
    private Label getArrtibuteLabel(RefBookColumn col){
        Label label;
        if (col.isRequired()){
            SafeHtmlBuilder builder = new SafeHtmlBuilder();
            builder.appendHtmlConstant(col.getName() + ":<span class='required'>*</span>");
            label = new HTML(builder.toSafeHtml());
        } else{
            label = new Label(col.getName()+":");
        }
        label.addStyleName("inputLabel");

        return label;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void fillInputFields(Map<String, RefBookValueSerializable> record) {
        inputRecord = record;
        if (record==null)
            return;
        boolean textFieldFound = false;
        for (Map.Entry<RefBookColumn, HasValue> w : widgets.entrySet()) {
            //Первый по порядку текстовый атрибут справочника принимает значение "Новая запись" (если текстовые атрибуты отсутствуют, то шаг не выполняется)
            if (record.containsKey(NEW_RECORD_ALIAS) && w.getValue() instanceof HasText && !(w.getValue() instanceof CheckBox)
                    && w.getKey().getAttributeType() == RefBookAttributeType.STRING && !textFieldFound) {
                textFieldFound = true;
                w.getValue().setValue(record.get(NEW_RECORD_ALIAS).getStringValue());
                continue;
            }
            RefBookValueSerializable recordValue = record.get(w.getKey().getAlias());
            if (recordValue == null)
                continue;
            if (w.getValue() instanceof RefBookPickerWidget) {
                RefBookPickerWidget rbw = (RefBookPickerWidget) w.getValue();
                if (isNeedToReload) {
                    isNeedToReload = false;
                    rbw.reload();
                }
                rbw.setDereferenceValue(recordValue.getDereferenceValue());
                rbw.setSingleValue(recordValue.getReferenceValue());
            } else if(w.getValue() instanceof HasText) {
                if (w.getKey().getAttributeType() == RefBookAttributeType.NUMBER) {
                    if (w.getValue() instanceof CheckBox) {
                        if(recordValue.getValue() == null){
                            w.getValue().setValue(false);
                        } else if(BigDecimal.ZERO.equals(recordValue.getValue())){
                            w.getValue().setValue(false);
                        } else {
                            w.getValue().setValue(true);
                        }
                    } else {
                        w.getValue().setValue((recordValue.getValue()) == null ? ""
                                : ((BigDecimal) recordValue.getValue()).toPlainString());
                    }
                } else {
                    w.getValue().setValue(recordValue.getValue());
                }
            } else {
                w.getValue().setValue(recordValue.getValue());
            }
        }
        updateRefBookPickerPeriod();
    }

    @Override
    public boolean checkChanges() {
        if (inputRecord == null) return false;
        Map<String, RefBookValueSerializable> map;
        try {
            map = getFieldsValues(false);
        } catch (BadValueException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
        for (Map.Entry<RefBookColumn, HasValue> w : widgets.entrySet()) {
            RefBookValueSerializable recordValue = inputRecord.get(w.getKey().getAlias());
            RefBookValueSerializable mapValue = map.get(w.getKey().getAlias());
            if (recordValue == null) {
                if (mapValue == null)
                    continue;
                else
                    return true;
            }
            if (mapValue == null) {
                return true;
            } else if (recordValue.getValue() != null && mapValue.getValue() != null) {
                if (!(recordValue.getValue() instanceof String && equalsCleanStrings(recordValue.getValue().toString(), mapValue.getValue().toString()) ||
                        recordValue.getValue() instanceof BigDecimal && mapValue.getValue() != null && ((BigDecimal)recordValue.getValue()).compareTo((BigDecimal)mapValue.getValue()) == 0 ||
                        recordValue.getValue().equals(mapValue.getValue()))) {
                    return true;
                }
            } else if (mapValue.getValue() != null) {
                return true;
            }

        }
        if (isVersionDatesChanged()) {
            return true;
        }

        return false;
    }

    protected boolean isVersionDatesChanged() {
        return false;
    }

    @Override
    public void updateInputFields() {
        try {
            inputRecord = getFieldsValues(false);
        } catch (BadValueException e) {
            //Nothing
        }
    }

    @Override
    public Map<String, RefBookValueSerializable> getFieldsValues() throws BadValueException {
        return getFieldsValues(true);
    }

    private BigDecimal checkNumber(RefBookColumn key, HasValue value, boolean checkRequired)  throws BadValueException{
        return checkNumber(key, value, checkRequired, new HashMap<String, String>(0));
    }

    private BigDecimal checkNumber(RefBookColumn key, HasValue value, boolean checkRequired, Map<String, String> errorMap)  throws BadValueException{
        BigDecimal number;
        if (value instanceof CheckBox) {
            number = value.getValue() == null ?
                    null :
                    (Boolean) value.getValue() ?
                            BigDecimal.ONE : BigDecimal.ZERO;
        } else {
            if (value.getValue() != null && !value.getValue().toString().trim().isEmpty()) {
                String valStr = (String) value.getValue();
                double val = Double.valueOf(valStr);
                if (val < 0){
                    throw new NumberFormatException();
                }
                number = new BigDecimal(valStr);
                valStr = (number).toPlainString();
                if (valStr.contains(".")) {
                    number = (new BigDecimal(valStr.replaceAll("()(0+)(e|$)", "$1$3"))).setScale(key.getPrecision(), BigDecimal.ROUND_HALF_UP);
                }
            } else {
                number = null;
            }
        }
        if (checkRequired) checkRequired(key, number, errorMap);
        if (number != null) {
            int fractionalPart = number.scale();
            int integerPart = number.precision();
            integerPart = fractionalPart < integerPart ? (integerPart - fractionalPart) : 0;
            fractionalPart = fractionalPart < 0 ? 0 : fractionalPart;

            Integer maxLength = key.getMaxLength();
            Integer precision = key.getPrecision();

            // пердпологается, что (maxLength - precision) <= 17
            if (fractionalPart > precision || integerPart > (maxLength - precision)) {
                throw new BadValueException(Collections.singletonMap(key.getName(), "значение не соответствует формату. Максимальное количество цифр = " + maxLength + ", максимальная точность = " + precision));
            }
        }
        return number;
    }

    protected void updateRefBookPeriod(Date versionStart, Date versionEnd) {
        for (Map.Entry<RefBookColumn, HasValue> field : widgets.entrySet()) {
            if (field.getKey().getAttributeType() == RefBookAttributeType.REFERENCE) {
                RefBookPickerWidget widget = (RefBookPickerWidget) field.getValue();
                widget.setPeriodDates(versionStart, versionEnd);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, RefBookValueSerializable> getFieldsValues(boolean checkRequired) throws BadValueException {
        Map<String, RefBookValueSerializable> fieldsValues = new HashMap<String, RefBookValueSerializable>();
        Map<String, String> errorMap = new LinkedHashMap<String, String>();
        for (Map.Entry<RefBookColumn, HasValue> field : widgets.entrySet()) {
            RefBookValueSerializable value = new RefBookValueSerializable();
            try {
                switch (field.getKey().getAttributeType()) {
                    case NUMBER:
                        value.setAttributeType(RefBookAttributeType.NUMBER);
                        value.setNumberValue(checkNumber(field.getKey(), field.getValue(), checkRequired, errorMap));
                        break;
                    case STRING:
                        String string = (field.getValue().getValue() == null || ((String)field.getValue().getValue()).trim().isEmpty()) ?
                                null : (String)field.getValue().getValue();
                        if (checkRequired) checkRequired(field.getKey(), string, errorMap);
                        Integer maxLength = field.getKey().getMaxLength();
                        if (maxLength == null) maxLength = MAX_STRING_VALUE_LENGTH;
                        if (string!= null && string.length() > maxLength) {
                            errorMap.put(field.getKey().getName(), "количество символов превышает максимально допустимое = " + maxLength);
                        }
                        value.setAttributeType(RefBookAttributeType.STRING);
                        value.setStringValue(string);
                        break;
                    case DATE:
                        Date date = field.getValue().getValue() == null ? null : (Date)field.getValue().getValue();
                        if (checkRequired) checkRequired(field.getKey(), date, errorMap);
                        value.setAttributeType(RefBookAttributeType.DATE);
                        value.setDateValue(date);
                        break;
                    case REFERENCE:
                        Long longValue = (field.getValue().getValue() == null || ((List<Long>) field.getValue().getValue()).isEmpty()) ? null : ((List<Long>)field.getValue().getValue()).get(0);
                        if (checkRequired) checkRequired(field.getKey(), longValue, errorMap);
                        value.setAttributeType(RefBookAttributeType.REFERENCE);
                        value.setReferenceValue(longValue);
                        value.setDereferenceValue(((RefBookPickerWidget)field.getValue()).getDereferenceValue());
                        break;
                    default:
                        break;
                }
                fieldsValues.put(field.getKey().getAlias(), value);
            } catch (NumberFormatException nfe) {
                errorMap.put(field.getKey().getName(), "значение некорректно. Неправильный формат числа");
                /*BadValueException badValueException = new BadValueException();
                badValueException.setFieldName(field.getKey().getName());
                badValueException.setDescription("значение некорректно. Неправильный формат числа");
                throw badValueException;*/
            } catch (ClassCastException cce) {
                errorMap.put(field.getKey().getName(), "значение некорректно");
                /*BadValueException badValueException = new BadValueException();
                badValueException.setFieldName(field.getKey().getName());
                badValueException.setDescription("значение некорректно");
                throw badValueException;*/
            }
        }
        if (!errorMap.isEmpty())
            throw new BadValueException(errorMap);
        return fieldsValues;
    }

    private void checkRequired(RefBookColumn attr, Object val, Map<String, String> errorMap) throws BadValueException {
        if (attr.isRequired() && (val == null)) {
            errorMap.put(attr.getName(), "обязателен для заполнения");
            /*BadValueException badValueException = new BadValueException();
            badValueException.setFieldName(attr.getName());
            badValueException.setDescription("обязателен для заполнения");
            throw badValueException;*/
        }
    }

    void updateWidgetsVisibility(boolean enabled) {
        if (widgets != null){
            for (HasValue entry : widgets.values()) {
                if (entry instanceof HasEnabled) {
                    boolean readonly = ((UIObject) entry).getStyleName().contains(READ_ONLY_FIELD_STYLE);
                    ((HasEnabled) entry).setEnabled(!readonly && enabled);
                }
            }
        }
    }

    @Override
    public void setNeedToReload(boolean b) {
        isNeedToReload = b;
    }

    private boolean equalsCleanStrings(String first, String second) {
        return first.equals(second);
    }

    @Override
    public void setVersionMode(boolean versionMode) {
        isVersionMode = versionMode;
    }

    @Override
    public boolean isVersionMode() {
        return isVersionMode;
    }

    @Override
    public HasClickHandlers getClickAllVersion() {
        return null;
    }
}

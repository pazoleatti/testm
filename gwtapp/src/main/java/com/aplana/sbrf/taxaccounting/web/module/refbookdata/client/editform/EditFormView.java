package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform;

import com.aplana.gwt.client.TextBox;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.Formats;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.FormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception.BadValueException;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookColumn;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookRecordVersionData;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookValueSerializable;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditFormView extends ViewWithUiHandlers<EditFormUiHandlers> implements EditFormPresenter.MyView{

	/** Маскимальная длина для строковых значений у справочников */
	private static final int MAX_STRING_VALUE_LENGTH = 2000;

    private static final String READ_ONLY_FIELD_STYLE = "read-only-field";

    interface Binder extends UiBinder<Widget, EditFormView> { }

	Map<RefBookColumn, HasValue> widgets;

	@UiField
	VerticalPanel editPanel;
	@UiField
	Button save;
	@UiField
	Button cancel;

    @UiField
    DateMaskBoxPicker versionStart;
    @UiField
    DateMaskBoxPicker versionEnd;
    @UiField
    LinkAnchor allVersion;
    @UiField
    HorizontalPanel buttonBlock;
    @UiField
    Label startVersionDateLabel;
    @UiField
    Label endVersionDateLabel;

    private boolean isVersionMode = false;
    private boolean canVersion = true;
    private boolean isNeedToReload = false;

    private Map<String, RefBookValueSerializable> inputRecord;

	@Inject
	@UiConstructor
	public EditFormView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));

        versionStart.setStartLimitDate(new Date(0));//01.01.1970
        versionStart.setEndLimitDate(new Date(4133894400000L));//31.12.2100
        versionStart.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                if (versionEnd.getValue() != null && event.getValue().after(versionEnd.getValue())) {
                    Dialog.errorMessage("Неправильно указан диапазон дат!");
                    save.setEnabled(false);
                    cancel.setEnabled(false);
                }else if (event.getValue() == null){
                    Dialog.errorMessage("Введите дату начала!");
                    save.setEnabled(false);
                    cancel.setEnabled(false);
                } else {
                    save.setEnabled(true);
                    cancel.setEnabled(true);
                }
            }
        });
        versionEnd.setStartLimitDate(new Date(0));//01.01.1970
        versionEnd.setEndLimitDate(new Date(4133894400000L));//31.12.2100
        versionEnd.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                if (versionStart.getValue() != null && event.getValue() != null && event.getValue().before(versionStart.getValue())) {
                    Dialog.errorMessage("Неправильно указан диапазон дат!");
                    save.setEnabled(false);
                    cancel.setEnabled(false);
                } else if (versionStart.getValue() == null){
                    Dialog.errorMessage("Введите дату начала!");
                    save.setEnabled(false);
                    cancel.setEnabled(false);
                } else {
                    save.setEnabled(true);
                    cancel.setEnabled(true);
                }
            }
        });
        versionStart.setCanBeEmpty(true);
        versionStart.setCanBeEmpty(true);
	}

    @Override
    public void updateRefBookPickerPeriod() {
        if(canVersion){
            Date start = versionStart.getValue();
            if (start == null) {
                start = new Date();
            }

            for (Map.Entry<RefBookColumn, HasValue> w : widgets.entrySet()) {
                if (w.getValue() instanceof RefBookPickerWidget) {
                    RefBookPickerWidget rbw = (RefBookPickerWidget) w.getValue();
                    rbw.setPeriodDates(start, versionEnd.getValue());
                }
            }
        } else {
            for (Map.Entry<RefBookColumn, HasValue> w : widgets.entrySet()) {
                if (w.getValue() instanceof RefBookPickerWidget) {
                    RefBookPickerWidget rbw = (RefBookPickerWidget) w.getValue();
                    rbw.setPeriodDates(null, null);
                }
            }
        }
    }

    @Override
    public void setVisibleFields(boolean canVersion) {
        this.canVersion = canVersion;
        versionStart.setVisible(canVersion);
        versionEnd.setVisible(canVersion);
        startVersionDateLabel.setVisible(canVersion);
        endVersionDateLabel.setVisible(canVersion);
        allVersion.setVisible(canVersion);
    }

    @Override
    public void setAllVersionField(boolean isVisible) {
        allVersion.setVisible(isVisible);
    }

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
		editPanel.clear();
		if (widgets != null) widgets.clear();
		Map<RefBookColumn, HasValue> widgets = new HashMap<RefBookColumn, HasValue>();
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
                        widget = new com.aplana.gwt.client.TextBox();
                    }
					break;
				case STRING:
					widget = new com.aplana.gwt.client.TextBox();
					break;
				case DATE:
					widget = new DateMaskBoxPicker(col.getFormat());
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
        updateRefBookPickerPeriod();
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
                TextBox hasValue = new TextBox();
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
		if (record == null) {
            boolean textFieldFound = false;

			for (Map.Entry<RefBookColumn, HasValue> entry : widgets.entrySet()) {
                HasValue widget = entry.getValue();
                RefBookColumn column = entry.getKey();
                widget.setValue(null);
                if (widget instanceof RefBookPickerWidget) {
                    if (isNeedToReload) {
                        isNeedToReload = false;
                        ((RefBookPickerWidget) widget).reload();
                    }
                    ((RefBookPickerWidget) widget).setDereferenceValue("");
                }
                //Первый по порядку текстовый атрибут справочника принимает значение "Новая запись" (если текстовые атрибуты отсутствуют, то шаг не выполняется)
                if (column.getAttributeType() == RefBookAttributeType.STRING && !textFieldFound){
                    textFieldFound = true;
                    ((TextBox) widget).setValue("Новая запись");
                }
			}
		} else {
			for (Map.Entry<RefBookColumn, HasValue> w : widgets.entrySet()) {
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
            } else if (recordValue.getValue() != null) {
                if (!(recordValue.getValue() instanceof String && equalsCleanStrings(recordValue.getValue().toString(), mapValue.getValue().toString()) ||
                        recordValue.getValue() instanceof BigDecimal && mapValue.getValue() != null && ((BigDecimal)recordValue.getValue()).compareTo((BigDecimal)mapValue.getValue()) == 0 ||
                        recordValue.getValue().equals(mapValue.getValue()))) {
                    return true;
                }
            } else if (mapValue.getValue() != null) {
                return true;
            }

        }

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
        BigDecimal number;
        if (value instanceof CheckBox) {
            number = value.getValue() == null ?
                    null :
                    (Boolean) value.getValue() ?
                            BigDecimal.ONE : BigDecimal.ZERO;
        } else {
            if (value.getValue() != null && !value.getValue().toString().trim().isEmpty()) {
                String valStr = (String) value.getValue();
                number = new BigDecimal(valStr);
                valStr = (number).toPlainString();
                if (valStr.contains(".")) {
                    number = new BigDecimal(valStr.replaceAll("()(0+)(e|$)", "$1$3"));
                }
            } else {
                number = null;
            }
        }
        if (checkRequired) checkRequired(key, number);
        if (number != null) {
            int fractionalPart = number.scale();
            int integerPart = number.precision();
            integerPart = fractionalPart < integerPart ? (integerPart - fractionalPart) : 0;
            fractionalPart = fractionalPart < 0 ? 0 : fractionalPart;

            Integer maxLength = key.getMaxLength();
            Integer precision = key.getPrecision();

            // пердпологается, что (maxLength - precision) <= 17
            if (fractionalPart > precision || integerPart > (maxLength - precision)) {
                BadValueException badValueException = new BadValueException();
                badValueException.setFieldName(key.getName());
                badValueException.setDescription("значение не соответствует формату. Максимальное количество цифр = " + maxLength + ", максимальная точность = " + precision);
                throw badValueException;
            }
        }
        return number;
    }

    @SuppressWarnings("unchecked")
    private Map<String, RefBookValueSerializable> getFieldsValues(boolean checkRequired) throws BadValueException {
		Map<String, RefBookValueSerializable> fieldsValues = new HashMap<String, RefBookValueSerializable>();
		for (Map.Entry<RefBookColumn, HasValue> field : widgets.entrySet()) {
			RefBookValueSerializable value = new RefBookValueSerializable();
			try {
                switch (field.getKey().getAttributeType()) {
					case NUMBER:
						value.setAttributeType(RefBookAttributeType.NUMBER);
						value.setNumberValue(checkNumber(field.getKey(), field.getValue(), checkRequired));
						break;
					case STRING:
						String string = (field.getValue().getValue() == null || ((String)field.getValue().getValue()).trim().isEmpty()) ?
								null : (String)field.getValue().getValue();
						if (checkRequired) checkRequired(field.getKey(), string);
                        Integer maxLength = field.getKey().getMaxLength();
                        if (maxLength == null) maxLength = MAX_STRING_VALUE_LENGTH;
						if (string!= null && string.length() > maxLength) {
							BadValueException badValueException = new BadValueException();
							badValueException.setFieldName(field.getKey().getName());
							badValueException.setDescription("количество символов превышает максимально допустимое = " + maxLength);
							throw badValueException;
						}
						value.setAttributeType(RefBookAttributeType.STRING);
						value.setStringValue(StringUtils.cleanString(string));
						break;
					case DATE:
						Date date = field.getValue().getValue() == null ? null : (Date)field.getValue().getValue();
                        if (checkRequired) checkRequired(field.getKey(), date);
                        value.setAttributeType(RefBookAttributeType.DATE);
						value.setDateValue(date);
						break;
					case REFERENCE:
                        Long longValue = (field.getValue().getValue() == null || ((List<Long>) field.getValue().getValue()).isEmpty()) ? null : ((List<Long>)field.getValue().getValue()).get(0);
                        if (checkRequired) checkRequired(field.getKey(), longValue);
                        value.setAttributeType(RefBookAttributeType.REFERENCE);
						value.setReferenceValue(longValue);
						break;
					default:
						//TODO
						break;
				}
				fieldsValues.put(field.getKey().getAlias(), value);
			} catch (NumberFormatException nfe) {
				BadValueException badValueException = new BadValueException();
				badValueException.setFieldName(field.getKey().getName());
                badValueException.setDescription("значение некорректно. Неправильный формат числа");
				throw badValueException;
			} catch (ClassCastException cce) {
				BadValueException badValueException = new BadValueException();
				badValueException.setFieldName(field.getKey().getName());
                badValueException.setDescription("значение некорректно");
				throw badValueException;
			}
		}
		return fieldsValues;
	}

	private void checkRequired(RefBookColumn attr, Object val) throws BadValueException {
		if (attr.isRequired() && (val == null)) {
			BadValueException badValueException = new BadValueException();
			badValueException.setFieldName(attr.getName());
			badValueException.setDescription("обязателен для заполнения");
			throw badValueException;
		}
	}

	private void updateWidgetsVisibility(boolean enabled) {
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
    public void fillVersionData(RefBookRecordVersionData versionData, Long refBookId, Long refBookRecordId) {
        versionStart.setValue(versionData.getVersionStart());
        versionEnd.setValue(versionData.getVersionEnd());
        allVersion.setVisible(!isVersionMode && canVersion);
        allVersion.setText("Все версии ("+versionData.getVersionCount()+")");
        allVersion.setHref("#"
                + RefBookDataTokens.refBookVersion
                + ";" + RefBookDataTokens.REFBOOK_DATA_ID  + "=" + refBookId
                + ";" + RefBookDataTokens.REFBOOK_RECORD_ID + "=" + refBookRecordId);
    }

    @Override
    public void setVersionMode(boolean versionMode) {
        isVersionMode = versionMode;
        allVersion.setVisible(false);
    }

    @Override
    public Date getVersionFrom() {
        return versionStart.getValue();
    }

    @Override
    public Date getVersionTo() {
        return versionEnd.getValue();
    }

    @Override
    public void setVersionFrom(Date value) {
        versionStart.setValue(value);
    }

    @Override
    public void setVersionTo(Date value) {
        versionEnd.setValue(value);
    }

    @Override
    public void updateMode(FormMode mode) {
        if (mode == null) {
            mode = FormMode.VIEW;
        }
        switch (mode){
            case CREATE:
                save.setEnabled(true);
                cancel.setEnabled(true);
                updateWidgetsVisibility(true);
                versionStart.setEnabled(true);
                versionEnd.setEnabled(true);
                allVersion.setVisible(false);
                break;
            case EDIT:
                save.setEnabled(true);
                cancel.setEnabled(true);
                updateWidgetsVisibility(true);
                versionStart.setEnabled(true);
                versionEnd.setEnabled(true);
                allVersion.setVisible(true);
                break;
            case READ:
            case VIEW:
                save.setEnabled(false);
                cancel.setEnabled(false);
                versionStart.setEnabled(false);
                versionEnd.setEnabled(false);
                updateWidgetsVisibility(false);
                allVersion.setVisible(true);
                break;
        }
    }

    @Override
    public void setNeedToReload(boolean b) {
        isNeedToReload = b;
    }

    @UiHandler("save")
	void saveButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onSaveClicked(false);
		}
	}

	@UiHandler("cancel")
	void cancelButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onCancelClicked();
		}
    }

    private boolean equalsCleanStrings(String first, String second) {
        return StringUtils.cleanString(first).equals(StringUtils.cleanString(second));
    }
}

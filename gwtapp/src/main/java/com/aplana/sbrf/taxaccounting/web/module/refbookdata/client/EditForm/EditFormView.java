package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.Formats;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.exception.BadValueException;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.FormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookColumn;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookRecordVersionData;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookValueSerializable;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import java.util.*;

public class EditFormView extends ViewWithUiHandlers<EditFormUiHandlers> implements EditFormPresenter.MyView{

	/** Маскимальная длина для строковых значений у справочников */
	private static final int MAX_STRING_VALUE_LENGTH = 2000;

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

    private boolean isVersionMode = false;
    private boolean readOnly;
    private boolean isHierarchy = false;
    private boolean isNeedToReload = false;

	@Inject
	@UiConstructor
	public EditFormView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
        allVersion.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // Обновление истории для корретной работы кнопки "Назад" браузера
                getUiHandlers().updateHistory();
            }
        });
        versionStart.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                updateRefBookPickerPeriod();
            }
        });
        versionEnd.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                updateRefBookPickerPeriod();
            }
        });
        versionStart.setCanBeEmpty(true);
        versionStart.setCanBeEmpty(true);
	}

    @Override
    public void updateRefBookPickerPeriod() {
        Date start = versionStart.getValue();
        if (start == null) {
            start = new Date();
        }

        if (versionEnd.getValue() != null && start.after(versionEnd.getValue())) {
            Dialog.errorMessage("Неправильно указан диапазон дат!");
            versionEnd.setValue(null);
            return;
        }

        for (Map.Entry<RefBookColumn, HasValue> w : widgets.entrySet()) {
            if (w.getValue() instanceof RefBookPickerWidget) {
                RefBookPickerWidget rbw = (RefBookPickerWidget) w.getValue();
                rbw.setPeriodDates(start, versionEnd.getValue());
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

			Widget widget;
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
                    RefBookPickerWidget refbookWidget = new RefBookPickerWidget(isHierarchy, false);
                    refbookWidget.setManualUpdate(true);
					refbookWidget.setAttributeId(col.getRefBookAttributeId());
                    refbookWidget.setTitle(col.getRefBookName());
					widget = refbookWidget;
					break;
				default:
					widget = new com.aplana.gwt.client.TextBox();
					break;
			}

            final HasValue hasValue = (HasValue)widget;
            hasValue.addValueChangeHandler(new ValueChangeHandler() {
                @Override
                public void onValueChange(ValueChangeEvent event) {
                    if (getUiHandlers() != null) {
                        getUiHandlers().valueChanged(col.getAlias(), hasValue.getValue());
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

    /**
     *  Label для input'a редактирования значения справочника
     *  с названием атрибута справочника
     */
    private Label getArrtibuteLabel(RefBookColumn col){
        Label label;
        if (col.isRequired()){
            SafeHtmlBuilder builder = new SafeHtmlBuilder();
            builder.appendHtmlConstant(col.getName() + ":<span class='required'>*</span>");
            HTML span = new HTML(builder.toSafeHtml());
            label = span;
        } else{
            label = new Label(col.getName()+":");
        }
        label.addStyleName("inputLabel");

        return label;
    }

	@Override
	public void fillInputFields(Map<String, RefBookValueSerializable> record) {
		if (record == null) {
			for (HasValue w : widgets.values()) {
				w.setValue(null);
				if (w instanceof UIObject) {
                    if (w instanceof RefBookPickerWidget) {
                        if (isNeedToReload) {
                            isNeedToReload = false;
                            ((RefBookPickerWidget) w).reload();
                        }
                        ((RefBookPickerWidget)w).setDereferenceValue("");
					}
				}
			}
		} else {
			for (Map.Entry<RefBookColumn, HasValue> w : widgets.entrySet()) {
				RefBookValueSerializable recordValue = record.get(w.getKey().getAlias());
				if (w.getValue() instanceof RefBookPickerWidget) {
                    RefBookPickerWidget rbw = (RefBookPickerWidget) w.getValue();
                    if (isNeedToReload) {
                        isNeedToReload = false;
                        rbw.reload();
                    }
                    //rbw.setPeriodDates(versionStart.getValue(), versionEnd.getValue());
					rbw.setDereferenceValue(recordValue.getDereferenceValue());
					rbw.setSingleValue(recordValue.getReferenceValue());
				} else if(w.getValue() instanceof HasText) {
					if (w.getKey().getAttributeType() == RefBookAttributeType.NUMBER) {
                        if (w.getValue() instanceof CheckBox) {
                            if(recordValue.getValue() == null){
                                w.getValue().setValue(false);
                            } else if(BigDecimal.ZERO.equals((BigDecimal) recordValue.getValue())){
                                w.getValue().setValue(false);
                            } else {
                                w.getValue().setValue(true);
                            }
                        } else {
                            w.getValue().setValue(((BigDecimal) recordValue.getValue()) == null ? ""
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
	public Map<String, RefBookValueSerializable> getFieldsValues() throws BadValueException {
		Map<String, RefBookValueSerializable> fieldsValues = new HashMap<String, RefBookValueSerializable>();
		for (Map.Entry<RefBookColumn, HasValue> field : widgets.entrySet()) {
			RefBookValueSerializable value = new RefBookValueSerializable();
			try {
                switch (field.getKey().getAttributeType()) {
					case NUMBER:
                        Number number;
                        if (field.getValue() instanceof CheckBox) {
                            number = field.getValue().getValue() == null ?
                                    null :
                                    (Boolean) field.getValue().getValue() ?
                                            BigDecimal.ONE : BigDecimal.ZERO;
                        } else {
                            number = (field.getValue().getValue() == null || field.getValue().getValue().toString().trim().isEmpty())
                                    ? null : new BigDecimal((String) field.getValue().getValue());
                        }
                        checkRequired(field.getKey(), number);
						if (number != null) {
							String numberStr = Double.toString(number.doubleValue());
							String fractionalStr = numberStr.substring(numberStr.indexOf('.')+1);
							String intStr = Integer.toString(Math.abs(number.intValue()));
							if ((intStr.length() > 10) || (fractionalStr.length() > 17)) {
								BadValueException badValueException = new BadValueException();
								badValueException.setFieldName(field.getKey().getName());
								badValueException.setDescription("Значение не соответствует формату (27, 10)");
								throw badValueException;
							}
						}
						value.setAttributeType(RefBookAttributeType.NUMBER);
						value.setNumberValue(number);
						break;
					case STRING:
						String string = (field.getValue().getValue() == null || ((String)field.getValue().getValue()).trim().isEmpty()) ?
								null : (String)field.getValue().getValue();
						checkRequired(field.getKey(), string);
						if (string!= null && string.length() > MAX_STRING_VALUE_LENGTH) {
							BadValueException badValueException = new BadValueException();
							badValueException.setFieldName(field.getKey().getName());
							badValueException.setDescription("Значение более " + MAX_STRING_VALUE_LENGTH + " символов");
							throw badValueException;
						}
						value.setAttributeType(RefBookAttributeType.STRING);
						value.setStringValue(StringUtils.cleanString(string));
						break;
					case DATE:
						Date date = field.getValue().getValue() == null ? null : (Date)field.getValue().getValue();
						checkRequired(field.getKey(), date);
						value.setAttributeType(RefBookAttributeType.DATE);
						value.setDateValue(date);
						break;
					case REFERENCE:
                        //dloshkarev: так не работало для окато и октмо
                        //Long longValue = field.getValue().getValue() == null ? null : (Long)field.getValue().getValue();
                        Long longValue = (field.getValue().getValue() == null || ((List<Long>) field.getValue().getValue()).isEmpty()) ? null : ((List<Long>)field.getValue().getValue()).get(0);
						checkRequired(field.getKey(), longValue);
						value.setAttributeType(RefBookAttributeType.REFERENCE);
						value.setReferenceValue(longValue);
						break;
					default:
						//TODO
						break;
				}
				fieldsValues.put(field.getKey().getAlias(), value);
			} catch (NumberFormatException nfe) {
                nfe.printStackTrace();
				BadValueException badValueException = new BadValueException();
				badValueException.setFieldName(field.getKey().getName());
                badValueException.setDescription("значение некорректно!");
				throw badValueException;
			} catch (ClassCastException cce) {
                cce.printStackTrace();
				BadValueException badValueException = new BadValueException();
				badValueException.setFieldName(field.getKey().getName());
                badValueException.setDescription("значение некорректно!");
				throw badValueException;
			}
		}
		return fieldsValues;
	}

	private void checkRequired(RefBookColumn attr, Object val) throws BadValueException {
		if (attr.isRequired() && (val == null)) {
			BadValueException badValueException = new BadValueException();
			badValueException.setFieldName(attr.getName());
			badValueException.setDescription("Обязательно для заполнения");
			throw badValueException;
		}
	}

    @Override
    public void setHierarchy(boolean isHierarchy) {
        this.isHierarchy = isHierarchy;
    }

    @Override
    public boolean isHierarchy() {
        return isHierarchy;
    }

	private void updateWidgetsVisibility(boolean enabled) {
        if (widgets != null){
            for (HasValue entry : widgets.values()) {
                if (entry instanceof HasEnabled) {
                    ((HasEnabled) entry).setEnabled(enabled);
                }
            }
        }

	}

    @Override
    public void fillVersionData(RefBookRecordVersionData versionData, Long refBookId, Long refBookRecordId) {
        versionStart.setValue(versionData.getVersionStart());
        versionEnd.setValue(versionData.getVersionEnd());
        allVersion.setVisible(!isVersionMode);
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
        switch (mode){
            case CREATE:
                save.setEnabled(true);
                cancel.setEnabled(true);
                updateWidgetsVisibility(true);
                versionStart.setEnabled(true);
                versionEnd.setEnabled(true);
                break;
            case EDIT:
                save.setEnabled(true);
                cancel.setEnabled(true);
                updateWidgetsVisibility(true);
                versionStart.setEnabled(isVersionMode);
                versionEnd.setEnabled(isVersionMode);
                break;
            case READ:
            case VIEW:
                save.setEnabled(false);
                cancel.setEnabled(false);
                versionStart.setEnabled(false);
                versionEnd.setEnabled(false);
                updateWidgetsVisibility(false);
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
			getUiHandlers().onSaveClicked();
		}
	}

	@UiHandler("cancel")
	void cancelButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onCancelClicked();
		}
	}
}

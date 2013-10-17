package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.exception.BadValueException;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookColumn;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookValueSerializable;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.CustomDateBox;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client.RefBookPickerPopupWidget;
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

	interface Binder extends UiBinder<Widget, EditFormView> { }

	Map<RefBookColumn, HasValue> widgets;

	@UiField
	VerticalPanel editPanel;
	@UiField
	Button save;
	@UiField
	Button cancel;

	@Inject
	@UiConstructor
	public EditFormView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public Map<RefBookColumn, HasValue> createInputFields(List<RefBookColumn> attributes) {
		editPanel.clear();
		if (widgets != null) widgets.clear();
		Map<RefBookColumn, HasValue> widgets = new HashMap<RefBookColumn, HasValue>();
		for (RefBookColumn col : attributes) {
			HorizontalPanel oneField = new HorizontalPanel();
			oneField.setWidth("100%");
            Label label = getArrtibuteLabel(col);
            label.setWordWrap(true);
			HorizontalPanel panel = new HorizontalPanel();
			panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			panel.setWidth("100%");
			oneField.add(label);
			Widget widget;
			switch (col.getAttributeType()) {
				case NUMBER:
					widget = new TextBox();
					break;
				case STRING:
					widget = new TextBox();
					break;
				case DATE:
					widget = new CustomDateBox();
					break;
				case REFERENCE:
					RefBookPickerPopupWidget refbookWidget = new RefBookPickerPopupWidget();
					refbookWidget.setAttributeId(col.getRefBookAttributeId());
					widget = refbookWidget;
					break;
				default:
					widget = new TextBox();
					break;
			}
            widget.setWidth("300px");
			panel.add(widget);
			HasValue hasValue = (HasValue)widget;
			hasValue.addValueChangeHandler(new ValueChangeHandler() {
				@Override
				public void onValueChange(ValueChangeEvent event) {
					if (event.getSource() instanceof UIObject) {
						((UIObject) event.getSource()).setTitle(event.getValue().toString());
					}
					if (getUiHandlers() != null) {
						getUiHandlers().valueChanged();
					}
				}
			});
			oneField.add(panel);
			editPanel.add(oneField);
			widgets.put(col, (HasValue)widget);
		}
		this.widgets = widgets;
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
					((UIObject) w).setTitle(null);
					if (w instanceof RefBookPickerPopupWidget) {
						((RefBookPickerPopupWidget)w).setDereferenceValue("");
					}
				}
			}
		} else {
			for (Map.Entry<RefBookColumn, HasValue> w : widgets.entrySet()) {
				RefBookValueSerializable recordValue = record.get(w.getKey().getAlias());
				if (w.getValue() instanceof RefBookPickerPopupWidget) {
					RefBookPickerPopupWidget rbw = (RefBookPickerPopupWidget) w.getValue();
					rbw.setDereferenceValue(recordValue.getDereferenceValue());
					rbw.setValue(recordValue.getReferenceValue());
                    rbw.setTitle(String.valueOf(rbw.getDereferenceValue()));
				} else if(w.getValue() instanceof HasText) {
                    ((Widget)w.getValue()).setTitle(((HasText)w.getValue()).getText());
					if (w.getKey().getAttributeType() == RefBookAttributeType.NUMBER) {
						w.getValue().setValue(((BigDecimal) recordValue.getValue()) == null ? ""
								: ((BigDecimal) recordValue.getValue()).toPlainString());
					} else {
						w.getValue().setValue(recordValue.getValue());
					}
                } else {
					w.getValue().setValue(recordValue.getValue());
				}
				if (w.getValue() instanceof Widget) {
					((Widget) w.getValue()).setTitle(w.getValue().getValue() == null ? ""
							: w.getValue().getValue().toString());
				}
			}
		}
	}

	@Override
	public Map<String, RefBookValueSerializable> getFieldsValues() throws BadValueException {
		Map<String, RefBookValueSerializable> fieldsValues = new HashMap<String, RefBookValueSerializable>();
		for (Map.Entry<RefBookColumn, HasValue> field : widgets.entrySet()) {
			RefBookValueSerializable value = new RefBookValueSerializable();
			try {
				switch (field.getKey().getAttributeType()) {
					case NUMBER:
						Number number = (field.getValue().getValue() == null || field.getValue().getValue().toString().trim().isEmpty())
								? null : new BigDecimal((String)field.getValue().getValue());
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
						if (string!= null && string.length() > 2000) {
							BadValueException badValueException = new BadValueException();
							badValueException.setFieldName(field.getKey().getName());
							badValueException.setDescription("Значение более 2000 символов");
							throw badValueException;
						}
						value.setAttributeType(RefBookAttributeType.STRING);
						value.setStringValue(string);
						break;
					case DATE:
						Date date = field.getValue().getValue() == null ? null : (Date)field.getValue().getValue();
						checkRequired(field.getKey(), date);
						value.setAttributeType(RefBookAttributeType.DATE);
						value.setDateValue(date);
						break;
					case REFERENCE:
						Long longValue = field.getValue().getValue() == null ? null : (Long)field.getValue().getValue();
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
				BadValueException badValueException = new BadValueException();
				badValueException.setFieldName(field.getKey().getName());
				throw badValueException;
			} catch (ClassCastException cce) {
				BadValueException badValueException = new BadValueException();
				badValueException.setFieldName(field.getKey().getName());
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
	public void setSaveButtonEnabled(boolean enabled) {
		save.setEnabled(enabled);
	}

	@Override
	public void setCancelButtonEnabled(boolean enabled) {
		cancel.setEnabled(enabled);
	}

	@Override
	public void setEnabled(boolean enabled) {
		for (HasValue entry : widgets.values()) {
			if (entry instanceof HasEnabled) {
				((HasEnabled) entry).setEnabled(enabled);
			}
		}
		save.setEnabled(enabled);
		cancel.setEnabled(enabled);
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

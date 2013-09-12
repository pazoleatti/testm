package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.event.UpdateForm;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookValueSerializable;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.CustomDateBox;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client.RefBookPickerPopupWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditFormView extends ViewWithUiHandlers<EditFormUiHandlers> implements EditFormPresenter.MyView{

	interface Binder extends UiBinder<Widget, EditFormView> { }

	Map<RefBookAttribute, HasValue> widgets;

	@UiField
	VerticalPanel editPanel;
	@UiField
	Button save;
	@UiField
	Button cancel;
	@UiField
	DateBox relevanceDate;

	@Inject
	@UiConstructor
	public EditFormView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
		relevanceDate.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat("dd.MM.yyyy")));
		relevanceDate.setValue(new Date());
		relevanceDate.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				if (getUiHandlers() != null) {
					getUiHandlers().onRelevanceDateChanged();
				}
			}
		});
	}

	@Override
	public Map<RefBookAttribute, HasValue> createInputFields(List<RefBookAttribute> attributes) {
		editPanel.clear();
		if (widgets != null) widgets.clear();
		Map<RefBookAttribute, HasValue> widgets = new HashMap<RefBookAttribute, HasValue>();
		for (RefBookAttribute attr : attributes) {
			HorizontalPanel oneField = new HorizontalPanel();
			Label label = new Label(attr.getName());
            label.setWordWrap(true);
			label.setWidth("100px");
			oneField.add(label);
			Widget widget;
			switch (attr.getAttributeType()) {
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
					refbookWidget.setAttributeId(attr.getRefBookAttributeId());
					widget = refbookWidget;
					break;
				default:
					widget = new TextBox();
					break;
			}
            widget.setWidth("300px");
			HasValue hasValue = (HasValue)widget;
			hasValue.addValueChangeHandler(new ValueChangeHandler() {
				@Override
				public void onValueChange(ValueChangeEvent event) {
					if (getUiHandlers() != null) {
						getUiHandlers().valueChanged();
					}
				}
			});
			oneField.add(widget);
			editPanel.add(oneField);
			widgets.put(attr, (HasValue)widget);
		}
		this.widgets = widgets;
		return widgets;
	}

	@Override
	public void fillInputFields(Map<String, RefBookValueSerializable> record) {
		if (record == null) {
			for (HasValue w : widgets.values()) {
				w.setValue(null);
			}
		} else {
			for (Map.Entry<RefBookAttribute, HasValue> w : widgets.entrySet()) {
				RefBookValueSerializable recordValue = record.get(w.getKey().getAlias());
				if (w.getValue() instanceof RefBookPickerPopupWidget) {
					RefBookPickerPopupWidget rbw = (RefBookPickerPopupWidget) w.getValue();
					rbw.setDereferenceValue(recordValue.getDereferenceValue());
					rbw.setValue(recordValue.getReferenceValue());
                    rbw.setTitle(String.valueOf(rbw.getDereferenceValue()));
				} else if(w.getValue() instanceof HasText) {
                    ((Widget)w.getValue()).setTitle(((HasText)w.getValue()).getText());
					if (w.getKey().getAttributeType() == RefBookAttributeType.NUMBER) {
						w.getValue().setValue(((BigDecimal)recordValue.getValue()).toPlainString());
					} else {
						w.getValue().setValue(recordValue.getValue());
					}
                } else {
					w.getValue().setValue(recordValue.getValue());
				}
			}
		}
	}

	@Override
	public Map<String, RefBookValueSerializable> getFieldsValues() {
		Map<String, RefBookValueSerializable> fieldsValues = new HashMap<String, RefBookValueSerializable>();
		for (Map.Entry<RefBookAttribute, HasValue> field : widgets.entrySet()) {
			RefBookValueSerializable value = new RefBookValueSerializable();
			switch (field.getKey().getAttributeType()) {
				case NUMBER:
					Number number = field.getValue().getValue() == null ? null : new BigDecimal((String)field.getValue().getValue());
					value.setAttributeType(RefBookAttributeType.NUMBER);
					value.setNumberValue(number);
					break;
				case STRING:
					String string = field.getValue().getValue() == null ? null : (String)field.getValue().getValue();
					value.setAttributeType(RefBookAttributeType.STRING);
					value.setStringValue(string);
					break;
				case DATE:
					Date date = field.getValue().getValue() == null ? null : (Date)field.getValue().getValue();
					value.setAttributeType(RefBookAttributeType.DATE);
					value.setDateValue(date);
					break;
				case REFERENCE:
					Long longValue = field.getValue().getValue() == null ? null : (Long)field.getValue().getValue();
					value.setAttributeType(RefBookAttributeType.REFERENCE);
					value.setReferenceValue(longValue);
					break;
				default:
					//TODO
					break;
			}
			fieldsValues.put(field.getKey().getAlias(), value);
		}
		return fieldsValues;
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
	public Date getRelevanceDate() {
		return relevanceDate.getValue();
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

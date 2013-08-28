package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookValueSerializable;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.CustomDateBox;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client.RefBookPickerPopupWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

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

	@Inject
	@UiConstructor
	public EditFormView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public Map<RefBookAttribute, HasValue> createInputFields(List<RefBookAttribute> attributes) {
		Map<RefBookAttribute, HasValue> widgets = new HashMap<RefBookAttribute, HasValue>();
		for (RefBookAttribute attr : attributes) {
			HorizontalPanel oneField = new HorizontalPanel();
			Label label = new Label(attr.getName());
			label.setWidth("200px");
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
			oneField.add(widget);
			editPanel.add(oneField);
			widgets.put(attr, (HasValue)widget);
		}
		this.widgets = widgets;
		return widgets;
	}

	@Override
	public void fillInputFields(Map<String, RefBookValueSerializable> record) {
		System.out.println("Fill " + record);
		if (record == null) {
			for (HasValue w : widgets.values()) {
				w.setValue(null);
			}
		} else {

			for (Map.Entry<RefBookAttribute, HasValue> w : widgets.entrySet()) {
//				if (w.getKey().getAlias().equals())
				w.getValue().setValue(record.get(w.getKey().getAlias()).getValue());
			}

//			for (Map.Entry<String, RefBookValueSerializable> attr : record.entrySet()) {
//				HasValue w = widgets.get(attr);
//				if (w != null) {
//					w.setValue(attr.getValue().getValue());
//				}
//			}
		}
	}

	@Override
	public Map<String, RefBookValueSerializable> getFieldsValues() {
		Map<String, RefBookValueSerializable> fieldsValues = new HashMap<String, RefBookValueSerializable>();
		for (Map.Entry<RefBookAttribute, HasValue> field : widgets.entrySet()) {
			RefBookValueSerializable value = new RefBookValueSerializable();
			switch (field.getKey().getAttributeType()) {
				case NUMBER:
					Number number = Long.valueOf((String) field.getValue().getValue());
					value.setAttributeType(RefBookAttributeType.NUMBER);
					value.setNumberValue(number);
					break;
				case STRING:
					String string = (String) field.getValue().getValue();
					value.setAttributeType(RefBookAttributeType.STRING);
					value.setStringValue(string);
					break;
				case DATE:
					Date date = (Date)field.getValue().getValue();
					value.setAttributeType(RefBookAttributeType.DATE);
					value.setDateValue(date);
					break;
				case REFERENCE:
					Long longValue = (Long)field.getValue().getValue();
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

	@UiHandler("save")
	void saveButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onSaveClicked();
		}
	}
}

package com.aplana.sbrf.taxaccounting.web.module.admin.client.ui;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.view.FormTemplateRowView;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.List;


public class StyleCellPopup extends Composite implements Editor<Cell>, TakesValue<Cell> {
	interface StyleWidgetUiBinder extends UiBinder<HTMLPanel, StyleCellPopup> {
	}
	interface MyDriver extends SimpleBeanEditorDriver<Cell, StyleCellPopup> {
	}

	private static StyleWidgetUiBinder ourUiBinder = GWT.create(StyleWidgetUiBinder.class);
	private final MyDriver driver = GWT.create(MyDriver.class);
	private PopupPanel popup = new PopupPanel();
	private List<String> stylesAlias = new ArrayList<String>();
	private final FormTemplateRowView parent;

	@UiField(provided = true)
	ValueListBox<String> styleAlias;

	@UiField
	IntegerBox colSpan;

	@UiField
	IntegerBox rowSpan;

	@UiField
	Button saveButton;

	@UiConstructor
	public StyleCellPopup(FormTemplateRowView parent) {
		super();
		this.parent = parent;

		styleAlias = new ValueListBox<String>(new AbstractRenderer<String>() {
			@Override
			public String render(String object) {
				if (object == null) {
					return "";
				}
				return object;
			}
		});

		initWidget(ourUiBinder.createAndBindUi(this));
		driver.initialize(this);

		this.addDomHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					save();
				}
			}
		}, KeyPressEvent.getType());

		popup.setWidget(this);
		popup.setAnimationEnabled(true);
		popup.setAutoHideEnabled(true);
	}

	@Override
	public void setValue(Cell value) {
		driver.edit(value);
	}

	@Override
	public Cell getValue() {
		return driver.flush();
	}

	@UiHandler("saveButton")
	public void onClickSaveButton(ClickEvent event) {
		save();
	}

	public void show(int left, int top) {
		popup.setPopupPosition(left, top);
		popup.show();
	}

	public void setStyleAlias(List<FormStyle> styleAlias) {
		stylesAlias.clear();
		for(FormStyle style : styleAlias) {
			stylesAlias.add(style.getAlias());
		}
		this.styleAlias.setAcceptableValues(stylesAlias);
	}

	private void save() {
		if (parent.validateCellsUnionRange(rowSpan.getValue(), colSpan.getValue())) {
			getValue();
			parent.refresh();
		}

		popup.hide();
	}
}
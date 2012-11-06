package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.event.dom.client.ClickEvent;

/**
 * Заготовка для виджета для выбора значения из справочника
 * @author dsultanbekov
 *
 */
public class DictionaryPickerWidget extends Composite {
	interface MyUiBinder extends UiBinder<Widget, DictionaryPickerWidget> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	@UiField(provided=true) CellTable<Object> cellTable = new CellTable<Object>();
	@UiField Button button;

	public DictionaryPickerWidget() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	@UiHandler("button")
	void onButtonClick(ClickEvent event) {
	}
	
	public String getValue() {
		return "testValue";
	}
}
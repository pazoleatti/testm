package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client;

import java.util.LinkedList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.dictionary.SimpleDictionaryItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Заготовка для виджета для выбора значения из справочника
 * @author dsultanbekov
 *
 */
public class DictionaryPickerWidget extends Composite implements HasValueChangeHandlers<SimpleDictionaryItem<Long>>{
	interface MyUiBinder extends UiBinder<Widget, DictionaryPickerWidget> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	@UiField(provided=true) CellTable<SimpleDictionaryItem<Long>> cellTable = new CellTable<SimpleDictionaryItem<Long>>();
//	@UiField Button button;
	@UiField TextBox txtFind;
	@UiField SimplePager pager;
	
	private String value;

	public DictionaryPickerWidget() {
		initWidget(uiBinder.createAndBindUi(this));
		
		TextColumn<SimpleDictionaryItem<Long>> idColumn = new TextColumn<SimpleDictionaryItem<Long>>() {
			@Override
			public String getValue(SimpleDictionaryItem<Long> object) {
				return object.getName();
			}
		};
		
		TextColumn<SimpleDictionaryItem<Long>> formTypeColumn = new TextColumn<SimpleDictionaryItem<Long>>() {
			@Override
			public String getValue(SimpleDictionaryItem<Long> object) {
				return object.getValue().toString();
			}
		};
		cellTable.addColumn(idColumn, "Столбец1");
		cellTable.addColumn(formTypeColumn, "Столбец2");
//		List<String> values = new LinkedList<String>();
//		for (int i=0; i<13; i++) {
//			values.add("Value" + String.valueOf(i));
//		}
//		cellTable.setRowData(values);
		cellTable.setPageSize(5);
//		SimplePager.Resources pagerResources = GWT
//                 .create(SimplePager.Resources.class);
//		pager = new SimplePager(TextLocation.CENTER, pagerResources, true, 0,
//                 true);
		
		 
		pager.setDisplay(cellTable);
//		ListDataProvider<String> dataProvider = new ListDataProvider<String>(values);
		DictionaryDataProvider dataProvider = new DictionaryDataProvider();
		dataProvider.addDataDisplay(cellTable);
		cellTable.addCellPreviewHandler(new Handler<SimpleDictionaryItem<Long>>() {

			@Override
			public void onCellPreview(CellPreviewEvent<SimpleDictionaryItem<Long>> event) {
				Boolean isClick = "click".equals(event.getNativeEvent().getType());
				if (isClick) {
//					value = event.getValue();
					ValueChangeEvent.fire(DictionaryPickerWidget.this, event.getValue());
				}
			}
		});
		
		
		txtFind.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
			}
		});
		
	}
//	@UiHandler("button")
//	void onButtonClick(ClickEvent event) {
//		dispatcher.execute(new GetDictionaryDataList(), new AbstractCallback<GetDictionaryDataListResult>() {
//			@Override
//			public void onSuccess(GetDictionaryDataListResult result) {
////				getView().setFormDataList(result.getRecords());	
//			}
//		});
		
//		value = txtFind.getValue();
//		ValueChangeEvent.fire(this, value);
//	}
	
	@UiHandler("btnClear")
	void onBtnClearClick(ClickEvent event) {
		ValueChangeEvent.fire(DictionaryPickerWidget.this, null);
	}
	
	
	
	public String getValue() {
//		return "testValue";
		return value;
	}
	
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<SimpleDictionaryItem<Long>> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	 }
}
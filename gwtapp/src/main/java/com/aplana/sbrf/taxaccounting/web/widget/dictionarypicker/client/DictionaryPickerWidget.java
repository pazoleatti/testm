package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;

import java.io.Serializable;

/**
 * Заготовка для виджета для выбора значения из справочника
 * @author dsultanbekov
 *
 */
public abstract class DictionaryPickerWidget<ValueType extends Serializable> extends Composite implements HasValueChangeHandlers<DictionaryItem<ValueType>>{
	interface MyUiBinder extends UiBinder<Widget, DictionaryPickerWidget> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	@UiField CellTable<DictionaryItem<ValueType>> cellTable;
	@UiField Button btnFind;
	@UiField TextBox txtFind;
	@UiField SimplePager pager;

	private AsyncDataProvider<DictionaryItem<ValueType>> dataProvider;

	public DictionaryPickerWidget(String dictionaryCode) {
		initWidget(uiBinder.createAndBindUi(this));

		btnFind.setVisible(true);

		// Table
		TextColumn<DictionaryItem<ValueType>> idColumn = new TextColumn<DictionaryItem<ValueType>>() {
			@Override
			public String getValue(DictionaryItem<ValueType> object) {
				return object.getName();
			}
		};

		TextColumn<DictionaryItem<ValueType>> valueColumn = createValueColumn();

		cellTable.addColumn(idColumn, "Имя");
		cellTable.addColumn(valueColumn, "Значение");
		cellTable.setPageSize(10);
		 
		pager.setDisplay(cellTable);
		dataProvider = createDataProvider(dictionaryCode);
		dataProvider.addDataDisplay(cellTable);
		cellTable.addCellPreviewHandler(new Handler<DictionaryItem<ValueType>>() {
			@Override
			public void onCellPreview(CellPreviewEvent<DictionaryItem<ValueType>> event) {
				Boolean isClick = "click".equals(event.getNativeEvent().getType());
				if (isClick) {
					ValueChangeEvent.fire(DictionaryPickerWidget.this, event.getValue());
				}
			}
		});
	}

	protected abstract AsyncDataProvider<DictionaryItem<ValueType>> createDataProvider(String dictionaryCode);

	protected abstract TextColumn<DictionaryItem<ValueType>> createValueColumn();
 /*
	@UiHandler("txtFind")
	public void onTxtFindKeyPress(KeyPressEvent event){
		int keyCode = event.getNativeEvent().getKeyCode();
		if (keyCode == KeyCodes.KEY_ENTER) {
			findAction();
		} else if (keyCode == KeyCodes.KEY_DOWN ) {
			Window.alert("OK");
			pager.nextPage();
		} else if (keyCode == KeyCodes.KEY_PAGEDOWN) {
			pager.previousPage();
		}
	}

	@UiHandler("txtFind")
	public void onTxtFindKeyUp(KeyUpEvent event){
		int keyCode = event.getNativeEvent().getKeyCode();
		if (keyCode == KeyCodes.KEY_DOWN ) {
			pager.nextPage();
		} else if (keyCode == KeyCodes.KEY_UP) {
			pager.previousPage();
		}
	}
	@UiHandler("btnFind")
	void onFindButtonClick(ClickEvent event) {
		
	}
   */

	@UiHandler("btnClear")
	void onBtnClearClick(ClickEvent event) {
		ValueChangeEvent.fire(DictionaryPickerWidget.this, null);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<DictionaryItem<ValueType>> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}
	/*
	private void findAction() {
		dataProvider.setFilter("%"+txtFind.getValue()+"%");
		pager.firstPage();
		Range range = cellTable.getVisibleRange();
		cellTable.setVisibleRangeAndClearData(range, true); 
	}

	@Override
	protected void onLoad() {
		super.onLoad();
	    findAction();
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {    
			  @Override
			  public void execute() {
				  txtFind.setFocus(true);
			  }
		});

		cellTable.addRangeChangeHandler(new RangeChangeEvent.Handler() {
	          public void onRangeChange(RangeChangeEvent event) {
	        	  txtFind.setFocus(true);
	          }
	    });	  
	}	
	*/
}
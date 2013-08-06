package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client;

import java.util.Date;
import java.util.List;

import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client.RefBookPickerWidgetPresenter.MyView;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.RefBookItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.client.DateTimeFormatRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent;


/**
 * @author sgoryachkin
 *
 */
public class RefBookPickerWidget extends Composite implements RefBookPicker, MyView {
	
	private final NoSelectionModel<RefBookItem> selectionModel = new NoSelectionModel<RefBookItem>();

	interface Binder extends UiBinder<Widget, RefBookPickerWidget> {
	}
	
	private static Binder binder = GWT.create(Binder.class);

	@UiField
	CellTable<RefBookItem> cellTable;

	@UiField
	TextBox txtFind;

	@UiField(provided=true)
	ValueListBox<Date> version;

	@UiField
	FlexiblePager pager;

	Timer timer = new Timer() {
		public void run() {
			uiHandlers.onSearchPatternChange();
		}
	};
	
	private MyDataProvider dataProvider = new MyDataProvider();

	
	private class MyDataProvider extends AsyncDataProvider<RefBookItem> {

		public void update() {
			for (HasData<RefBookItem> display: getDataDisplays()) {
				onRangeChanged(display);
			}
		}

		@Override
		public void onRangeChanged(HasData<RefBookItem> display) {
			Range range = display.getVisibleRange();
			uiHandlers.rangeChanged(range.getStart(), range.getLength());
		}
	}
	
	
	public RefBookPickerWidget() {
		version = new ValueListBox<Date>(new DateTimeFormatRenderer());
		initWidget(binder.createAndBindUi(this));
		new RefBookPickerWidgetPresenter(this);
		
		cellTable.setSelectionModel(selectionModel);
		
		cellTable
				.addCellPreviewHandler(new CellPreviewEvent.Handler<RefBookItem>() {
					@Override
					public void onCellPreview(
							CellPreviewEvent<RefBookItem> event) {
						if (BrowserEvents.MOUSEOVER.equals(event
								.getNativeEvent().getType())) {
							Element cellElement = event.getNativeEvent()
									.getEventTarget().cast();
							cellElement.setTitle(cellElement.getInnerText());
						}
					}
				});

		selectionModel
				.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
					@Override
					public void onSelectionChange(SelectionChangeEvent event) {
						uiHandlers.onSelectionChange();
						focus();
					}
				});

		cellTable
				.addLoadingStateChangeHandler(new LoadingStateChangeEvent.Handler() {
					@Override
					public void onLoadingStateChanged(
							LoadingStateChangeEvent event) {
						if (event.getLoadingState() == LoadingStateChangeEvent.LoadingState.LOADED) {
							focus();
						}
					}
				});

		cellTable.addRangeChangeHandler(new RangeChangeEvent.Handler() {
			public void onRangeChange(RangeChangeEvent event) {
				focus();
			}
		});


		pager.setDisplay(cellTable);
		pager.setPageSize(15);

		dataProvider.addDataDisplay(cellTable);
	}

	public void focus() {
		txtFind.setFocus(true);
		txtFind.setCursorPos(txtFind.getText().length());
	}

	@UiHandler("txtFind")
	public void onTxtFindKeyUp(KeyUpEvent event) {
		int keyCode = event.getNativeEvent().getKeyCode();
		if (keyCode == KeyCodes.KEY_PAGEDOWN) {
			pager.nextPage();
		} else if (keyCode == KeyCodes.KEY_PAGEUP) {
			pager.previousPage();
			// Не ищем, если пользователь просто передвигает курсор внутри поля
			// для поиска
		} else if (keyCode == KeyCodes.KEY_ENTER) {
			timer.schedule(500);
		}
	}

	@UiHandler("btnClear")
	void onBtnClearClick(ClickEvent event) {
		uiHandlers.onBtnClearClick();
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<Long> handler) {
		return uiHandlers.addValueChangeHandler(handler);
	}

	@Override
	public Long getValue() {
		return uiHandlers.getValue();
	}

	@Override
	public void setValue(Long value) {
		uiHandlers.setValue(value);
	}

	@Override
	public void setValue(Long value, boolean fireEvent) {
		uiHandlers.setValue(value, fireEvent);
	}
	
	public void clear() {
		txtFind.setValue("");
	}

	@Override
	public void addToSlot(Object slot, IsWidget content) {
		// 
	}

	@Override
	public void removeFromSlot(Object slot, IsWidget content) {
		// 
	}

	@Override
	public void setInSlot(Object slot, IsWidget content) {
		// 
	}

	@Override
	public Date getVersion() {
		return version.getValue();
	}

	@Override
	public void setRowData(int start, List<RefBookItem> values, int size) {
		dataProvider.updateRowData(start, values);
		dataProvider.updateRowCount(size, true);
	}

	private RefBookPickerWidgetUiHandlers uiHandlers;
	
	@Override
	public void setUiHandlers(RefBookPickerWidgetUiHandlers uiHandlers) {
		this.uiHandlers = uiHandlers;	
	}

	@Override
	public String getSearchPattern() {
		return txtFind.getValue();
	}

	@Override
	public void setVersion(Date versionDate) {
		version.setValue(versionDate);
	}



	@Override
	public void setAcceptableValues(long refBookAttrId) {
		uiHandlers.init(refBookAttrId, null, null);
	}

	@Override
	public void setAcceptableValues(long refBookAttrId, Date date1, Date date2) {
		uiHandlers.init(refBookAttrId, date1, date2);
	}

	@Override
	public void setVersions(List<Date> versions) {
		version.setValue(versions.iterator().next());
		version.setAcceptableValues(versions);	
	}

	@Override
	public void setHeaders(List<String> headers) {
		for (int i = 0; i < headers.size(); i++) {
			cellTable.addColumn(new RefBookItemTextColumn(i), headers.get(i));
		}
	}

	@Override
	public void refreshData() {
		dataProvider.update();
	}

	@Override
	public void widgetFireChangeEvent(Long value) {
		ValueChangeEvent.fire(RefBookPickerWidget.this, value);
	}
	
	@Override
	public HandlerRegistration widgetAddValueHandler(ValueChangeHandler<Long> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}



	@Override
	public RefBookItem getSelectionValue() {
		return selectionModel.getLastSelectedObject();
	}

	@Override
	public String getDereferenceValue() {
		return uiHandlers.getDereferenceValue();
	}

}
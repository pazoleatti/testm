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
import com.google.gwt.user.client.ui.HasValue;
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
public class RefBookPickerWidget extends Composite implements HasValue<Long>, MyView {
	
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
	
	AsyncDataProvider<RefBookItem> dataProvider = new AsyncDataProvider<RefBookItem>() {
		@Override
		protected void onRangeChanged(HasData<RefBookItem> display) {
			Range range = display.getVisibleRange();
			uiHandlers.rangeChanged(range.getStart(), range.getLength());
		}
	};
	
	
	public RefBookPickerWidget(long refBookId) {
		this(refBookId, null);
	}
	
	public RefBookPickerWidget(long refBookId, Long formDataId) {
		version = new ValueListBox<Date>(new DateTimeFormatRenderer());
		initWidget(binder.createAndBindUi(this));
		new RefBookPickerWidgetPresenter(this, refBookId, formDataId);
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
		setValue(null, true);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<Long> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
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
		uiHandlers.setValue(value);
		if (fireEvent) {
			ValueChangeEvent.fire(RefBookPickerWidget.this, value);
		}
	}

	public void clear() {
		txtFind.setValue("");
	}

	@Override
	public void addToSlot(Object slot, IsWidget content) {
		// TODO
		
	}

	@Override
	public void removeFromSlot(Object slot, IsWidget content) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInSlot(Object slot, IsWidget content) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Date getVersion() {
		return version.getValue();
	}

	@Override
	public void updateRowData(int start, List<RefBookItem> values, int size) {
		dataProvider.updateRowData(start, values);
		dataProvider.updateRowCount(size, true);
	}

	private RefBookPickerWidgetUiHandlers uiHandlers;
	
	@Override
	public void setUiHandlers(RefBookPickerWidgetUiHandlers uiHandlers) {
		this.uiHandlers = uiHandlers;	
	}

	@Override
	public void initView(List<String> headers, List<Date> versions) {

		for (int i = 0; i < headers.size(); i++) {
			cellTable.addColumn(new RefBookItemTextColumn(i), headers.get(i));
		}
		
		version.setValue(versions.iterator().next());
		version.setAcceptableValues(versions);	


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
						RefBookItem selected = selectionModel
								.getLastSelectedObject();
						if (selected != null) {
							setValue(selected.getId(), true);
						}
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

	@Override
	public String getSearchPattern() {
		return txtFind.getValue();
	}

	@Override
	public void goToFirstPage() {
		pager.firstPage();
	}

	@Override
	public void setVersion(Date versionDate) {
		version.setValue(versionDate);
	}
}
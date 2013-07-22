package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client;

import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent;

/**
 * Базовый класс виджета для выбора значения из справочника.
 * 
 * @param <ValueType>
 *            тип значения справочника
 */
public class RefBookPickerWidget extends Composite implements HasValue<Long> {
	
	private final NoSelectionModel<RefBookItem> selectionModel;

	interface Binder extends UiBinder<Widget, RefBookPickerWidget> {
	}

	private static Binder binder = GWT.create(Binder.class);

	@UiField
	CellTable<RefBookItem> cellTable;

	@UiField
	TextBox txtFind;

	@UiField
	FlexiblePager pager;

	Timer timer = new Timer() {
		public void run() {
			find();
		}
	};

	private RefBookDataProvider dataProvider;

	private Long value;

	public RefBookPickerWidget(Long refBookId) {
		initWidget(binder.createAndBindUi(this));

		// Table
		cellTable.addColumn(new TextColumn<RefBookItem>() {
			@Override
			public String getValue(RefBookItem object) {
				return object.getName();
			}
		}, "Имя");

		selectionModel = new NoSelectionModel<RefBookItem>();
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

		dataProvider = new RefBookDataProvider(refBookId);
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

	private void find() {
		String oldValue = dataProvider.getSearchPattern();
		String newValue = txtFind.getValue();
		if (oldValue != null ? !oldValue.equals(newValue) : newValue != null) {
			dataProvider.setSearchPattern(txtFind.getValue());
			if (cellTable.getVisibleRange().getStart() == 0) {
				dataProvider.load(cellTable.getVisibleRange());
			} else {
				pager.firstPage();
			}
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
		return value;
	}

	@Override
	public void setValue(Long value) {
		setValue(value, false);
	}

	@Override
	public void setValue(Long value, boolean b) {
		if (this.value != null ? !this.value.equals(value) : value != null) {
			this.value = value;

			if (b) {
				ValueChangeEvent.fire(RefBookPickerWidget.this, value);
			}
		}
	}

	public void clear() {
		txtFind.setValue("");
		String pattern = dataProvider.getSearchPattern();
		dataProvider.setSearchPattern("");
		if ((pattern == null || pattern.isEmpty())
				&& cellTable.getVisibleRange().getStart() != 0) {
			pager.firstPage();
		} else if (pattern != null && !pattern.isEmpty()) {
			dataProvider.load(cellTable.getVisibleRange());
		}
	}
}
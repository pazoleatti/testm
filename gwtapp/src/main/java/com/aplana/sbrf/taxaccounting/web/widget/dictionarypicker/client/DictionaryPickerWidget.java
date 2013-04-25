package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.google.gwt.core.client.GWT;
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
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.*;

import java.io.Serializable;

/**
 * Базовый класс виджета для выбора значения из справочника.
 *
 * @param <ValueType> тип значения справочника
 */
public abstract class DictionaryPickerWidget<ValueType extends Serializable> extends Composite
		implements HasValue<ValueType> {
	private final NoSelectionModel<DictionaryItem<ValueType>> selectionModel;

	interface MyUiBinder extends UiBinder<Widget, DictionaryPickerWidget> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	@UiField
	CellTable<DictionaryItem<ValueType>> cellTable;
	@UiField
	TextBox txtFind;
	@UiField
	FlexiblePager pager;

	Timer timer = new Timer() {
		public void run() {
			find();
		}
	};

	private DictionaryDataProvider<?, ValueType> dataProvider;

	private ValueType value;

	public DictionaryPickerWidget(String dictionaryCode) {
		initWidget(uiBinder.createAndBindUi(this));

		// Table
		cellTable.addColumn(new NameColumn(), "Имя");
		cellTable.addColumn(createValueColumn(), "Значение");
		selectionModel = new NoSelectionModel<DictionaryItem<ValueType>>();
		cellTable.setSelectionModel(selectionModel);
		cellTable.addCellPreviewHandler(new CellPreviewEvent.Handler<DictionaryItem<ValueType>>() {
			@Override
			public void onCellPreview(CellPreviewEvent<DictionaryItem<ValueType>> event) {
				if ("mouseover".equals(event.getNativeEvent().getType())) {
					Element cellElement = event.getNativeEvent().getEventTarget().cast();
					cellElement.setTitle(cellElement.getInnerText());
				}
			}
		});
		selectionModel.addSelectionChangeHandler(
				new SelectionChangeEvent.Handler() {
					@Override
					public void onSelectionChange(SelectionChangeEvent event) {
						DictionaryItem<ValueType> selected = selectionModel.getLastSelectedObject();
						if (selected != null) {
							setValue(selected.getValue(), true);
						}
						focus();
					}
				}
		);

		cellTable.addLoadingStateChangeHandler(new LoadingStateChangeEvent.Handler() {
			@Override
			public void onLoadingStateChanged(LoadingStateChangeEvent event) {
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

		dataProvider = createDataProvider(dictionaryCode);
		dataProvider.addDataDisplay(cellTable);
	}

	public void focus() {
		txtFind.setFocus(true);
		txtFind.setCursorPos(txtFind.getText().length());
	}

	private class NameColumn extends TextColumn<DictionaryItem<ValueType>> {
		@Override
		public String getValue(DictionaryItem<ValueType> object) {
			return object.getName();
		}
	}

	protected abstract DictionaryDataProvider<?, ValueType> createDataProvider(String dictionaryCode);

	protected TextColumn<DictionaryItem<ValueType>> createValueColumn() {
		return new TextColumn<DictionaryItem<ValueType>>() {
			@Override
			public String getValue(DictionaryItem<ValueType> item) {
				return valueToString(item.getValue());
			}
		};
	}

	@UiHandler("txtFind")
	public void onTxtFindKeyUp(KeyUpEvent event) {
		int keyCode = event.getNativeEvent().getKeyCode();
		if (keyCode == KeyCodes.KEY_PAGEDOWN) {
			pager.nextPage();
		} else if (keyCode == KeyCodes.KEY_PAGEUP) {
			pager.previousPage();
			// Не ищем, если пользователь просто передвигает курсор внутри поля для поиска
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
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<ValueType> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public ValueType getValue() {
		return value;
	}

	@Override
	public void setValue(ValueType value) {
		setValue(value, false);
	}

	abstract protected String valueToString(ValueType value);

	@Override
	public void setValue(ValueType value, boolean b) {
		if (this.value != null ? !this.value.equals(value) : value != null) {
			this.value = value;

			if (b) {
				ValueChangeEvent.fire(DictionaryPickerWidget.this, value);
			}
		}
	}

	public void clear() {
		txtFind.setValue("");
		String pattern = dataProvider.getSearchPattern();
		dataProvider.setSearchPattern("");
		if ((pattern == null || pattern.isEmpty()) && cellTable.getVisibleRange().getStart() != 0) {
			pager.firstPage();
		} else if (pattern != null && !pattern.isEmpty()) {
			dataProvider.load(cellTable.getVisibleRange());
		}
	}
}
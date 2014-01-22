package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client;

import com.aplana.sbrf.taxaccounting.web.widget.datepicker.CustomDateBox;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client.RefBookPickerWidgetPresenter.MyView;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.RefBookItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ShowRangeEvent;
import com.google.gwt.event.logical.shared.ShowRangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.client.DateTimeFormatRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.gwt.view.client.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
	DataGrid<RefBookItem> cellTable;

	@UiField
	TextBox txtFind;

	@UiField
    CustomDateBox version;

	@UiField
	FlexiblePager pager;

    /** Параметры инициализации */
    private Date startDate;
    private Date endDate;
    private long refBookAttrId;
    private String filter;

    private HashMap<RefBookItemTextColumn, Integer> sortableColumns;

    private RefBookItem defaultRefBookItem = null;
	
	private AsyncDataProvider<RefBookItem> dataProvider = new AsyncDataProvider<RefBookItem>() {
		@Override
		public void onRangeChanged(HasData<RefBookItem> display) {
			Range range = display.getVisibleRange();
			uiHandlers.rangeChanged(range.getStart(), range.getLength());
		}
	};
	
	public RefBookPickerWidget() {
        sortableColumns= new HashMap<RefBookItemTextColumn, Integer>();
		initWidget(binder.createAndBindUi(this));
		new RefBookPickerWidgetPresenter(this);

        version.addValueChangeHandler(new ValueChangeHandler<Date>()
        {
            @Override
            public void onValueChange(final ValueChangeEvent<Date> dateValueChangeEvent)
            {
                Date selectedDate = dateValueChangeEvent.getValue();
                if ((startDate != null && selectedDate.before(startDate)) || (endDate != null && selectedDate.after(endDate))) {
                    version.setValue(startDate, false);
                }
                uiHandlers.init(refBookAttrId, filter, version.getValue());
            }
        });
        version.getDatePicker().addShowRangeHandler(new ShowRangeHandler<Date>() {
            @Override
            public void onShowRange(final ShowRangeEvent<Date> dateShowRangeEvent) {
                Date d = new Date(dateShowRangeEvent.getStart().getTime());
                while (d.before(dateShowRangeEvent.getEnd())) {
                    if ((startDate != null && d.before(startDate)) || (endDate != null && d.after(endDate))) {
                        version.getDatePicker().setTransientEnabledOnDates(false, d);
                    }
                    CalendarUtil.addDaysToDate(d, 1);
                }
            }
        });
		
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

        cellTable.addColumnSortHandler(new ColumnSortEvent.Handler() {
            @Override
            public void onColumnSort(ColumnSortEvent event) {
                uiHandlers.onSort(sortableColumns.get(event.getColumn()), event.isSortAscending());
            }
        });

		pager.setDisplay(cellTable);
		pager.setPageSize(15);

		dataProvider.addDataDisplay(cellTable);
		txtFind.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					uiHandlers.searche();
				}
			}
		});
	}

	public void focus() {
		txtFind.setFocus(true);
		txtFind.setCursorPos(txtFind.getText().length());
	}


	@UiHandler("clearButton")
	void onBtnClearClick(ClickEvent event) {
		uiHandlers.clearValue();
        defaultRefBookItem = null;
	}
	
	@UiHandler("searchButton")
	void onSearchButtonClick(ClickEvent event) {
		uiHandlers.searche();
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
	public void setAcceptableValues(long refBookAttrId, Date startDate, Date endDate) {
        setInitValues(refBookAttrId, null, startDate, endDate);
	}

	@Override
	public void setAcceptableValues(long refBookAttrId, String filter,
			Date startDate, Date endDate) {
        setInitValues(refBookAttrId, filter, startDate, endDate);
	}

    private void setInitValues(long refBookAttrId, String filter,
                          Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.refBookAttrId = refBookAttrId;
        this.filter = filter;
        this.version.setValue(endDate != null ? endDate : startDate);
        uiHandlers.init(refBookAttrId, filter, version.getValue());
    }

	@Override
	public void setHeaders(List<String> headers) {
        for (int i = cellTable.getColumnCount()-1; i >= 0; i--) {
            cellTable.removeColumn(i);
        }
		for (int i = 0; i < headers.size(); i++) {
            RefBookItemTextColumn refBookItemTextColumn = new RefBookItemTextColumn(i);
            refBookItemTextColumn.setSortable(true);
            sortableColumns.put(refBookItemTextColumn, i);
			cellTable.addColumn(refBookItemTextColumn, headers.get(i));
		}
	}

	@Override
	public void refreshDataAndGoToFirstPage() {
		if (pager.getPage() != 0){
			pager.firstPage();
		} else {
			cellTable.setVisibleRangeAndClearData(cellTable.getVisibleRange(), true);
		}
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

    @Override
    public String getOtherDereferenceValue(String alias){
        return uiHandlers.getOtherDereferenceValue(alias);
    }

    @Override
    public String getOtherDereferenceValue(Long attrId){
        return uiHandlers.getOtherDereferenceValue(attrId);
    }

    @UiHandler("ok")
    void onBtnOkClick(ClickEvent event) {
        if (uiHandlers.getValue() != null) {
            defaultRefBookItem = selectionModel.getLastSelectedObject();
            widgetFireChangeEvent(uiHandlers.getValue());
        }
    }

    @UiHandler("cancel")
    void onBtnCancelClick(ClickEvent event) {
        if (defaultRefBookItem != null) {
            selectionModel.setSelected(defaultRefBookItem, true);
            uiHandlers.onSelectionChange();
            widgetFireChangeEvent(defaultRefBookItem.getId());
        } else {
            uiHandlers.clearValue();
        }
    }
}
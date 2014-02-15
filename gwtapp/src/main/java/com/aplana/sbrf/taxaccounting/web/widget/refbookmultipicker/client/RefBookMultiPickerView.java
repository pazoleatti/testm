package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.RefBookItem;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ShowRangeEvent;
import com.google.gwt.event.logical.shared.ShowRangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.gwt.view.client.*;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

/**
 * Представление компонента для выбора из линейного справочника
 *
 * @author sgoryachkin
 */
public class RefBookMultiPickerView extends ViewWithUiHandlers<RefBookMultiPickerUiHandlers>
        implements RefBookMultiPickerPresenter.MyView, RefBookView {

    interface Binder extends UiBinder<Widget, RefBookMultiPickerView> {
    }

    private static Binder binder = GWT.create(Binder.class);

    @UiField
    DataGrid<RefBookItem> cellTable;

    @UiField
    TextBox txtFind;

    @UiField
    DateMaskBoxPicker version;

    @UiField
    FlexiblePager pager;

    @UiField
    Button okButton;

    @UiField
    Label selectionCountLabel;

    private Boolean multiSelect = false;
    // так как развыделение асинхронное используем флаг для создания события измнеения
    private Boolean isClearEvent = false;

    private Long refBookAttrId;
    private String filter;
    private Date startDate;
    private Date endDate;

    private HashMap<RefBookItemTextColumn, Integer> sortableColumns = new HashMap<RefBookItemTextColumn, Integer>();
    private Set<RefBookItem> prevSelectedItems = new HashSet<RefBookItem>();

    private AbstractDataProvider<RefBookItem> dataProvider;
    private final SetSelectionModel<RefBookItem> selectionModel;

    public RefBookMultiPickerView() {
        this(false, null);
    }

    public RefBookMultiPickerView(final boolean multiSelect) {
        this(multiSelect, null);
    }

    public RefBookMultiPickerView(final boolean multiSelect, AbstractDataProvider<RefBookItem> provider) {
        this.multiSelect = multiSelect;
        // Определеяем провайдер данных.
        // Либо по умолчанию или свой.
        if (provider != null) {
            this.dataProvider = provider;
        } else {
            dataProvider = new AsyncDataProvider<RefBookItem>(RefBookPickerUtils.KEY_PROVIDER) {
                @Override
                public void onRangeChanged(HasData<RefBookItem> display) {
                    Range range = display.getVisibleRange();
                    getUiHandlers().rangeChanged(range.getStart(), range.getLength());
                }
            };
        }

        selectionModel = multiSelect ?
                new MultiSelectionModel<RefBookItem>(RefBookPickerUtils.KEY_PROVIDER){
                    protected boolean isEventScheduled() {
                        return false;
                    }
                    protected void scheduleSelectionChangeEvent() {
                        fireSelectionChangeEvent();
                    }
                } :
                new SingleSelectionModel<RefBookItem>(RefBookPickerUtils.KEY_PROVIDER){
                    protected boolean isEventScheduled() {
                        return false;
                    }

                    protected void scheduleSelectionChangeEvent() {
                        fireSelectionChangeEvent();
                    }
                };

        initWidget(binder.createAndBindUi(this));
        new RefBookMultiPickerPresenter(this);

        version.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(final ValueChangeEvent<Date> dateValueChangeEvent) {
                clearSelected(false);
                Date d = dateValueChangeEvent.getValue();
                if (RefBookPickerUtils.isCorrectDate(startDate, endDate, d)) {
                    version.setValue(startDate, false);
                }
                getUiHandlers().reload(version.getValue());
            }
        });
        version.getDatePicker().addShowRangeHandler(new ShowRangeHandler<Date>() {
            @Override
            public void onShowRange(final ShowRangeEvent<Date> dateShowRangeEvent) {
                Date d = new Date(dateShowRangeEvent.getStart().getTime());
                while (d.before(dateShowRangeEvent.getEnd())) {
                    if (RefBookPickerUtils.isCorrectDate(startDate, endDate, d)) {
                        version.getDatePicker().setTransientEnabledOnDates(false, d);
                    }
                    CalendarUtil.addDaysToDate(d, 1);
                }
            }
        });

        DefaultSelectionEventManager<RefBookItem> multiSelectManager = DefaultSelectionEventManager.createCustomManager(
                new DefaultSelectionEventManager.CheckboxEventTranslator<RefBookItem>(0) {
                    public boolean clearCurrentSelection(CellPreviewEvent<RefBookItem> event) {
                        return false;
                    }

                    public DefaultSelectionEventManager.SelectAction translateSelectionEvent(CellPreviewEvent<RefBookItem> event) {
                        return DefaultSelectionEventManager.SelectAction.TOGGLE;
                    }
                });

        if (multiSelect) {
            cellTable.setSelectionModel(selectionModel, multiSelectManager);
        } else {
            cellTable.setSelectionModel(selectionModel);
        }

        cellTable.addCellPreviewHandler(new CellPreviewEvent.Handler<RefBookItem>() {
            @Override
            public void onCellPreview(CellPreviewEvent<RefBookItem> event) {
                if (BrowserEvents.MOUSEOVER.equals(event.getNativeEvent().getType())) {
                    Element cellElement = event.getNativeEvent().getEventTarget().cast();
                    cellElement.setTitle(cellElement.getInnerText());
                }
            }
        });

        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (isClearEvent) {
                    isClearEvent = false;
                    widgetFireChangeEvent(getSelectedIds());
                }
                selectionCountLabel.setText("Выбрано: " + selectionModel.getSelectedSet().size());
                okButton.setEnabled(!selectionModel.getSelectedSet().isEmpty());
            }
        });

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
                //focus();
            }
        });

        cellTable.addColumnSortHandler(new ColumnSortEvent.Handler() {
            @Override
            public void onColumnSort(ColumnSortEvent event) {
                getUiHandlers().onSort(sortableColumns.get(event.getColumn()), event.isSortAscending());
            }
        });

        cellTable.setPageSize(pager.getPageSize());
        pager.setDisplay(cellTable);

        dataProvider.addDataDisplay(cellTable);

        txtFind.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    getUiHandlers().search();
                }
            }
        });
    }

    @UiHandler("okButton")
    void onBtnOkClick(ClickEvent event) {
        prevSelectedItems = new HashSet<RefBookItem>(selectionModel.getSelectedSet());
        widgetFireChangeEvent(getSelectedIds());
    }

    @UiHandler("cancelButton")
    void onBtnCancelClick(ClickEvent event) {
        //TODO aivanov сделать нормальную отмену, а не как очистку.
//        if (prevSelectedItems != null && !prevSelectedItems.isEmpty()) {
//            //widgetFireChangeEvent(prevSelectedItems);
//        } else {
//            clearSelected(true);
//        }

        clearSelected(true);
    }

    @UiHandler("clearButton")
    void onBtnClearClick(ClickEvent event) {
        clearSelected(true);
    }

    @UiHandler("searchButton")
    void onSearchButtonClick(ClickEvent event) {
        getUiHandlers().search();
    }

    @Override
    public void setRowData(int start, List<RefBookItem> values, int size) {
        //Данный метод дергается столько если провайдер AsyncDataProvider
        // Определяется в конструкторе
        ((AsyncDataProvider) dataProvider).updateRowData(start, values);
        ((AsyncDataProvider) dataProvider).updateRowCount(size, true);

        okButton.setEnabled(!selectionModel.getSelectedSet().isEmpty());
    }

    @Override
    public List<Long> getValue() {
        return getSelectedIds();
    }

    @Override
    public Long getSingleValue() {
        return (selectionModel.getSelectedSet() != null && !selectionModel.getSelectedSet().isEmpty() ? getSelectedIds().get(0) : null);
    }

    @Override
    public void setValue(Long value) {
        if (value != null) {
            clearSelected(false);
            getUiHandlers().loadingForSelection(Arrays.asList(value));
        }
    }

    @Override
    public void trySetSelection(List<RefBookItem> values) {
        if (values != null) {
            for (RefBookItem item : values) {
                selectionModel.setSelected(item, true);
            }
        }

        widgetFireChangeEvent(getSelectedIds());
    }

    @Override
    public void setValue(List<Long> value) {
        setValue(value, false);
    }

    @Override
    public void setValue(List<Long> value, boolean fireEvent) {
        if (value != null) {
            clearSelected(false);
            if (!value.isEmpty()) {
                getUiHandlers().loadingForSelection(value);
            }
        }
    }

    public void clear() {
        txtFind.setValue("");
    }

    public void focus() {
        txtFind.setFocus(true);
        txtFind.setCursorPos(txtFind.getText().length());
    }

    private void clearSelected(boolean fireChangeEvent) {
        selectionModel.clear();
        isClearEvent = fireChangeEvent;
        prevSelectedItems.clear();
    }

    private List<Long> getSelectedIds() {
        List<Long> longs = new LinkedList<Long>();
        for (RefBookItem item : selectionModel.getSelectedSet()) {
            longs.add(item.getId());
        }
        return longs;
    }

    @Override
    public List<RefBookItem> getSelectionValues() {
        return new LinkedList<RefBookItem>(selectionModel.getSelectedSet());
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
    public Date getVersion() {
        return version.getValue();
    }

    @Override
    public void load() {
        if (version.getValue() == null) {
            this.version.setValue(endDate != null ? endDate : startDate);
        }
        getUiHandlers().init(refBookAttrId, filter, version.getValue());
    }

    @Override
    public void load(long refBookAttrId, String filter, Date startDate, Date endDate) {
        this.refBookAttrId = refBookAttrId;
        this.filter = filter;
        this.startDate = startDate;
        this.endDate = endDate;
        if (!RefBookPickerUtils.isCorrectDate(startDate, endDate, version.getValue())) {
            this.version.setValue(endDate != null ? endDate : startDate);
        }
        getUiHandlers().init(refBookAttrId, filter, version.getValue());
    }

    @Override
    public void setHeaders(Map<String, Integer> headers) {
        for (int i = cellTable.getColumnCount() - 1; i >= 0; i--) {
            cellTable.removeColumn(i);
        }
        if (multiSelect) {
            // добавить колонку с чекбоксами
            Column<RefBookItem, Boolean> rowSelectColumn = new Column<RefBookItem, Boolean>(new CheckboxCell(true, false)) {
                @Override
                public Boolean getValue(RefBookItem object) {
                    Boolean result;
                    if (object == null || object.getId() == null) {
                        result = null;
                    } else {
                        result = selectionModel.isSelected(object);
                    }
                    return result;
                }
            };
            rowSelectColumn.setSortable(false);
            cellTable.addColumn(rowSelectColumn, "");
            cellTable.setColumnWidth(rowSelectColumn, 5, Style.Unit.EM);
        }

        int i = 0;
        for (Map.Entry<String, Integer> entry : headers.entrySet()) {
            RefBookItemTextColumn refBookItemTextColumn = new RefBookItemTextColumn(i, true);
            sortableColumns.put(refBookItemTextColumn, i);
            cellTable.addColumn(refBookItemTextColumn, entry.getKey());
            cellTable.setColumnWidth(refBookItemTextColumn, entry.getValue(), Style.Unit.PC);
            i++;
        }
    }

    @Override
    public void refreshDataAndGoToFirstPage() {
        if (pager.getPage() != 0) {
            pager.firstPage();
        } else {
            cellTable.setVisibleRangeAndClearData(cellTable.getVisibleRange(), true);
        }
    }

    @Override
    public String getDereferenceValue() {
        List<RefBookItem> selectedItems = getSelectionValues();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedItems.size(); i++) {
            RefBookItem item = selectedItems.get(i);
            sb.append(item.getDereferenceValue());
            if (i < selectedItems.size() - 1) {
                sb.append("; ");
            }
        }
        return sb.toString();
    }

    @Override
    public String getOtherDereferenceValue(String alias) {
        List<RefBookItem> selectedItems = getSelectionValues();
        if (selectedItems != null && !selectedItems.isEmpty()) {
            return RefBookPickerUtils.getDereferenceValue(selectedItems.get(0).getRefBookRecordDereferenceValues(), alias);
        }
        return null;
    }

    @Override
    public String getOtherDereferenceValue(Long attrId) {
        List<RefBookItem> selectedItems = getSelectionValues();
        if (selectedItems != null && !selectedItems.isEmpty()) {
            return RefBookPickerUtils.getDereferenceValue(selectedItems.get(0).getRefBookRecordDereferenceValues(), attrId);
        }
        return null;
    }

//    /**
//     * Получить разименованные значения выбранных строк в виде строки через ";".
//     *
//     * @param key           ключ
//     * @param selectedItems список выбрнных элементов
//     */
//    private String getValueByKey(Integer key, List<RefBookItem> selectedItems) {
//        if (key == null) {
//            return null;
//        }
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < selectedItems.size(); i++) {
//            RefBookItem item = selectedItems.get(i);
//            sb.append(item.getValues().get(key));
//            if (i < selectedItems.size() - 1) {
//                sb.append("; ");
//            }
//        }
//        return sb.toString();
//    }

    @Override
    public void widgetFireChangeEvent(List<Long> value) {
        ValueChangeEvent.fire(RefBookMultiPickerView.this, value);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<Long>> handler) {
        return asWidget().addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        asWidget().fireEvent(event);
    }

    @Override
    public Long getAttributeId() {
        return refBookAttrId;
    }

    @Override
    public void setAttributeId(long refBookAttrId) {
        this.refBookAttrId = refBookAttrId;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    @Override
    public void setPeriodDates(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.version.setValue(endDate != null ? endDate : startDate);
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }


    @Override
    public String getFilter() {
        return filter;
    }

    @Override
    public void setFilter(String filter) {
        this.filter = filter;
    }
}
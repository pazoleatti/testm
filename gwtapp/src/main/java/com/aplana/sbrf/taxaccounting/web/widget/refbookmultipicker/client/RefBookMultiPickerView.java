package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookMultiPickerViewPresenter.MyView;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.RefBookItem;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ShowRangeEvent;
import com.google.gwt.event.logical.shared.ShowRangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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

import java.util.*;

/**
 * @author sgoryachkin
 *
 */
public class RefBookMultiPickerView extends Composite implements RefBookMultiPicker, MyView {

    private final SetSelectionModel<RefBookItem> selectionModel;
    private Boolean multiSelect = false;

    private List<Long> valuesId = new ArrayList<Long>();
    private Map<Long, RefBookItem> itemsMap = new HashMap<Long, RefBookItem>();

    private RefBookMultiPickerViewUiHandlers uiHandlers;

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
    Button ok;

    /** Параметры инициализации */
    private Date startDate;
    private Date endDate;
    private long refBookAttrId;
    private String filter;

    private HashMap<RefBookItemTextColumn, Integer> sortableColumns;

    private List<RefBookItem> defaultRefBookItems = new ArrayList<RefBookItem>();

    private AsyncDataProvider<RefBookItem> dataProvider = new AsyncDataProvider<RefBookItem>() {
        @Override
        public void onRangeChanged(HasData<RefBookItem> display) {
            Range range = display.getVisibleRange();
            uiHandlers.rangeChanged(range.getStart(), range.getLength());
        }
    };

    public RefBookMultiPickerView() {
        this(false);
    }

    public RefBookMultiPickerView(final boolean multiSelect) {
        this.multiSelect = multiSelect;
        selectionModel = (multiSelect ?
                new MultiSelectionModel<RefBookItem>() : new SingleSelectionModel<RefBookItem>());
        sortableColumns= new HashMap<RefBookItemTextColumn, Integer>();
        initWidget(binder.createAndBindUi(this));
        new RefBookMultiPickerViewPresenter(this);

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

        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (!multiSelect) {
                    valuesId.clear();
                    itemsMap.clear();
                }
                ok.setEnabled(!selectionModel.getSelectedSet().isEmpty());
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

        cellTable.setPageSize(pager.getPageSize());
        pager.setDisplay(cellTable);

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
        clearSelected();
    }

    private void clearSelected() {
        itemsMap.clear();
        valuesId.clear();
        defaultRefBookItems.clear();
        for (RefBookItem i : selectionModel.getSelectedSet()) {
            selectionModel.setSelected(i, false);
        }
        widgetFireChangeEvent(valuesId);
    }

    @UiHandler("searchButton")
    void onSearchButtonClick(ClickEvent event) {
        uiHandlers.searche();
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<Long>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public List<Long> getValue() {
        return valuesId;
    }

    @Override
    public Long getSingleValue() {
        return (valuesId != null && !valuesId.isEmpty() ? valuesId.get(0) : null);
    }

    @Override
    public void setValue(Long value) {
        List<Long> list = null;
        if (value != null) {
            list = new ArrayList<Long>();
            list.add(value);
        }
        setValue(list);
    }

    @Override
    public void setValue(List<Long> value) {
        setValue(value, false);
    }

    @Override
    public void setValue(List<Long> value, boolean fireEvent) {
        if (value != null) {
            valuesId = value;
        } else {
            valuesId.clear();
        }
        // убрать выделения предыдущих выборов
        defaultRefBookItems.clear();
        itemsMap.clear();
        for (RefBookItem i : cellTable.getVisibleItems()) {
            boolean isContain = valuesId.contains(i.getId());
            if (isContain) {
                defaultRefBookItems.add(i);
                itemsMap.put(i.getId(), i);
            }
            selectionModel.setSelected(i, isContain);
        }
        ok.setEnabled(!valuesId.isEmpty());
        if (fireEvent) {
            widgetFireChangeEvent(value);
        }
    }

    public void clear() {
        txtFind.setValue("");
    }

    @Override
    public void addToSlot(Object slot, IsWidget content) {

    }

    @Override
    public void removeFromSlot(Object slot, IsWidget content) {

    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {

    }

    @Override
    public void setRowData(int start, List<RefBookItem> values, int size) {
        updateSelectionItems();

        dataProvider.updateRowData(start, values);
        dataProvider.updateRowCount(size, true);

        ok.setEnabled(!valuesId.isEmpty());
        if (values.isEmpty()) {
            return;
        }
        for (RefBookItem i : values) {
            if (this.valuesId.contains(i.getId())) {
                selectionModel.setSelected(i, true);
            }
        }
    }

    /** Обновляет выбранные элементы при загрузки порции данных или при нажатии кнопики "выбрать". */
    private void updateSelectionItems() {
        for (RefBookItem i : cellTable.getVisibleItems()) {
            valuesId.remove(i.getId());
            itemsMap.remove(i.getId());
        }
        for (RefBookItem i : selectionModel.getSelectedSet()) {
            if (!valuesId.contains(i.getId())) {
                valuesId.add(i.getId());
            }
            itemsMap.put(i.getId(), i);
        }
    }

    @Override
    public void setUiHandlers(RefBookMultiPickerViewUiHandlers uiHandlers) {
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
                    if(object == null || object.getId() == null) {
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
        for(Map.Entry<String, Integer> entry : headers.entrySet()){
            RefBookItemTextColumn refBookItemTextColumn = new RefBookItemTextColumn(i);
            refBookItemTextColumn.setSortable(true);
            sortableColumns.put(refBookItemTextColumn, i);
            cellTable.addColumn(refBookItemTextColumn, entry.getKey());
            cellTable.setColumnWidth(refBookItemTextColumn, entry.getValue(), Style.Unit.PC);
            i++;
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
    public void widgetFireChangeEvent(List<Long> value) {
        ValueChangeEvent.fire(RefBookMultiPickerView.this, value);
    }

    @Override
    public HandlerRegistration widgetAddValueHandler(ValueChangeHandler<List<Long>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public List<RefBookItem> getSelectionValues() {
        return new ArrayList<RefBookItem>(itemsMap.values());
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
    public String getOtherDereferenceValue(String alias){
        List<RefBookItem> selectedItems = getSelectionValues();
        if (selectedItems != null && !selectedItems.isEmpty() && alias != null && !alias.isEmpty()) {
            Integer key = null;
            List<String> attrAliases = selectedItems.get(0).getValuesAttrAlias();
            for (int i = 0; i < attrAliases.size(); i++) {
                if (alias.equals(attrAliases.get(i))) {
                    key = i;
                    break;
                }
            }
            return getValueByKey(key, selectedItems);
        }
        return null;
    }

    @Override
    public String getOtherDereferenceValue(Long attrId){
        List<RefBookItem> selectedItems = getSelectionValues();
        if (selectedItems != null && !selectedItems.isEmpty() && attrId != null) {
            Integer key = null;
            List<Long> attrIds = selectedItems.get(0).getValuesAttrId();
            for (int i = 0; i < attrIds.size(); i++) {
                if (attrId.equals(attrIds.get(i))) {
                    key = i;
                    break;
                }
            }
            return getValueByKey(key, selectedItems);
        }
        return null;
    }

    /**
     * Получить разименованные значения выбранных строк в виде строки через ";".
     *
     * @param key ключ
     * @param selectedItems список выбрнных элементов
     */
    private String getValueByKey(Integer key, List<RefBookItem> selectedItems) {
        if (key == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedItems.size(); i++) {
            RefBookItem item = selectedItems.get(i);
            sb.append(item.getValues().get(key));
            if (i < selectedItems.size() - 1) {
                sb.append("; ");
            }
        }
        return sb.toString();
    }

    @UiHandler("ok")
    void onBtnOkClick(ClickEvent event) {
        updateSelectionItems();
        if (valuesId != null && !valuesId.isEmpty()) {
            defaultRefBookItems = new ArrayList<RefBookItem>(itemsMap.values());
            widgetFireChangeEvent(valuesId);
        }
    }

    @UiHandler("cancel")
    void onBtnCancelClick(ClickEvent event) {
        if (defaultRefBookItems != null && !defaultRefBookItems.isEmpty()) {
            itemsMap.clear();
            valuesId.clear();
            for (RefBookItem i : defaultRefBookItems) {
                valuesId.add(i.getId());
                itemsMap.put(i.getId(), i);
            }
            widgetFireChangeEvent(valuesId);
        } else {
            clearSelected();
        }
    }
}
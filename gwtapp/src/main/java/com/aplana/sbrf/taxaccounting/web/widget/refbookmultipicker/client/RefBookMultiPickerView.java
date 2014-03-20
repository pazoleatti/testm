package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.PickerState;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.RefBookItem;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

import static com.google.gwt.view.client.DefaultSelectionEventManager.*;

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
    GenericDataGrid<RefBookItem> cellTable;

    @UiField
    FlexiblePager pager;

    private Set<Long> longList = new LinkedHashSet<Long>();

    // так как развыделение асинхронное используем флаг для создания события измнеения
    private Boolean isClearEvent = false;
    private Boolean isEnabledFireChangeEvent = true;
    private Boolean multiSelect = false;

    private HandlerRegistration selectionHandlerRegistration;
    private HashMap<RefBookItemTextColumn, Integer> sortColumns = new HashMap<RefBookItemTextColumn, Integer>();

    private SetSelectionModel<RefBookItem> selectionModel;
    private AsyncDataProvider<RefBookItem> dataProvider =
            new AsyncDataProvider<RefBookItem>(RefBookPickerUtils.KEY_PROVIDER) {
                @Override
                public void onRangeChanged(HasData<RefBookItem> display) {
                    Range range = display.getVisibleRange();
                    getUiHandlers().rangeChanged(range.getStart(), range.getLength());
                }
            };
    private DefaultSelectionEventManager<RefBookItem> multiSelectManager = createCustomManager(
            new CheckboxEventTranslator<RefBookItem>(0) {
                public boolean clearCurrentSelection(CellPreviewEvent<RefBookItem> event) {
                    return false;
                }

                public SelectAction translateSelectionEvent(CellPreviewEvent<RefBookItem> event) {
                    return SelectAction.TOGGLE;
                }
            });

    public RefBookMultiPickerView() {
        this(false);
    }

    public RefBookMultiPickerView(final boolean multiSelect) {
        this.multiSelect = multiSelect;

        selectionModel = getSelectionModel(multiSelect);

        initWidget(binder.createAndBindUi(this));

        new RefBookMultiPickerPresenter(this);

        if (multiSelect) {
            cellTable.setSelectionModel(selectionModel, multiSelectManager);
        } else {
            cellTable.setSelectionModel(selectionModel);
        }

        selectionHandlerRegistration = selectionModel.addSelectionChangeHandler(
                new SelectionChangeEvent.Handler() {
                    @Override
                    public void onSelectionChange(SelectionChangeEvent event) {
                        onSelection();
                    }
                });

        cellTable.addCellPreviewHandler(new CellPreviewEvent.Handler<RefBookItem>() {
            @Override
            public void onCellPreview(CellPreviewEvent<RefBookItem> event) {
                if (BrowserEvents.MOUSEOVER.equals(event.getNativeEvent().getType())) {
                    Element cellElement = event.getNativeEvent().getEventTarget().cast();
                    cellElement.setTitle(cellElement.getInnerText());
                }
            }
        });

        cellTable.addColumnSortHandler(new ColumnSortEvent.Handler() {
            @Override
            public void onColumnSort(ColumnSortEvent event) {
                getUiHandlers().onSort(sortColumns.get(event.getColumn()), !event.isSortAscending());
            }
        });
        cellTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED);
        //        cellTable.addLoadingStateChangeHandler(new LoadingStateChangeEvent.Handler() {
//            @Override
//            public void onLoadingStateChanged(LoadingStateChangeEvent event) {
//                if (event.getLoadingState() == LoadingStateChangeEvent.LoadingState.LOADED) {
//                    //заглушка
//                }
//            }
//        });

//        cellTable.addRangeChangeHandler(new RangeChangeEvent.Handler() {
//            public void onRangeChange(RangeChangeEvent event) {
//                //заглушка
//            }
//        });

        cellTable.setPageSize(pager.getPageSize());
        pager.setDisplay(cellTable);
        dataProvider.addDataDisplay(cellTable);
    }

    private SetSelectionModel<RefBookItem> getSelectionModel(boolean multiSelect) {
        return multiSelect ?
                new MultiSelectionModel<RefBookItem>(RefBookPickerUtils.KEY_PROVIDER) {
                    // Переопределение методов - попытка убрать задержку при сеттинге селекта
                    @Override
                    protected boolean isEventScheduled() {
                        return false;
                    }
                    @Override
                    protected void scheduleSelectionChangeEvent() {
                        fireSelectionChangeEvent();
                    }
                } :
                new SingleSelectionModel<RefBookItem>(RefBookPickerUtils.KEY_PROVIDER) {
                    // Переопределение методов - попытка убрать задержку при сеттинге селекта
                    @Override
                    public boolean isEventScheduled() {
                        return false;
                    }
                    @Override
                    public void scheduleSelectionChangeEvent() {
                        fireSelectionChangeEvent();
                    }
                };
    }

    @Override
    public void setRowData(int start, List<RefBookItem> values, int size) {
        dataProvider.updateRowData(start, values);
        dataProvider.updateRowCount(size, true);
    }

    public void trySelectValues(Set<Long> value) {
        clearSelected(false);
        if (!value.isEmpty()) {
            getUiHandlers().loadingForSelection(value);
        } else {
            widgetFireChangeEvent(longList);
        }
    }

    @Override
    public void setSelection(List<RefBookItem> values) {
        if (values != null) {
            if (!values.isEmpty()) {
                clearSelected(false);
                for (int i = 0; i < values.size(); i++) {
                    RefBookItem item = values.get(i);
                    if (i != values.size() - 1) {
                        // Пока не последний элемент событие изменения не пробрасываем
                        isEnabledFireChangeEvent = false;
                    }
                    selectionModel.setSelected(item, true);
                }
            } else {
                clearSelected(true);
            }
        }
    }

    private void onSelection() {
        if (!isEnabledFireChangeEvent) {
            isEnabledFireChangeEvent = true;
        } else {
            widgetFireChangeEvent(getSelectedIds());
        }
    }

    @Override
    public void load(PickerState pickerState) {
        getUiHandlers().init(pickerState);
    }

    @Override
    public void reload() {

    }

    @Override
    public void reload(List<Long> needToSelectIds) {

    }

    @Override
    public void find(String searchPattern) {
        getUiHandlers().find(searchPattern);
        refresh();
    }

    @Override
    public void reloadOnDate(Date version) {
        clearSelected(true);
        getUiHandlers().reload(version);
    }

    @Override
    public void clearSelected(boolean fireChangeEvent) {
        longList.clear();
        if (selectionModel.getSelectedSet().size() != 0) {
            isEnabledFireChangeEvent = fireChangeEvent;
            selectionModel.clear();
        }
    }

    @Override
    public Set<Long> getSelectedIds() {
        longList.clear();
        for (RefBookItem item : getSelectedSet()) {
            longList.add(item.getId());
        }
        return longList;
    }

    private Set<RefBookItem> getSelectedSet() {
        return selectionModel.getSelectedSet();
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
                    return (object == null || object.getId() == null) ? null : selectionModel.isSelected(object);
                }
            };
            rowSelectColumn.setSortable(false);
            cellTable.addColumn(rowSelectColumn, "");
            cellTable.setColumnWidth(rowSelectColumn, 5, Style.Unit.EM);
        }

        int i = 0;
        for (Map.Entry<String, Integer> entry : headers.entrySet()) {
            RefBookItemTextColumn refBookItemTextColumn = new RefBookItemTextColumn(i, true);
            sortColumns.put(refBookItemTextColumn, i);
            cellTable.addResizableColumn(refBookItemTextColumn, entry.getKey());
            cellTable.setColumnWidth(refBookItemTextColumn, entry.getValue(), Style.Unit.PC);
            i++;
        }
    }

    @Override
    public void refresh() {
        if (asWidget().getElement().getOffsetWidth() > 0) {
            // когда вьха уже разместилась в дереве DOM у она получает физическую ширину
            // по ней и определем что таблица отображается на странице
            if (pager.getPage() != 0) {
                pager.firstPage();
            } else {
                cellTable.setVisibleRangeAndClearData(cellTable.getVisibleRange(), true);
            }
        }
    }

    @Override
    public String getDereferenceValue() {
        Set<RefBookItem> selectedItems = getSelectedSet();
        StringBuilder sb = new StringBuilder();
        if (selectedItems.size() > 0) {
            for (RefBookItem item : selectedItems) {
                sb.append(item.getDereferenceValue());
                sb.append("; ");
            }
            sb.deleteCharAt(sb.length() - 2);
        }
        return sb.toString();
    }

    @Override
    public String getOtherDereferenceValue(String alias) {
        Set<RefBookItem> selectedItems = getSelectedSet();
        if (selectedItems != null && !selectedItems.isEmpty()) {
            return RefBookPickerUtils.getDereferenceValue(selectedItems.iterator().next().getRefBookRecordDereferenceValues(), alias);
        }
        return null;
    }

    @Override
    public String getOtherDereferenceValue(Long attrId) {
        Set<RefBookItem> selectedItems = getSelectedSet();
        if (selectedItems != null && !selectedItems.isEmpty()) {
            return RefBookPickerUtils.getDereferenceValue(selectedItems.iterator().next().getRefBookRecordDereferenceValues(), attrId);
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
    public Boolean isMultiSelect() {
        return multiSelect;
    }

    @Override
    public void setMultiSelect(Boolean multiSelect) {
        this.multiSelect = multiSelect;

        clearSelected(true);

        selectionModel = getSelectionModel(multiSelect);
        if (multiSelect) {
            cellTable.setSelectionModel(selectionModel, multiSelectManager);
        } else {
            DefaultSelectionEventManager<RefBookItem> defaultSelectionEventManager = createDefaultManager();
            cellTable.setSelectionModel(selectionModel, defaultSelectionEventManager);
        }
        // удаляем регистрацию на уничтоженый менеджер выделений
        selectionHandlerRegistration.removeHandler();
        selectionHandlerRegistration = selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                onSelection();
            }
        });
    }

    public void widgetFireChangeEvent(Set<Long> value) {
        ValueChangeEvent.fire(RefBookMultiPickerView.this, value);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Set<Long>> handler) {
        return asWidget().addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        asWidget().fireEvent(event);
    }
}
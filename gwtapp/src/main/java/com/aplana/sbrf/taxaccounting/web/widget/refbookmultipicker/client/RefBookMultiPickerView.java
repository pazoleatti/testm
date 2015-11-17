package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.web.main.api.client.handler.DeferredInvokeHandler;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.event.CheckValuesCountHandler;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.PickerState;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookItem;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.utils.TextUtils;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.ui.Widget;
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

    private Boolean isEnabledFireChangeEvent = true;
    private Boolean multiSelect = false;

    private HandlerRegistration selectionHandlerRegistration;
    private Map<RefBookItemTextColumn, Integer> sortColumns = new HashMap<RefBookItemTextColumn, Integer>();
    private String filterText;
    private String singleColumn = null;
    private RefBookPicker mainWidget;

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
                @Override
                public boolean clearCurrentSelection(CellPreviewEvent<RefBookItem> event) {
                    return false;
                }

                @Override
                public SelectAction translateSelectionEvent(CellPreviewEvent<RefBookItem> event) {
                    return SelectAction.TOGGLE;
                }
            });

    public RefBookMultiPickerView() {
        this(false, null);
    }

    public RefBookMultiPickerView(final Boolean multiSelect, RefBookPicker refBookPicker) {
        this.multiSelect = multiSelect;
        this.mainWidget = refBookPicker;

        selectionModel = getSelectionModel(multiSelect != null && multiSelect);

        initWidget(binder.createAndBindUi(this));

        new RefBookMultiPickerPresenter(this);

        if (multiSelect != null && multiSelect) {
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
            getUiHandlers().loadingForSelection(value, null);
        } else {
            widgetFireChangeEvent(longList);
        }
    }

    @Override
    public void setSelection(List<RefBookItem> values) {
        if (values != null && !values.isEmpty()) {
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

    private void onSelection() {
        if (!isEnabledFireChangeEvent) {
            isEnabledFireChangeEvent = true;
        } else {
            widgetFireChangeEvent(getSelectedIds());
        }
    }

    @Override
    public void load(PickerState pickerState, boolean force) {
        filterText = pickerState.getSearchPattern();
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
        filterText = searchPattern;
        refresh(true);
    }

    @Override
    public void reloadOnDate(Date version) {
        clearSelected(true);
        getUiHandlers().reload(version);
    }

    @Override
    public void clearSelected(boolean fireChangeEvent) {
        longList.clear();
        if (!selectionModel.getSelectedSet().isEmpty()) {
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

    @Override
    public void setSingleColumn(String columnAlias) {
        this.singleColumn = columnAlias;
    }

    private Set<RefBookItem> getSelectedSet() {
        return selectionModel.getSelectedSet();
    }

    @Override
    public void setAttributes(List<RefBookAttribute> attributes) {
        for (int i = cellTable.getColumnCount() - 1; i >= 0; i--) {
            cellTable.removeColumn(i);
        }
        if (multiSelect != null && multiSelect) {
            // добавить колонку с чекбоксами
            Column<RefBookItem, Boolean> rowSelectColumn = new Column<RefBookItem, Boolean>(new CheckboxCell(true, false)) {
                @Override
                public Boolean getValue(RefBookItem object) {
                    return (object == null || object.getId() == null) ? null : selectionModel.isSelected(object);
                }
            };
            rowSelectColumn.setSortable(false);

            CheckboxCell checkboxCell = new CheckboxCell();
            Header<Boolean> headerCheckBox = new Header<Boolean>(checkboxCell) {
                @Override
                public Boolean getValue() {
                    return false;
                }
            };

            ValueUpdater<Boolean> valueUpdater = new ValueUpdater<Boolean>() {

                @Override
                public void update(Boolean value) {
                    if (value) {
                        selectAll(null);
                    } else {
                        clearSelected(true);
                    }
                }
            };
            headerCheckBox.setUpdater(valueUpdater);

            cellTable.addColumn(rowSelectColumn, headerCheckBox);
            cellTable.setColumnWidth(rowSelectColumn, 5, Style.Unit.EM);
        }

        int i = 0;
        for (RefBookAttribute attribute : attributes) {
            if (singleColumn == null || attribute.getAlias().equals(singleColumn)) {
                Cell<String> cell = new AbstractCell<String>() {
                    @Override
                    public void render(Context context, String value, SafeHtmlBuilder sb) {
                        if (value != null) {
                            if (filterText != null && !filterText.isEmpty()) {
                                String link = RegExp.compile(TextUtils.quote(filterText), "gi").replace(value, "<span style=\"color: #ff0000;\">$&</span>");
                                sb.appendHtmlConstant(link);
                            } else {
                                sb.appendHtmlConstant(value);
                            }
                        } else {
                            sb.append(SafeHtmlUtils.EMPTY_SAFE_HTML);
                        }
                    }
                };
                if (attribute.isVisible()) {
                    RefBookItemTextColumn refBookItemTextColumn = new RefBookItemTextColumn(i, true, cell);
                    sortColumns.put(refBookItemTextColumn, i);
                    cellTable.addResizableColumn(refBookItemTextColumn, attribute.getName());
                    cellTable.setColumnWidth(refBookItemTextColumn, attribute.getWidth(), Style.Unit.PC);
                }
            }
            i++;
        }
    }

    @Override
    public void refresh(boolean force) {
        int width = asWidget().getElement().getOffsetWidth();
        if (width > 0) {
            // когда вьха уже разместилась в дереве DOM у она получает физическую ширину
            // по ней и определем что таблица отображается на странице
            if (pager.getPage() != 0) {
                pager.firstPage();
            } else {
                //// Если данные еще не грузились или игнорим если "силой" грузим SBRFACCTAX-6844
                if (cellTable.getVisibleItemCount() == 0 || force) {
                    cellTable.setVisibleRangeAndClearData(cellTable.getVisibleRange(), true);
                }
            }
            if (cellTable.getColumnCount() > 0 && cellTable.getHeader(0) != null && cellTable.getHeader(0).getCell() instanceof CheckboxCell) {
                ((CheckboxCell) cellTable.getHeader(0).getCell()).clearViewData(false);
            }
        } else if (force) {
            // все равно очищаем таблицу даже если она не показывается,
            // потому как при открытии все равно будет происходит загрузка силой
            cellTable.setRowCount(0);
        }
    }

    @Override
    public void showVersionDate(boolean versioned) {
        mainWidget.showVersionDate(versioned);
    }

    @Override
    public String getDereferenceValue() {
        Set<RefBookItem> selectedItems = getSelectedSet();
        StringBuilder sb = new StringBuilder();
        if (!selectedItems.isEmpty()) {
            for (RefBookItem item : selectedItems) {
                sb.append(item.getDereferenceValue());
                sb.append("; ");
            }
            sb.deleteCharAt(sb.length() - 2);
        }
        return sb.toString();
    }

    @Override
    public String getOtherDereferenceValue(Long attrId) {
        Set<RefBookItem> selectedItems = getSelectedSet();
        if (selectedItems != null && !selectedItems.isEmpty()) {
            return RefBookPickerUtils.getDereferenceValue(selectedItems.iterator().next().getRefBookRecordDereferenceValues(), attrId);
        }
        return null;
    }

    @Override
    public String getOtherDereferenceValue(Long attrId, Long attrId2) {
        Set<RefBookItem> selectedItems = getSelectedSet();
        if (selectedItems != null && !selectedItems.isEmpty()) {
            return RefBookPickerUtils.getDereferenceValue(selectedItems.iterator().next().getRefBookRecordDereferenceValues(), attrId, attrId2);
        }
        return null;
    }

    @Override
    public void setMultiSelect(Boolean multiSelect) {
        this.multiSelect = multiSelect;

        clearSelected(true);

        selectionModel = getSelectionModel(multiSelect);
        if (multiSelect) {
            cellTable.setSelectionModel(selectionModel, multiSelectManager);
        } else {
            cellTable.setSelectionModel(selectionModel, DefaultSelectionEventManager.<RefBookItem>createDefaultManager());
        }
        // удаляем регистрацию на уничтоженый менеджер выделений
        selectionHandlerRegistration.removeHandler();
        selectionHandlerRegistration = selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) { onSelection(); }
        });
    }

    @Override
    public void selectAll(DeferredInvokeHandler handler) {
        if (multiSelect) {
            for (RefBookItem refBookItem : cellTable.getVisibleItems()) {
                selectionModel.setSelected(refBookItem, true);
            }
            widgetFireChangeEvent(getSelectedIds());
            if (handler != null) {
                handler.onInvoke();
            }
        }
    }

    @Override
    public void unselectAll(DeferredInvokeHandler handler) {
        if (multiSelect) {
            clearSelected(true);
            if (handler != null) {
                handler.onInvoke();
            }
            if (cellTable.getColumnCount() > 0 && cellTable.getHeader(0) != null && cellTable.getHeader(0).getCell() instanceof CheckboxCell) {
                ((CheckboxCell) cellTable.getHeader(0).getCell()).clearViewData(false);
            }
        }
    }

    @Override
    public void checkCount(String text, CheckValuesCountHandler checkValuesCountHandler) {
        getUiHandlers().getValuesCount(text, checkValuesCountHandler);
    }

    @Override
    public void cleanValues() {
        dataProvider.updateRowData(0, new ArrayList<RefBookItem>(0));
        dataProvider.updateRowCount(0, true);
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
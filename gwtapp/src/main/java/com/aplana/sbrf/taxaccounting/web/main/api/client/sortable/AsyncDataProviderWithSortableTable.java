package com.aplana.sbrf.taxaccounting.web.main.api.client.sortable;

import com.aplana.sbrf.taxaccounting.web.main.api.client.AplanaUiHandlers;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

/**
 * Дата провайдер для сортируемых таблиц
 *
 * @author Fail Mukhametdinov
 * @author Aydar Uldanov
 */
public abstract class AsyncDataProviderWithSortableTable<T, B extends AplanaUiHandlers, C extends ViewWithSortableTable> extends AsyncDataProvider<T> {

    private boolean isAscSorting = true;
    private GenericDataGrid<T> table;
    private C view;


    public AsyncDataProviderWithSortableTable(GenericDataGrid<T> table, C view) {
        this.table = table;
        this.view = view;
        initTable();
    }

    private void initTable() {
        int columnCount = table.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            table.getColumn(i).setSortable(true);
        }

        table.addColumnSortHandler(new ColumnSortEvent.AsyncHandler(table));
        table.getColumnSortList().setLimit(1);
        addDataDisplay(table);
    }

    @Override
    protected void onRangeChanged(HasData<T> display) {
        if (getUiHandlersX() != null) {
            // Сортировка
            final ColumnSortList sortList = table.getColumnSortList();

            if (sortList.size() > 0) {
                isAscSorting = sortList.get(0).isAscending();
                view.setSortByColumn(sortList.get(0).getColumn().getDataStoreName());
            }
            final Range range = display.getVisibleRange();
            getUiHandlersX().onRangeChange(range.getStart(), range.getLength());
        }
    }

    public abstract B getUiHandlersX();

    public boolean isAscSorting() {
        return isAscSorting;
    }

    public void setAscSorting(boolean isAscSorting) {
        this.isAscSorting = isAscSorting;
    }
}

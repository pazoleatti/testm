package com.aplana.sbrf.taxaccounting.web.module.lock.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.LockDataItem;
import com.aplana.sbrf.taxaccounting.model.LockSearchOrdering;
import com.aplana.sbrf.taxaccounting.web.main.api.client.sortable.AsyncDataProviderWithSortableTable;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * View для формы "Блокировки"
 * @author dloshkarev
 */
public class LockListView extends ViewWithUiHandlers<LockListUiHandlers>
        implements LockListPresenter.MyView {

    interface Binder extends UiBinder<Widget, LockListView> {
    }

    public static final String KEY_TITLE = "Ключ блокировки";
    public static final String USER_TITLE = "Пользователь";
    public static final String DATE_LOCK_BEFORE = "Дата истечения блокировки";
    public static final String DATE_LOCK = "Дата установки блокировки";

    @UiField
    Button extendButton;

    @UiField
    Button deleteButton;

    @UiField
    Button stopButton;

    @UiField
    TextBox filterText;

    @UiField
    GenericDataGrid<LockDataItem> lockDataTable;

    @UiField
    FlexiblePager pager;

    final MultiSelectionModel<LockDataItem> selectionModel = new MultiSelectionModel<LockDataItem>(
            new ProvidesKey<LockDataItem>() {
                @Override
                public Object getKey(LockDataItem item) {
                    return item == null ? null : item.getKey();
                }
            }
    );

    private AsyncDataProviderWithSortableTable dataProvider;
    private LockSearchOrdering sortByColumn;

    @Override
    public boolean isAscSorting() {
        return dataProvider.isAscSorting();
    }

    @Override
    public void setSortByColumn(String sortByColumn) {
        this.sortByColumn = LockSearchOrdering.valueOf(sortByColumn);
    }

    @Override
    public LockSearchOrdering getSearchOrdering() {
        if (sortByColumn == null) {
            sortByColumn = LockSearchOrdering.KEY;
        }
        return sortByColumn;
    }

    @Inject
    @UiConstructor
    public LockListView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));

        Column<LockDataItem, Boolean> checkColumn = new Column<LockDataItem, Boolean>(
            new CheckboxCell(true, false)) {
                @Override
                public Boolean getValue(LockDataItem item) {
                    return selectionModel.isSelected(item);
                }
            };

        TextColumn<LockDataItem> keyColumn = new TextColumn<LockDataItem>() {
            @Override
            public String getValue(LockDataItem taskItem) {
                return taskItem.getKey();
            }
        };
        keyColumn.setDataStoreName(LockSearchOrdering.KEY.name());

        TextColumn<LockDataItem> userColumn = new TextColumn<LockDataItem>() {
            @Override
            public String getValue(LockDataItem taskItem) {
                return taskItem.getUserLogin();
            }
        };
        userColumn.setDataStoreName(LockSearchOrdering.LOGIN.name());

        TextColumn<LockDataItem> dateLockColumn = new TextColumn<LockDataItem>() {
            @Override
            public String getValue(LockDataItem taskItem) {
                return taskItem.getDateLock();
            }
        };
        dateLockColumn.setDataStoreName(LockSearchOrdering.DATE_LOCK.name());

        TextColumn<LockDataItem> dateBeforeColumn = new TextColumn<LockDataItem>() {
            @Override
            public String getValue(LockDataItem taskItem) {
                return taskItem.getDateBefore();
            }
        };
        dateBeforeColumn.setDataStoreName(LockSearchOrdering.DATE_BEFORE.name());

        lockDataTable.addColumn(checkColumn);
        lockDataTable.setColumnWidth(checkColumn, 40, Style.Unit.PX);
        lockDataTable.addColumn(keyColumn, KEY_TITLE);
        lockDataTable.addColumn(userColumn, USER_TITLE);
        lockDataTable.addResizableColumn(dateLockColumn, DATE_LOCK);
        lockDataTable.addResizableColumn(dateBeforeColumn, DATE_LOCK_BEFORE);

        lockDataTable.setSelectionModel(selectionModel, DefaultSelectionEventManager
                .<LockDataItem>createCheckboxManager());

        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                updateButtonsStatuses();
            }
        });

        dataProvider = new AsyncDataProviderWithSortableTable<LockDataItem, LockListUiHandlers, LockListView>(lockDataTable, this) {
            @Override
            public LockListUiHandlers getViewUiHandlers() {
                return getUiHandlers();
            }
        };
        lockDataTable.setPageSize(pager.getPageSize());
        pager.setDisplay(lockDataTable);

        updateButtonsStatuses();
    }

    /**
     * Устанавливаем не доступность кнопок которые работают с выделенными
     * задачами из списка задач
     */
    private void updateButtonsStatuses(){
        boolean status = !selectionModel.getSelectedSet().isEmpty();
        deleteButton.setEnabled(status);
        extendButton.setEnabled(status);
        stopButton.setEnabled(status);
    }

    @Override
    public void setTableData(int startIndex, long count, List<LockDataItem> itemList) {
        lockDataTable.setRowCount((int) count);
        lockDataTable.setRowData(startIndex, itemList);
    }

    @Override
    public void clearSelection() {
        selectionModel.clear();
    }

    @Override
    public String getFilter() {
        return filterText.getValue();
    }

    @UiHandler("extendButton")
    public void onExtend(ClickEvent event){
        if(getUiHandlers() != null) {
            Dialog.confirmMessage("Продление блокировок", "Вы действительно хотите продлить блокировку на 1 час?", new DialogHandler() {
                @Override
                public void yes() {
                    if (getUiHandlers() != null) {
                        getUiHandlers().onExtendLock();
                    }
                }
            });
        }
    }

    @UiHandler("deleteButton")
    public void onDelete(ClickEvent event){
        Dialog.confirmMessage("Удаление блокировки", "Вы действительно хотите удалить блокировку?", new DialogHandler() {
            @Override
            public void yes() {
                if(getUiHandlers() != null){
                    getUiHandlers().onDeleteLock();
                }
            }
        });
    }

    @UiHandler("stopButton")
    public void onStop(ClickEvent event){
        Dialog.confirmMessage("Остановка асинхронной задачи", "Вы действительно хотите остановить выполнение асинхронной задачи?", new DialogHandler() {
            @Override
            public void yes() {
                if(getUiHandlers() != null){
                    getUiHandlers().onStopAsync();
                }
            }
        });
    }

    @UiHandler("findButton")
    void onFindClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onFindClicked();
        }
    }

    @Override
    public List<String> getSelectedItem() {
        Set<LockDataItem> selectedSet = selectionModel.getSelectedSet();
        List<String> tasksIds = new ArrayList<String>();
        for (LockDataItem item : selectedSet) {
            tasksIds.add(item.getKey());
        }

        return tasksIds;
    }

    @Override
    public void updateData(int pageNumber) {
        if (pageNumber == 0) {
            lockDataTable.getColumnSortList().clear();
        }
        if (pager.getPage() == pageNumber) {
            lockDataTable.setVisibleRangeAndClearData(lockDataTable.getVisibleRange(), true);
        } else {
            pager.setPage(pageNumber);
        }
    }
}
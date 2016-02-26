package com.aplana.sbrf.taxaccounting.web.module.lock.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.LockDataItem;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

import static com.google.gwt.view.client.DefaultSelectionEventManager.createCustomManager;

/**
 * View для формы "Блокировки"
 * @author dloshkarev
 */
public class LockListView extends ViewWithUiHandlers<LockListUiHandlers> implements LockListPresenter.MyView {

    interface Binder extends UiBinder<Widget, LockListView> {
    }

    public static final String KEY_TITLE = "Ключ блокировки";
    public static final String QUEUE_TITLE = "Тип очереди";
    public static final String DESCRIPTION_TITLE = "Описание";
    public static final String USER_TITLE = "Пользователь";
    public static final String STATE_TITLE = "Состояние задачи";
    public static final String STATE_DATE_TITLE = "Дата изменения состояния";
    public static final String DATE_LOCK = "Дата установки блокировки";
    public static final String SERVER_NODE_TITLE = "Сервер";

    @UiField
    Button deleteButton;
    @UiField
    TextBox filterText;
    @UiField
    GenericDataGrid<LockDataItem> lockDataTable;
    @UiField
    FlexiblePager pager;
    @UiField(provided = true)
    ValueListBox<LockData.LockQueues> queueList;

    final MultiSelectionModel<LockDataItem> selectionModel = new MultiSelectionModel<LockDataItem>(
            new ProvidesKey<LockDataItem>() {
                @Override
                public Object getKey(LockDataItem item) {
                    return item == null ? null : item.getKey();
                }
            }
    );

    private boolean hasRoleAdmin;
    private int currentUserId;

    @Inject
    @UiConstructor
    public LockListView(final Binder uiBinder) {
        queueList = new ValueListBox<LockData.LockQueues>(new AbstractRenderer<LockData.LockQueues>() {
            @Override
            public String render(LockData.LockQueues item) {
                return item.getText();
            }
        }, new ProvidesKey<LockData.LockQueues>() {
            @Override
            public Object getKey(LockData.LockQueues item) {
                return item;
            }
        });
        queueList.setValue(LockData.LockQueues.ALL);
        queueList.setAcceptableValues(Arrays.asList(LockData.LockQueues.values()));
        initWidget(uiBinder.createAndBindUi(this));

        Column<LockDataItem, Boolean> checkColumn = new Column<LockDataItem, Boolean>(
            new CheckboxCell(true, false)) {
                @Override
                public Boolean getValue(LockDataItem item) {
                    return selectionModel.isSelected(item);
                }
            };

        TextColumn<LockDataItem> descriptionColumn = new TextColumn<LockDataItem>() {
            @Override
            public String getValue(LockDataItem taskItem) {
                return taskItem.getDescription();
            }
        };

        TextColumn<LockDataItem> queueColumn = new TextColumn<LockDataItem>() {
            @Override
            public String getValue(LockDataItem taskItem) {
                return taskItem.getQueue();
            }
        };


        TextColumn<LockDataItem> keyColumn = new TextColumn<LockDataItem>() {
            @Override
            public String getValue(LockDataItem taskItem) {
                return taskItem.getKey();
            }
        };
        TextColumn<LockDataItem> serverNodeColumn = new TextColumn<LockDataItem>() {
            @Override
            public String getValue(LockDataItem taskItem) {
                return taskItem.getServerNode();
            }
        };
        serverNodeColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        TextColumn<LockDataItem> userColumn = new TextColumn<LockDataItem>() {
            @Override
            public String getValue(LockDataItem taskItem) {
                return taskItem.getUser();
            }
        };
        userColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        TextColumn<LockDataItem> stateColumn = new TextColumn<LockDataItem>() {
            @Override
            public String getValue(LockDataItem taskItem) {
                return taskItem.getState();
            }
        };

        TextColumn<LockDataItem> stateDateColumn = new TextColumn<LockDataItem>() {
            @Override
            public String getValue(LockDataItem taskItem) {
                return taskItem.getStateDate();
            }
        };
        stateDateColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

        TextColumn<LockDataItem> dateLockColumn = new TextColumn<LockDataItem>() {
            @Override
            public String getValue(LockDataItem taskItem) {
                return taskItem.getDateLock();
            }
        };
        dateLockColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        lockDataTable.setColumnWidth(checkColumn, 20, Style.Unit.PX);
        lockDataTable.setColumnWidth(dateLockColumn, 110, Style.Unit.PX);
        lockDataTable.setColumnWidth(userColumn, 120, Style.Unit.PX);
        lockDataTable.setColumnWidth(stateColumn, 100, Style.Unit.PX);
        lockDataTable.setColumnWidth(stateDateColumn, 120, Style.Unit.PX);
        lockDataTable.setColumnWidth(keyColumn, 200, Style.Unit.PX);
        lockDataTable.setColumnWidth(serverNodeColumn, 100, Style.Unit.PX);
        lockDataTable.setColumnWidth(descriptionColumn, 250, Style.Unit.PX);
        lockDataTable.setColumnWidth(queueColumn, 100, Style.Unit.PX);

        lockDataTable.addColumn(checkColumn);
        lockDataTable.addResizableColumn(dateLockColumn, DATE_LOCK);
        lockDataTable.addColumn(keyColumn, KEY_TITLE);
        lockDataTable.addColumn(serverNodeColumn, SERVER_NODE_TITLE);
        lockDataTable.addColumn(descriptionColumn, DESCRIPTION_TITLE);
        lockDataTable.addColumn(userColumn, USER_TITLE);
        lockDataTable.addColumn(queueColumn, QUEUE_TITLE);
        lockDataTable.addColumn(stateColumn, STATE_TITLE);
        lockDataTable.addColumn(stateDateColumn, STATE_DATE_TITLE);

        lockDataTable.setSelectionModel(selectionModel, DefaultSelectionEventManager
                .<LockDataItem>createCheckboxManager());

        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                updateButtonsStatuses();
            }
        });

        lockDataTable.setPageSize(pager.getPageSize());
        pager.setDisplay(lockDataTable);

        updateButtonsStatuses();
        filterText.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    getUiHandlers().onFindClicked();
                }
            }
        });
    }

    /**
     * Устанавливаем не доступность кнопок которые работают с выделенными
     * задачами из списка задач
     */
    private void updateButtonsStatuses(){
        boolean selected = !selectionModel.getSelectedSet().isEmpty();
        boolean hasNoOwnLocks = false;
        for (LockDataItem item : selectionModel.getSelectedSet()) {
            if (currentUserId != item.getUserId()) {
                hasNoOwnLocks = true;
            }
        }
        boolean status = selected && (hasRoleAdmin || !hasNoOwnLocks);
        deleteButton.setEnabled(status);
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

    @Override
    public int getPageSize() {
        return pager.getPageSize();
    }

    @Override
    public void assignDataProvider(int pageSize, AbstractDataProvider<LockDataItem> data) {
        lockDataTable.setPageSize(pageSize);
        data.addDataDisplay(lockDataTable);
    }

    @Override
    public void setRoleInfo(int currentUserId, boolean hasRoleAdmin) {
        this.hasRoleAdmin = hasRoleAdmin;
        this.currentUserId = currentUserId;
    }

    @Override
    public LockData.LockQueues getQueues() {
        return queueList.getValue();
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
        if (pager.getPage() == pageNumber) {
            lockDataTable.setVisibleRangeAndClearData(lockDataTable.getVisibleRange(), true);
        } else {
            pager.setPage(pageNumber);
        }
    }
}
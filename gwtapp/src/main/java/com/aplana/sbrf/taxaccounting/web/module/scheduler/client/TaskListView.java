package com.aplana.sbrf.taxaccounting.web.module.scheduler.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.TaskSearchResultItem;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * View для формы "Планировщик задач"       *
 * @author dloshkarev
 */
public class TaskListView extends ViewWithUiHandlers<TaskListUiHandlers>
        implements TaskListPresenter.MyView {

    interface Binder extends UiBinder<Widget, TaskListView> {
    }

    public static final String NUMBER = "№";
    public static final String NAME_TITLE = "Название";
    public static final String STATE_TITLE = "Статус";
    public static final String TIME_CHANGED_TITLE = "Дата редактирования";
    public static final String NEXT_FIRE_TIME_TITLE = "Дата следующего запуска";

    @UiField
    Button createButton;

    @UiField
    Button stopButton;

    @UiField
    Button resumeButton;

    @UiField
    Button startButton;

    @UiField
    Button deleteButton;

    @UiField
    GenericDataGrid<TaskSearchResultItem> taskDataTable;

    final MultiSelectionModel<TaskSearchResultItem> selectionModel = new MultiSelectionModel<TaskSearchResultItem>(
            new ProvidesKey<TaskSearchResultItem>() {
                @Override
                public Object getKey(TaskSearchResultItem item) {
                    return item == null ? null : item.getId();
                }
            }
    );

    @Inject
    @UiConstructor
    public TaskListView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));

        TextColumn<TaskSearchResultItem> numberColumn = new TextColumn<TaskSearchResultItem>() {
            @Override
            public String getValue(TaskSearchResultItem taskItem) {
                return String.valueOf(taskItem.getContextId());
            }
        };

        Column<TaskSearchResultItem, Boolean> checkColumn = new Column<TaskSearchResultItem, Boolean>(
            new CheckboxCell(true, false)) {
                @Override
                public Boolean getValue(TaskSearchResultItem item) {
                    return selectionModel.isSelected(item);
                }
            };

        Column<TaskSearchResultItem, TaskSearchResultItem> nameColumn = new Column<TaskSearchResultItem, TaskSearchResultItem>(
                new AbstractCell<TaskSearchResultItem>() {
                    @Override
                    public void render(Context context, TaskSearchResultItem value, SafeHtmlBuilder sb) {
                        if (value == null) {
                            return;
                        }
                        sb.appendHtmlConstant("<a href=\"#"
                                + SchedulerTokens.task + ";"
                                + SchedulerTokens.taskId + "="
                                + value.getId() + "\">"
                                + value.getName() + "</a>");
                    }
                }
        ) {
            @Override
            public TaskSearchResultItem getValue(TaskSearchResultItem taskItem) {
                return taskItem;
            }
        };

        TextColumn<TaskSearchResultItem> stateColumn = new TextColumn<TaskSearchResultItem>() {
            @Override
            public String getValue(TaskSearchResultItem taskItem) {
                return taskItem.getState();
            }
        };

        TextColumn<TaskSearchResultItem> modificationDateColumn = new TextColumn<TaskSearchResultItem>() {
            @Override
            public String getValue(TaskSearchResultItem taskItem) {
                return taskItem.getModificationDate();
            }
        };

        TextColumn<TaskSearchResultItem> nextFireTimeColumn = new TextColumn<TaskSearchResultItem>() {
            @Override
            public String getValue(TaskSearchResultItem taskItem) {
                return taskItem.getNextFireTime();
            }
        };

        taskDataTable.addColumn(checkColumn);
        taskDataTable.setColumnWidth(checkColumn, 40, Style.Unit.PX);
        taskDataTable.addColumn(numberColumn, NUMBER);
        taskDataTable.setColumnWidth(numberColumn, 30, Style.Unit.PX);
        taskDataTable.addResizableColumn(nameColumn, NAME_TITLE);
        taskDataTable.addResizableColumn(stateColumn, STATE_TITLE);
        taskDataTable.addResizableColumn(modificationDateColumn, TIME_CHANGED_TITLE);
        taskDataTable.addResizableColumn(nextFireTimeColumn, NEXT_FIRE_TIME_TITLE);

        taskDataTable.setSelectionModel(selectionModel, DefaultSelectionEventManager
                .<TaskSearchResultItem>createCheckboxManager());

        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                updateButtonsStatuses();
            }
        });

        updateButtonsStatuses();
    }

    /**
     * Устанавливаем не доступность кнопок которые работают с выделенными
     * задачами из списка задач
     */
    private void updateButtonsStatuses(){
        boolean status = !selectionModel.getSelectedSet().isEmpty();
        deleteButton.setEnabled(status);
        startButton.setEnabled(status);
        stopButton.setEnabled(status);
        resumeButton.setEnabled(status);
    }

    @Override
    public void setTableData(List<TaskSearchResultItem> tasks) {
        taskDataTable.setRowData(tasks);
        selectionModel.clear();
        updateButtonsStatuses();
    }

    @UiHandler("createButton")
    public void onCreate(ClickEvent event){
        if(getUiHandlers() != null){
            getUiHandlers().onShowCreateTaskForm();
        }
    }

    @UiHandler("stopButton")
    public void onStop(ClickEvent event){
        if(getUiHandlers() != null){
            getUiHandlers().onStopTask();
        }
    }

    @UiHandler("resumeButton")
    public void onResume(ClickEvent event){
        if(getUiHandlers() != null){
            getUiHandlers().onResumeTask();
        }
    }

    @UiHandler("startButton")
    public void onStart(ClickEvent event){
        if(getUiHandlers() != null){
            getUiHandlers().onStartTask();
        }
    }

    @UiHandler("deleteButton")
    public void onDelete(ClickEvent event){
        Dialog.confirmMessage("Удаление задачи", "Удалить задачу?", new DialogHandler() {
            @Override
            public void yes() {
                if(getUiHandlers() != null){
                    getUiHandlers().onDeleteTask();
                }
            }
        });
    }

    @Override
    public List<Long> getSelectedItem() {
        Set<TaskSearchResultItem> selectedSet = selectionModel.getSelectedSet();
        List<Long> tasksIds = new ArrayList<Long>();
        for (TaskSearchResultItem item : selectedSet) {
            tasksIds.add(item.getId());
        }

        return tasksIds;
    }
}
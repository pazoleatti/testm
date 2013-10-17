package com.aplana.sbrf.taxaccounting.web.module.scheduler.client;

import com.aplana.sbrf.taxaccounting.model.TaskSearchResultItem;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;

/**
 * View для формы "Планировщик задач"       *
 * @author dloshkarev
 */
public class TaskListView extends ViewWithUiHandlers<TaskListUiHandlers>
        implements TaskListPresenter.MyView {

    interface Binder extends UiBinder<Widget, TaskListView> {
    }

    public static final String NAME_TITLE = "Название";
    public static final String STATE_TITLE = "Статус";
    public static final String REPEATS_LEFT_TITLE = "Повторений выполнено";
    public static final String NUMBER_OF_REPEATS_TITLE = "Повторений всего";
    public static final String TIME_CREATED_TITLE = "Дата создания";
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
    DataGrid<TaskSearchResultItem> taskDataTable;

    @Inject
    @UiConstructor
    public TaskListView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));

        final SelectionModel<TaskSearchResultItem> selectionModel = new SingleSelectionModel<TaskSearchResultItem>(
                new ProvidesKey<TaskSearchResultItem>() {
                    @Override
                    public Object getKey(TaskSearchResultItem item) {
                        return item == null ? null : item.getId();
                    }
                }
        );

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

        TextColumn<TaskSearchResultItem> repeatsLeftColumn = new TextColumn<TaskSearchResultItem>() {
            @Override
            public String getValue(TaskSearchResultItem taskItem) {
                return taskItem.getRepeatsLeft().toString();
            }
        };

        TextColumn<TaskSearchResultItem> numberOfRepeatsColumn = new TextColumn<TaskSearchResultItem>() {
            @Override
            public String getValue(TaskSearchResultItem taskItem) {
                return String.valueOf(taskItem.getNumberOfRepeats());
            }
        };

        TextColumn<TaskSearchResultItem> timeCreatedColumn = new TextColumn<TaskSearchResultItem>() {
            @Override
            public String getValue(TaskSearchResultItem taskItem) {
                return taskItem.getTimeCreated();
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
        taskDataTable.addColumn(nameColumn, NAME_TITLE);
        taskDataTable.addColumn(stateColumn, STATE_TITLE);
        taskDataTable.addColumn(repeatsLeftColumn, REPEATS_LEFT_TITLE);
        taskDataTable.addColumn(numberOfRepeatsColumn, NUMBER_OF_REPEATS_TITLE);
        taskDataTable.addColumn(timeCreatedColumn, TIME_CREATED_TITLE);
        taskDataTable.addColumn(nextFireTimeColumn, NEXT_FIRE_TIME_TITLE);

        taskDataTable.setSelectionModel(selectionModel, DefaultSelectionEventManager
                .<TaskSearchResultItem>createCheckboxManager());
    }

    @Override
    public void setTableData(List<TaskSearchResultItem> tasks) {
        taskDataTable.setRowData(tasks);
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
        if(getUiHandlers() != null){
            getUiHandlers().onDeleteTask();
        }
    }

    @Override
    public TaskSearchResultItem getSelectedItem() {
        return ((SingleSelectionModel<TaskSearchResultItem>) taskDataTable.getSelectionModel()).getSelectedObject();
    }
}
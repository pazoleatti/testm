package com.aplana.sbrf.taxaccounting.web.module.scheduler.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.TaskSearchResultItem;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.List;

/**
 * Presenter для формы "Планировщик задач"       *
 * @author dloshkarev
 */
public class TaskListPresenter extends Presenter<TaskListPresenter.MyView,
        TaskListPresenter.MyProxy> implements TaskListUiHandlers {

    private final DispatchAsync dispatcher;
    private PlaceManager placeManager;

    @ProxyCodeSplit
    @NameToken(SchedulerTokens.taskList)
    public interface MyProxy extends ProxyPlace<TaskListPresenter>, Place {
    }

    public interface MyView extends View, HasUiHandlers<TaskListUiHandlers> {
        void setTableData(List<TaskSearchResultItem> records);
        List<Long> getSelectedItem();
    }

    @Inject
    public TaskListPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                             DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        this.placeManager = placeManager;
        getView().setUiHandlers(this);
    }

    @Override
    public void onShowCreateTaskForm() {
        placeManager.revealPlace(new PlaceRequest.Builder().nameToken(SchedulerTokens.task).build());
    }

    @Override
    public void onStopTask() {
        if (isSelectedTaskExist()) {
            StopTaskAction action = new StopTaskAction();
            action.setTasksIds(getView().getSelectedItem());
            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<StopTaskResult>() {
                        @Override
                        public void onSuccess(StopTaskResult result) {
                            // проверка ошибок
                            LogAddEvent.fire(TaskListPresenter.this, result.getUuid());
                            if (result.getUuid() != null){
                                Dialog.errorMessage("Остановка задачи", "Остановка не выполнена");
                            }
                            updateTableData();
                        }
                    }, TaskListPresenter.this));
        }
    }

    @Override
    public void onResumeTask() {
        if (isSelectedTaskExist()) {
            ResumeTaskAction action = new ResumeTaskAction();
            action.setTasksIds(getView().getSelectedItem());
            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<ResumeTaskResult>() {
                        @Override
                        public void onSuccess(ResumeTaskResult result) {
                            updateTableData();
                        }
                    }, TaskListPresenter.this));
        }
    }

    @Override
    public void onStartTask() {
        if (isSelectedTaskExist()) {
            StartTaskAction action = new StartTaskAction();
            action.setTasksIds(getView().getSelectedItem());
            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<StartTaskResult>() {
                        @Override
                        public void onSuccess(StartTaskResult result) {
                            updateTableData();
                        }
                    }, TaskListPresenter.this));

        }
    }

    @Override
    public void onDeleteTask() {
        if (isSelectedTaskExist()) {
            DeleteTaskAction action = new DeleteTaskAction();
            action.setTasksIds(getView().getSelectedItem());
            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<DeleteTaskResult>() {
                        @Override
                        public void onSuccess(DeleteTaskResult result) {
                            // проверка ошибок
                            LogAddEvent.fire(TaskListPresenter.this, result.getUuid());
                            if (result.getUuid() != null){
                                Dialog.errorMessage("Удаление задачи", "Удаление не выполнено");
                            }
                            updateTableData();
                        }
                    }, TaskListPresenter.this));
        }
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
        updateTableData();
    }

    private void updateTableData() {
        GetTaskListAction action = new GetTaskListAction();
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetTaskListResult>() {
                    @Override
                    public void onSuccess(GetTaskListResult result) {
                        if (result.getUuid() != null) {
                            Dialog.errorMessage("Произошла ошибка, задачи планировщика удалены");
                            LogCleanEvent.fire(TaskListPresenter.this);
                            LogAddEvent.fire(TaskListPresenter.this, result.getUuid());
                        }
                        getView().setTableData(result.getTasks());
                    }
                }, TaskListPresenter.this));
    }

    /**
     * Проверяет есть ли выбранные задачи
     * @return
     */
    private boolean isSelectedTaskExist() {
        if (getView().getSelectedItem() == null) {
            MessageEvent.fire(this, "Для выполнения этого действия должна быть выбрана одна задача из списка");
            return false;
        }
        return true;
    }

}
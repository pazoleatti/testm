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
    public void onStopTask() {
        if (isSelectedTaskExist()) {
            SetActiveTaskAction action = new SetActiveTaskAction();
            action.setTasksIds(getView().getSelectedItem());
            action.setActive(false);
            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<SetActiveTaskResult>() {
                        @Override
                        public void onSuccess(SetActiveTaskResult result) {
                            updateTableData();
                        }
                    }, TaskListPresenter.this));
        }
    }

    @Override
    public void onResumeTask() {
        if (isSelectedTaskExist()) {
            SetActiveTaskAction action = new SetActiveTaskAction();
            action.setTasksIds(getView().getSelectedItem());
            action.setActive(true);
            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<SetActiveTaskResult>() {
                        @Override
                        public void onSuccess(SetActiveTaskResult result) {
                            updateTableData();
                        }
                    }, TaskListPresenter.this));
        }
    }

    @Override
    public void onUpdateTask() {
        updateTableData();
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
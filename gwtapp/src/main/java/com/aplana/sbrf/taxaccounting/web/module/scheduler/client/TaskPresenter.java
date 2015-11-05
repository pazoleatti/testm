package com.aplana.sbrf.taxaccounting.web.module.scheduler.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.TaskParamModel;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskJndiInfo;
import com.aplana.sbrf.taxaccounting.web.main.api.client.ParamUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.dispatch.shared.Result;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.client.TaskView.LinkStyle;

import java.util.*;

/**
 * Презентер для формы "Задача планировщика"
 *
 * @author dloshkarev
 */
public class TaskPresenter extends Presenter<TaskPresenter.MyView,
        TaskPresenter.MyProxy> implements TaskUiHandlers {

    private final DispatchAsync dispatcher;
    private PlaceManager placeManager;
    private boolean editMode;
    private Long currentTaskId;
    private Long contextId;

    @ProxyCodeSplit
    @NameToken(SchedulerTokens.task)
    public interface MyProxy extends ProxyPlace<TaskPresenter>, Place {
    }

    public interface MyView extends View, HasUiHandlers<TaskUiHandlers> {
        LinkStyle getCss();

        String getTaskName();

        String getTaskSchedule();

        String getJndi();

        void setJndiList(List<TaskJndiInfo> jndiList);

        void clearForm();

        void setTaskData(GetTaskInfoResult taskData);

        void setMode(boolean editMode);

        boolean validateTaskParams();

        String getErrorsOnValidateTaskParams();

        boolean isTaskTypeSelected();

        List<TaskParamModel> getTaskParams();
    }

    @Inject
    public TaskPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                         DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        this.placeManager = placeManager;
        getView().setUiHandlers(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
        editMode = !request.getParameterNames().isEmpty();
        getView().setMode(editMode);

        GetAvailableTasksAction initAction = new GetAvailableTasksAction();
        dispatcher.execute(initAction, CallbackUtils
                .defaultCallback(new AbstractCallback<GetAvailableTasksResult>() {
                    @Override
                    public void onSuccess(GetAvailableTasksResult result) {
                        //Инициализация формы
                        getView().setJndiList(result.getJndiList());
                        getView().clearForm();

                        //Загрузка информации по задаче, если она была выбрана
                        if (request.getParameter(SchedulerTokens.taskId, null) != null) {
                            currentTaskId = ParamUtils.getLong(request, SchedulerTokens.taskId);

                            //Получаем данные задачи
                            GetTaskInfoAction action = new GetTaskInfoAction();
                            action.setTaskId(currentTaskId);
                            dispatcher.execute(action, CallbackUtils
                                    .defaultCallback(new AbstractCallback<GetTaskInfoResult>() {
                                        @Override
                                        public void onSuccess(GetTaskInfoResult result) {
                                            getView().setTaskData(result);
                                            contextId = result.getContextId();
                                        }
                                    }, TaskPresenter.this));
                        }
                    }
                }, TaskPresenter.this));
    }

    @Override
    public void onCreateTask() {
        if (validateForm()) {
            if (editMode) {
                UpdateTaskAction action = new UpdateTaskAction();
                action.setTaskId(currentTaskId);
                action.setContextId(contextId);
                fillTaskData(action);

                dispatcher.execute(action, CallbackUtils
                        .defaultCallback(new AbstractCallback<UpdateTaskResult>() {
                            @Override
                            public void onSuccess(UpdateTaskResult result) {
                                if (result.isHasErrors()){
                                    Dialog.errorMessage(result.getErrorMessage());
                                } else {
                                    placeManager.revealPlace(new PlaceRequest.Builder().nameToken(SchedulerTokens.taskList).build());
                                }
                            }
                        }, TaskPresenter.this));
            } else {
                CreateTaskAction action = new CreateTaskAction();
                fillTaskData(action);

                dispatcher.execute(action, CallbackUtils
                        .defaultCallback(new AbstractCallback<CreateTaskResult>() {
                            @Override
                            public void onSuccess(CreateTaskResult result) {
                                if (result.isHasErrors()){
                                    Dialog.errorMessage(result.getErrorMessage());
                                } else {
                                    placeManager.revealPlace(new PlaceRequest.Builder().nameToken(SchedulerTokens.taskList).build());
                                }
                            }
                        }, TaskPresenter.this));
            }
        }
    }

    /**
     * Заполение данных задачи
     *
     * @param action
     * @param <T>
     */
    private <T extends Result> void fillTaskData(TaskData<T> action) {
        action.setTaskName(getView().getTaskName());
        action.setSchedule(getView().getTaskSchedule());
        action.setUserTaskJndi(getView().getJndi());

        List<TaskParamModel> params = getView().getTaskParams();
        action.setParams(params);
    }

    @Override
    public void onCancel() {
        placeManager.revealPlace(new PlaceRequest.Builder().nameToken(SchedulerTokens.taskList).build());
    }

    /**
     * Валидация введенных параметров
     */
    private boolean validateForm() {
        StringBuilder validateMsg = new StringBuilder();
        if (getView().getTaskName().isEmpty()) {
            validateMsg.append("«Название», ");
        }
        if (getView().getTaskSchedule().isEmpty()) {
            validateMsg.append("«Расписание», ");
        }
        if (getView().getJndi().isEmpty()) {
            validateMsg.append("«Тип задачи», ");
        }
        if (validateMsg.length() > 0) {
            validateMsg.delete(validateMsg.length() - 2, validateMsg.length());
            validateMsg.append(". ");
        }

        /**
         * если часть формы с полями воода значений для задачи
         * видима, и на ней есть ошибки то добавим их в основной список ошибок
         */
        if (getView().isTaskTypeSelected() && !getView().validateTaskParams()){
            validateMsg.append(" Дополнительные параметры задачи: "+getView().getErrorsOnValidateTaskParams());
        }

        if (!validateMsg.toString().isEmpty()) {
            Dialog.errorMessage("Указание параметров", "Не заполнены поля обязательные для заполнения: "+validateMsg);
            return false;
        }

        return true;
    }
}

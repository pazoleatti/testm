package com.aplana.sbrf.taxaccounting.web.module.scheduler.client;

import com.aplana.sbrf.taxaccounting.model.TaskParamModel;
import com.aplana.sbrf.taxaccounting.model.TaskParamTypeValues;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskJndiInfo;
import com.aplana.sbrf.taxaccounting.web.main.api.client.ParamUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.*;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.user.client.ui.*;
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

    @ProxyCodeSplit
    @NameToken(SchedulerTokens.task)
    public interface MyProxy extends ProxyPlace<TaskPresenter>, Place {
    }

    public interface MyView extends View, HasUiHandlers<TaskUiHandlers> {
        LinkStyle getCss();

        String getTaskName();

        String getTaskSchedule();

        String getNumberOfRepeats();

        String getJndi();

        void setJndiList(List<TaskJndiInfo> jndiList);

        void clearForm();

        void setTaskData(GetTaskInfoResult taskData);

        void setTitle(String title);

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
        if (request.getParameterNames().size() != 0) {
            getView().setTitle("Изменить задачу");
        } else {
            getView().setTitle("Создать задачу");
        }
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
                            Long currentTaskId = ParamUtils.getLong(request,
                                    SchedulerTokens.taskId);

                            //Получаем данные задачи
                            GetTaskInfoAction action = new GetTaskInfoAction();
                            action.setTaskId(currentTaskId);
                            dispatcher.execute(action, CallbackUtils
                                    .defaultCallback(new AbstractCallback<GetTaskInfoResult>() {
                                        @Override
                                        public void onSuccess(GetTaskInfoResult result) {
                                            getView().setTaskData(result);
                                        }
                                    }, TaskPresenter.this));
                        }
                    }
                }, TaskPresenter.this));
    }

    @Override
    public void onCreateTask() {
        if (validateForm()) {
            CreateTaskAction action = new CreateTaskAction();
            action.setTaskName(getView().getTaskName());
            action.setSchedule(getView().getTaskSchedule());
            action.setNumberOfRepeats(Integer.parseInt(getView().getNumberOfRepeats()));
            action.setUserTaskJndi(getView().getJndi());

            List<TaskParamModel> params = getView().getTaskParams();
            action.setParams(params);

            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<CreateTaskResult>() {
                        @Override
                        public void onSuccess(CreateTaskResult result) {
                            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(SchedulerTokens.taskList).build());
                        }
                    }, TaskPresenter.this));
        }
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
            validateMsg.append("Не заполнено поле 'Название'").append("; ");
        }
        if (getView().getTaskSchedule().isEmpty()) {
            validateMsg.append("Не заполнено поле 'Расписание'").append("; ");
        }
        if (getView().getNumberOfRepeats().isEmpty()) {
            validateMsg.append("Не заполнено поле 'Количество повторений задачи'").append("; ");
        } else if (!checkStringAsInt(getView().getNumberOfRepeats(), false)) {
            validateMsg.append("Поле 'Количество повторений задачи' должно быть числом").append("; ");
        }
        if (getView().getJndi().isEmpty()) {
            validateMsg.append("Не заполнено поле 'JNDI класса-обработчика'").append("; ");
        }

        /**
         * если часть формы с полями воода значений для задачи
         * видима, и на ней есть ошибки то добавим их в основной список ошибок
         */
        if (getView().isTaskTypeSelected() && !getView().validateTaskParams()){
            validateMsg.append(getView().getErrorsOnValidateTaskParams());
        }

        if (!validateMsg.toString().isEmpty()) {
            MessageEvent.fire(this, "Ошибки в заполнении параметров задачи: " + validateMsg);
            return false;
        }

        return true;
    }

    /**
     * Проверка строки на число
     * @param string
     * @return true - если это число
     */
    private boolean checkStringAsInt(String string, Boolean isFloatExpected) {
        if (string == null || string.length() == 0) return false;

        int i = 0;
        if (string.charAt(0) == '-') {
            if (string.length() == 1) {
                return false;
            }
            i = 1;
        }

        char c;
        for (; i < string.length(); i++) {
            c = string.charAt(i);
            if (!(c >= '0' && c <= '9') &&
                    !(isFloatExpected && (c == '.' || c == ','))) {
                return false;
            }
        }
        return true;
    }

}

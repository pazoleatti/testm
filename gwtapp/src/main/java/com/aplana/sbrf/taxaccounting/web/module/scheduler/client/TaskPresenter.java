package com.aplana.sbrf.taxaccounting.web.module.scheduler.client;

import com.aplana.sbrf.taxaccounting.model.TaskParamModel;
import com.aplana.sbrf.taxaccounting.model.TaskParamTypeValues;
import com.aplana.sbrf.taxaccounting.web.main.api.client.ParamUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.CreateTaskAction;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.CreateTaskResult;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.GetTaskInfoAction;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.GetTaskInfoResult;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.CustomDateBox;
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

    /**
     * Позиция имени параметра в блоках виджетов
     */
    private static final int PARAM_NAME_POSITION = 0;
    /**
     * Позиция типа параметра в блоках виджетов
     */
    private static final int PARAM_TYPE_POSITION = 1;
    /**
     * Позиция значения параметра в блоках виджетов
     */
    private static final int PARAM_VALUE_POSITION = 2;

    /**
     * Счетчик параметров задачи
     */
    private int paramsCounter = 0;

    @ProxyCodeSplit
    @NameToken(SchedulerTokens.task)
    public interface MyProxy extends ProxyPlace<TaskPresenter>, Place {
    }

    public interface MyView extends View, HasUiHandlers<TaskUiHandlers> {
        VerticalPanel getParamsPanel();

        LinkStyle getCss();

        String getTaskName();

        String getTaskSchedule();

        String getNumberOfRepeats();

        String getJndi();

        void clearForm();

        void setTaskData(GetTaskInfoResult taskData);
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
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        getView().clearForm();
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

    @Override
    public void onCreateTask() {
        if (validateForm()) {
            CreateTaskAction action = new CreateTaskAction();
            action.setTaskName(getView().getTaskName());
            action.setSchedule(getView().getTaskSchedule());
            action.setNumberOfRepeats(Integer.parseInt(getView().getNumberOfRepeats()));
            action.setUserTaskJndi(getView().getJndi());

            List<TaskParamModel> params = new ArrayList<TaskParamModel>();

            for (int i = 0; i < getView().getParamsPanel().getWidgetCount(); i++) {
                TaskParamModel paramModel = new TaskParamModel();
                VerticalPanel param = (VerticalPanel) getView().getParamsPanel().getWidget(i);

                //Имя параметра
                paramModel.setTaskParamName(((TextBox) (
                        (HorizontalPanel) param.getWidget(PARAM_NAME_POSITION)).getWidget(1)
                ).getValue());

                //Тип параметра
                paramModel.setTaskParamType(((ValueListBox<TaskParamTypeValues>)
                        ((HorizontalPanel) param.getWidget(PARAM_TYPE_POSITION)).getWidget(1)
                ).getValue().getId());

                //Значение параметра
                Widget paramValueWidget = ((Widget) ((HorizontalPanel) param.getWidget(PARAM_VALUE_POSITION)).getWidget(1));
                if (paramValueWidget instanceof TextBox) {
                    paramModel.setTaskParamValue(((TextBox) paramValueWidget).getValue());
                } else {
                    paramModel.setTaskParamDateValue(((CustomDateBox) paramValueWidget).getValue());
                }
                params.add(paramModel);
            }
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

    @Override
    public void onAddParam() {
        addParam(null);
    }

    @Override
    public void onAddParam(TaskParamModel param) {
        addParam(param);
    }

    private void addParam(TaskParamModel param) {
        paramsCounter++;
        VerticalPanel paramPanel = new VerticalPanel();
        paramPanel.getElement().setId("paramPanel" + paramsCounter);

        //Название параметра
        HorizontalPanel namePanel = new HorizontalPanel();
        namePanel.setSpacing(7);
        Label nameLabel = new Label("Название");
        nameLabel.setWidth("200px");
        TextBox nameTextBox = new TextBox();
        nameTextBox.getElement().setId("paramName_" + paramsCounter);
        nameTextBox.setWidth("370px");
        nameTextBox.setValue((param == null) ? "" : param.getTaskParamName());
        nameTextBox.setReadOnly(param != null);

        namePanel.add(nameLabel);
        namePanel.add(nameTextBox);

        //Тип параметра
        HorizontalPanel typePanel = new HorizontalPanel();
        typePanel.setSpacing(7);
        Label typeLabel = new Label("Тип");
        typeLabel.setWidth("200px");
        ValueListBox<TaskParamTypeValues> typeListBox = new ValueListBox<TaskParamTypeValues>(
                new AbstractRenderer<TaskParamTypeValues>() {
                    @Override
                    public String render(TaskParamTypeValues object) {
                        if (object == null) {
                            return "";
                        }
                        return object.getName();
                    }
                });
        if (param == null) {
            typeListBox.setValue(TaskParamTypeValues.INT);
            typeListBox.setAcceptableValues(Arrays.asList(TaskParamTypeValues.values()));
        } else {
            typeListBox.setValue(TaskParamTypeValues.fromId(param.getTaskParamType()));
            typeListBox.setAcceptableValues(Arrays.asList(TaskParamTypeValues.fromId(param.getTaskParamType())));
        }
        typeListBox.getElement().setId("paramType_" + paramsCounter);
        typeListBox.setWidth("370px");
        typeListBox.addValueChangeHandler(new ValueChangeHandler<TaskParamTypeValues>() {
            @Override
            public void onValueChange(ValueChangeEvent<TaskParamTypeValues> event) {
                //Если будет выбран тип 'Дата', то надо поменять контрол для ввода значения параметра
                String paramNumber = getParamNumber(((ValueListBox<TaskParamTypeValues>) event.getSource())
                        .getElement().getId());

                //Находим виджет для ввода значения параметра
                Widget targetParamValue = null;
                HorizontalPanel targetParamValuePanel = null;
                for (int i = 0; i < getView().getParamsPanel().getWidgetCount(); i++) {
                    VerticalPanel param = (VerticalPanel) getView().getParamsPanel().getWidget(i);
                    if (param.getElement().getId().endsWith(paramNumber)) {
                        targetParamValuePanel = (HorizontalPanel) param.getWidget(PARAM_VALUE_POSITION);
                        targetParamValue = targetParamValuePanel.getWidget(1);
                        break;
                    }
                }

                //Заменяем контрол, если это необходимо
                if (event.getValue() == TaskParamTypeValues.DATE) {
                    if (targetParamValue instanceof TextBox) {
                        targetParamValue.removeFromParent();
                        CustomDateBox valueBox = new CustomDateBox();
                        valueBox.getElement().setId("paramValue" + paramNumber);
                        valueBox.setWidth("130px");
                        targetParamValuePanel.add(valueBox);
                    }
                } else {
                    if (targetParamValue instanceof CustomDateBox) {
                        targetParamValue.removeFromParent();
                        TextBox valueBox = new TextBox();
                        valueBox.getElement().setId("paramValue_" + paramNumber);
                        valueBox.setWidth("370px");
                        targetParamValuePanel.add(valueBox);
                    }
                }
            }
        });

        typePanel.add(typeLabel);
        typePanel.add(typeListBox);

        //Кнопка удаления параметра
        if (param == null) {
            Button paramDeleteButton = new Button();
            paramDeleteButton.setText("Удалить");
            paramDeleteButton.getElement().setId("deleteParamBtn_" + paramsCounter);
            paramDeleteButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    String paramNumber = getParamNumber(((Button) event.getSource())
                            .getElement().getId());

                    for (int i = 0; i < getView().getParamsPanel().getWidgetCount(); i++) {
                        VerticalPanel param = (VerticalPanel) getView().getParamsPanel().getWidget(PARAM_TYPE_POSITION);
                        if (param.getElement().getId().endsWith(paramNumber)) {
                            param.removeFromParent();
                            break;
                        }
                    }
                }
            });

            HorizontalPanel deleteButtonPanel = new HorizontalPanel();
            deleteButtonPanel.setWidth("100px");
            deleteButtonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
            deleteButtonPanel.add(paramDeleteButton);
            typePanel.add(deleteButtonPanel);
        }

        //Значение параметра
        HorizontalPanel valuePanel = new HorizontalPanel();
        valuePanel.setSpacing(7);
        Label valueLabel = new Label("Значение");
        valueLabel.setWidth("200px");
        TextBox valueBox = new TextBox();
        valueBox.getElement().setId("paramValue_" + paramsCounter);
        valueBox.setWidth("370px");
        valueBox.setValue((param == null) ? "" : param.getTaskParamValue());
        valueBox.setReadOnly(param != null);

        valuePanel.add(valueLabel);
        valuePanel.add(valueBox);

        //Разделитель
        HorizontalPanel separatorPanel = new HorizontalPanel();
        separatorPanel.setWidth("100%");
        separatorPanel.setStyleName(getView().getCss().separator());

        //Важно соблюдать порядок добавления виджетов
        paramPanel.add(namePanel);
        paramPanel.add(typePanel);
        paramPanel.add(valuePanel);
        paramPanel.add(separatorPanel);

        getView().getParamsPanel().add(paramPanel);
    }

    private String getParamNumber(String paramId) {
        return paramId.substring(paramId.lastIndexOf("_") + 1);
    }

    /**
     * Валидация введенных параметров
     */
    private boolean validateForm() {
        StringBuilder validateMsg = new StringBuilder();
        System.out.println("getView().getTaskName().isEmpty(): "+getView().getTaskName().isEmpty());
        System.out.println("getView().getTaskSchedule().isEmpty(): "+getView().getTaskSchedule().isEmpty());
        System.out.println("getView().getNumberOfRepeats().isEmpty(): "+getView().getNumberOfRepeats().isEmpty());
        System.out.println("getView().getJndi().isEmpty(): "+getView().getJndi().isEmpty());
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

        Set<String> paramNames = new HashSet<String>();
        for (int i = 0; i < getView().getParamsPanel().getWidgetCount(); i++) {
            VerticalPanel param = (VerticalPanel) getView().getParamsPanel().getWidget(i);
            //Имя параметра
            String paramName = ((TextBox) (
                    (HorizontalPanel) param.getWidget(PARAM_NAME_POSITION)).getWidget(1)
            ).getValue();
            if (paramName.isEmpty()) {
                validateMsg.append("Не заполнено поле 'Название' у пользовательского параметра задачи").append("; ");
            } else {
                if (paramNames.contains(paramName)) {
                    validateMsg.append("Названия пользовательских параметров не должны повторяться").append("; ");
                } else {
                    paramNames.add(paramName);
                }
            }

            //Значение параметра
            Widget paramValueWidget = ((Widget) ((HorizontalPanel) param.getWidget(PARAM_VALUE_POSITION)).getWidget(1));
            if (paramValueWidget instanceof TextBox) {
                String paramValue = ((TextBox) paramValueWidget).getValue();
                if (paramValue.isEmpty()) {
                    validateMsg.append("Не заполнено поле 'Значение' у пользовательского параметра задачи").append("; ");
                } else {
                    TaskParamTypeValues paramType = ((ValueListBox<TaskParamTypeValues>)
                            ((HorizontalPanel) param.getWidget(PARAM_TYPE_POSITION)).getWidget(1)
                    ).getValue();
                    if ((paramType == TaskParamTypeValues.INT || paramType == TaskParamTypeValues.LONG )
                            && !checkStringAsInt(paramValue, false)) {
                        validateMsg.append("Пользовательский параметр должен быть целым числом").append("; ");
                    }
                    if ((paramType == TaskParamTypeValues.FLOAT || paramType == TaskParamTypeValues.DOUBLE)
                            && !checkStringAsInt(paramValue, true)) {
                        validateMsg.append("Пользовательский параметр должен быть дробным числом").append("; ");
                    }
                    if (paramType == TaskParamTypeValues.BOOLEAN
                            && !(paramValue.equalsIgnoreCase("true") || paramValue.equalsIgnoreCase("false"))) {
                        validateMsg.append("Пользовательский параметр должен быть равен 'true' либо 'false'").append("; ");
                    }
                }
            } else {
                if (((CustomDateBox) paramValueWidget).getValue() == null) {
                    validateMsg.append("Не заполнено поле 'Значение' у пользовательского параметра задачи").append("; ");
                }
            }
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

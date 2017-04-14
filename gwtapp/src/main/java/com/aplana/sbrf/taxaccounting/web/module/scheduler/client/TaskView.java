package com.aplana.sbrf.taxaccounting.web.module.scheduler.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.TaskParamModel;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskParam;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.client.taskparams.TaskParamsWidget;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.GetTaskInfoResult;
import com.aplana.sbrf.taxaccounting.web.widget.style.Tooltip;
import com.aplana.sbrf.taxaccounting.web.widget.utils.TextUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;

/**
 * Вью для формы "Задача планировщика"
 * @author dloshkarev
 */
public class TaskView extends ViewWithUiHandlers<TaskUiHandlers>
        implements TaskPresenter.MyView {

    @UiField
    Button createButton;

    @UiField
    Button cancelButton;

    @UiField
    TextBox taskName;

    @UiField
    TextBox taskSchedule;

    @UiField
    Image helpImage;

    @UiField LinkStyle css;

    @UiField
    Label titleDesc;

    @UiField
    HTMLPanel formPanel;

    @UiField
    AllStyles styles;

    private TaskParamsWidget paramsWidget;

    private static final String CRON_HELP = "Расписание задается в в формате IBM CRON: <br>" +
            "секунда минута час число месяц день недели <br>" +
            "День недели или месяц не могут быть указаны одновременно. <br>"+
            "Один из них может быть определен символом '?'. <br>" +
            "Примеры: <br>" +
            "0 10 * * * ? выполняется каждый час в 10 минут, т.е 0:10, 1:10 <br>" +
            "0 0/5 * * * ? выполняется каждые 5 минут <br>" +
            "0 0 21 * * ? выполняется каждый день в 21:00 <br>" +
            "0 0 18 ? SEP MON-FRI выполняется в 18:00 с понедельника по пятницу весь сентябрь";

    interface LinkStyle extends CssResource {
        String separator();
    }

    interface AllStyles extends CssResource{
        String label();
        String horSep();
        String paramsTitle();
        String scroll();
        String paramsBlock();
        String btnPanel();
        String header();
    }

    interface Binder extends UiBinder<Widget, TaskView> {
    }

    @Inject
    @UiConstructor
    public TaskView(final Binder uiBinder) {
        paramsWidget = new TaskParamsWidget();

        initWidget(uiBinder.createAndBindUi(this));
        paramsWidget.setWrapper(formPanel);
        paramsWidget.setLabelStyleName(styles.label());

        Tooltip tooltip = new Tooltip();
        tooltip.setTextHtml(TextUtils.generateTextBoxHTMLTitle(CRON_HELP));
        //установка обработчиков для тестовой строк
        tooltip.addHandlersFor(helpImage);
    }

    @UiHandler("createButton")
    public void onCreate(ClickEvent event){
        if(getUiHandlers() != null){
            getUiHandlers().onCreateTask();
        }
    }

    @UiHandler("cancelButton")
    public void onCancel(ClickEvent event){
        Dialog.confirmMessage("Отмена", "Отменить изменения?", new DialogHandler() {
            @Override
            public void yes() {
                if (getUiHandlers() != null) {
                    getUiHandlers().onCancel();
                }
            }
        });
    }

    @Override
    public LinkStyle getCss() {
        return css;
    }

    @Override
    public String getTaskName() {
        return taskName.getValue();
    }

    @Override
    public String getTaskSchedule() {
        return taskSchedule.getValue();
    }

    @Override
    public void clearForm() {
        createButton.setVisible(true);
        taskName.setValue("");
        taskSchedule.setValue("");
        paramsWidget.clear();

        taskSchedule.setEnabled(true);
    }

    @Override
    public void setTaskData(GetTaskInfoResult taskData) {
        taskName.setValue(taskData.getTaskName());
        taskSchedule.setValue(taskData.getSchedule());
        paramsWidget.setParamsValues(taskData.getParams());
    }

    @Override
    public void setMode(boolean editMode) {
        titleDesc.setText(editMode ? "Изменить задачу":"Создать задачу");
    }

    @Override
    public boolean validateTaskParams() {
        return paramsWidget.validate();
    }

    @Override
    public String getErrorsOnValidateTaskParams() {
        return paramsWidget.getErrorMessage();
    }

    @Override
    public List<SchedulerTaskParam> getTaskParams() {
        return paramsWidget.getParamsValues();
    }
}

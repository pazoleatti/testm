package com.aplana.sbrf.taxaccounting.web.module.scheduler.client;

import com.aplana.sbrf.taxaccounting.model.TaskParamModel;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.GetTaskInfoResult;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;


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
    TextBox numberOfRepeats;

    @UiField
    TextBox jndi;

    @UiField
    Button addParamButton;

    @UiField
    VerticalPanel paramsPanel;

    @UiField LinkStyle css;

    interface LinkStyle extends CssResource {
        String separator();
    }

    interface Binder extends UiBinder<Widget, TaskView> {
    }

    @Inject
    @UiConstructor
    public TaskView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("createButton")
    public void onCreate(ClickEvent event){
        if(getUiHandlers() != null){
            getUiHandlers().onCreateTask();
        }
    }

    @UiHandler("cancelButton")
    public void onCancel(ClickEvent event){
        if(getUiHandlers() != null){
            getUiHandlers().onCancel();
        }
    }

    @UiHandler("addParamButton")
    public void onAddParam(ClickEvent event){
        if(getUiHandlers() != null){
            getUiHandlers().onAddParam();
        }
    }

    @Override
    public VerticalPanel getParamsPanel() {
        return paramsPanel;
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
    public String getNumberOfRepeats() {
        return numberOfRepeats.getValue();
    }

    @Override
    public String getJndi() {
        return jndi.getValue();
    }

    @Override
    public void clearForm() {
        createButton.setVisible(true);
        addParamButton.setVisible(true);
        paramsPanel.clear();
        taskName.setValue("");
        taskSchedule.setValue("");
        numberOfRepeats.setValue("");
        jndi.setValue("");

        taskName.setReadOnly(false);
        taskSchedule.setReadOnly(false);
        numberOfRepeats.setReadOnly(false);
        jndi.setReadOnly(false);
    }

    @Override
    public void setTaskData(GetTaskInfoResult taskData) {
        createButton.setVisible(false);
        addParamButton.setVisible(false);
        taskName.setValue(taskData.getTaskName());
        taskName.setReadOnly(true);
        taskSchedule.setValue(taskData.getSchedule());
        taskSchedule.setReadOnly(true);
        numberOfRepeats.setValue(String.valueOf(taskData.getNumberOfRepeats()));
        numberOfRepeats.setReadOnly(true);
        jndi.setValue(taskData.getUserTaskJndi());
        jndi.setReadOnly(true);

        for (TaskParamModel param : taskData.getParams()) {
            getUiHandlers().onAddParam(param);
        }
    }
}

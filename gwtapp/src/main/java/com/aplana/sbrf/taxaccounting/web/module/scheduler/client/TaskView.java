package com.aplana.sbrf.taxaccounting.web.module.scheduler.client;

import com.aplana.sbrf.taxaccounting.model.TaskParamModel;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskJndiInfo;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.GetTaskInfoResult;
import com.aplana.gwt.client.ListBoxWithTooltip;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
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
    TextBox numberOfRepeats;

    @UiField(provided = true)
    ListBoxWithTooltip<TaskJndiInfo> jndi;

    @UiField
    Button addParamButton;

    @UiField
    VerticalPanel paramsPanel;

    @UiField LinkStyle css;

    @UiField
    Label titleDesc;

    private List<TaskJndiInfo> jndiList;

    interface LinkStyle extends CssResource {
        String separator();
    }

    interface Binder extends UiBinder<Widget, TaskView> {
    }

    @Inject
    @UiConstructor
    public TaskView(final Binder uiBinder) {
        jndi = new ListBoxWithTooltip<TaskJndiInfo>(new AbstractRenderer<TaskJndiInfo>() {
            @Override
            public String render(TaskJndiInfo info) {
                if (info != null) {
                    return info.getName();
                }
                return "";
            }
        });
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
        return jndi.getValue() != null ? jndi.getValue().getJndi() : "";
    }

    @Override
    public void setJndiList(List<TaskJndiInfo> jndiList) {
        jndiList.add(null);
        this.jndiList = jndiList;
        jndi.setAcceptableValues(jndiList);
        jndi.setValue(null);
    }

    @Override
    public void clearForm() {
        createButton.setVisible(true);
        addParamButton.setVisible(true);
        paramsPanel.clear();
        taskName.setValue("");
        taskSchedule.setValue("");
        numberOfRepeats.setValue("");
        jndi.setValue(null);

        taskName.setReadOnly(false);
        taskSchedule.setReadOnly(false);
        numberOfRepeats.setReadOnly(false);
        jndi.setEnabled(true);
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

        jndi.setValue(findJndiInfo(taskData.getUserTaskJndi()));
        jndi.setEnabled(true);

        for (TaskParamModel param : taskData.getParams()) {
            getUiHandlers().onAddParam(param);
        }
    }

    @Override
    public void setTitle(String title) {
        titleDesc.setText(title);
    }

    private TaskJndiInfo findJndiInfo(String jndi) {
        for (TaskJndiInfo info : jndiList) {
            if (info != null && info.getJndi()!= null && info.getJndi().equals(jndi)) {
                return info;
            }
        }
        return null;
    }
}

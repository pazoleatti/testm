package com.aplana.sbrf.taxaccounting.web.module.scriptexecution.client;

import com.aplana.sbrf.taxaccounting.web.widget.fileupload.FileUploadWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * Представление модуля выполнения скриптов из конфигуратора
 * @author Stanislav Yasinskiy
 */
public class ScriptExecutionView extends ViewWithUiHandlers<ScriptExecutionUiHandlers> implements ScriptExecutionPresenter.MyView {

    @UiField
    HasText script;
    @UiField
    FileUploadWidget uploadScript;

    @Inject
    public ScriptExecutionView(Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        uploadScript.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                script.setText(event.getValue());
            }
        });
    }

    @Override
    public String getScriptCode() {
        return script.getText();
    }

    @Override
    public void setScriptCode(String text) {
        script.setText(text);
    }

    @UiHandler("execButton")
    void onExecClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
           getUiHandlers().execScript();
        }
    }

    interface Binder extends UiBinder<Widget, ScriptExecutionView> {
    }
}

package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.script;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookScript;
import com.aplana.sbrf.taxaccounting.web.widget.codemirror.client.CodeMirror;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * Представление редактирования скрипта справочника
 *
 * @author Fail Mukhametdinov
 */
public class RefBookScriptView extends ViewWithUiHandlers<RefBookScriptUiHandlers> implements RefBookScriptPresenter.MyView,
        Editor<RefBookScript> {

    interface MyDriver extends SimpleBeanEditorDriver<RefBookScript, RefBookScriptView> {}

    private MyDriver driver;

    @UiField
    CodeMirror script;

    @Ignore
    @UiField
    Anchor returnAnchor;

    @UiField
    Label pageTitle;

    @Inject
    public RefBookScriptView(Binder uiBinder, MyDriver driver) {
        initWidget(uiBinder.createAndBindUi(this));
        this.driver = driver;
        this.driver.initialize(this);
        this.driver.edit(new RefBookScript());
    }

    @Override
    public RefBookScript getData() {
        return driver.flush();
    }

    @Override
    public void setData(RefBookScript refBookScript) {
        driver.edit(refBookScript);
    }

    @Override
    public void showSavedMessage(final boolean isRedirect) {
        Dialog.infoMessage("Сообщение", "Скрипт справочника успешно обновлен", new DialogHandler() {
            @Override
            public void ok() {
                if (getUiHandlers() != null) {
                    if (isRedirect) {
                        getUiHandlers().cancelEdit();
                    } else {
                        getUiHandlers().getScript();
                    }
                }
            }
        });
    }

    @UiHandler("saveButton")
    void onSaveClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().saveScript(false);
        }
    }

    @UiHandler("cancelButton")
    void onCancelClicked(final ClickEvent event) {
        if (driver.isDirty()){
            Dialog.confirmMessage("Редактирование справочника", "Сохранить изменения?", new DialogHandler() {
                @Override
                public void yes() {
                    if (getUiHandlers() != null) {
                        getUiHandlers().saveScript(true);
                    }
                }

                @Override
                public void no() {
                    if (getUiHandlers() != null) {
                        getUiHandlers().cancelEdit();
                        event.preventDefault();
                        event.stopPropagation();
                    }
                }
            });
        } else {
            getUiHandlers().cancelEdit();
        }
    }

    @UiHandler("resetButton")
    void onResetClicked(ClickEvent event) {
        if (driver.isDirty()){
            Dialog.confirmMessage("Редактирование справочника", "Сбросить изменения?", new DialogHandler() {
                @Override
                public void yes() {
                    if (getUiHandlers() != null) {
                        getUiHandlers().getScript();
                    }
                }
            });
        }
    }

    @UiHandler("returnAnchor")
    void onReturnAnchor(ClickEvent event) {
        onCancelClicked(event);
    }

    interface Binder extends UiBinder<Widget, RefBookScriptView> {
    }
}

package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.script;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * Представление редактирования скрипта справочника
 *
 * @author Fail Mukhametdinov
 */
public class RefBookScriptView extends ViewWithUiHandlers<RefBookScriptUiHandlers> implements RefBookScriptPresenter.MyView {

    @UiField
    HasText script;

    @UiField
    Anchor returnAnchor;

    @UiField
    Label pageTitle;

    @Inject
    public RefBookScriptView(Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public String getScriptCode() {
        return script.getText();
    }

    @Override
    public void setScriptCode(String text) {
        script.setText(text);
    }

    @Override
    public void setPageTitle(String title) {
        pageTitle.setText(title);
    }

    @Override
    public void showSavedMessage(final boolean isRedirect) {
        Dialog.infoMessage("Сообщение", "Скрипт справочника успешно обновлен", new DialogHandler() {
            @Override
            public void ok() {
                if (getUiHandlers() != null && isRedirect) {
                    getUiHandlers().cancelEdit();
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
    }

    @UiHandler("resetButton")
    void onResetClicked(ClickEvent event) {
        Dialog.confirmMessage("Редактирование справочника", "Сбросить изменения?", new DialogHandler() {
            @Override
            public void yes() {
                if (getUiHandlers() != null) {
                    getUiHandlers().getScript();
                }
            }
        });
    }

    @UiHandler("returnAnchor")
    void onReturnAnchor(ClickEvent event) {
        onCancelClicked(event);
    }

    interface Binder extends UiBinder<Widget, RefBookScriptView> {
    }
}

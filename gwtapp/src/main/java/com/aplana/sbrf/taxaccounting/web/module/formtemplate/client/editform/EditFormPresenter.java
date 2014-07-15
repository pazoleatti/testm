package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.editform;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.UpdateTableEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.EditFormTypeAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.EditFormTypeResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 * @author Eugene Stetsenko
 * Форма редактирования макета формы
 */
public class EditFormPresenter extends PresenterWidget<EditFormPresenter.MyView> implements EditFormUiHandlers {

    public interface MyView extends View, HasUiHandlers<EditFormUiHandlers> {
        void setFormTypeName(String formTypeName);
        void setFormTypeCode(String formTypeCode);
        String getFormTypeName();
        String getFormTypeCode();
    }

    DispatchAsync dispatchAsync;

    int formTypeId;
    String initFormTypeName;
    String initFormTypeCode;

    @Inject
    public EditFormPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync) {
        super(eventBus, view);
        this.dispatchAsync = dispatchAsync;
        getView().setUiHandlers(this);
    }

    public void setFormTypeId(int formTypeId) {
        this.formTypeId = formTypeId;
    }

    public void setFormTypeName(String formTypeName) {
        this.initFormTypeName = formTypeName;
        getView().setFormTypeName(formTypeName);
    }

    public void setFormTypeCode(String formTypeCode) {
        this.initFormTypeCode = formTypeCode;
        getView().setFormTypeCode(formTypeCode);
    }

    @Override
    public void onSave() {
        EditFormTypeAction action = new EditFormTypeAction();
        action.setFormTypeId(formTypeId);
        action.setNewFormTypeName(getView().getFormTypeName());
        action.setNewFormTypeCode(getView().getFormTypeCode());
        dispatchAsync.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<EditFormTypeResult>() {
                    @Override
                    public void onSuccess(EditFormTypeResult result) {
                        initFormTypeName = getView().getFormTypeName();
                        initFormTypeCode = getView().getFormTypeCode();
                        UpdateTableEvent.fire(EditFormPresenter.this);
                    }
                }, this));
    }

    @Override
    public void onCancel() {
        Dialog.confirmMessage("Редактирование макета", "Сохранить изменения?", new DialogHandler() {
            @Override
            public void yes() {
                onSave();
            }

            @Override
            public void no() {
                getView().setFormTypeName(initFormTypeName);
                getView().setFormTypeCode(initFormTypeCode);
            }
        });

    }
}

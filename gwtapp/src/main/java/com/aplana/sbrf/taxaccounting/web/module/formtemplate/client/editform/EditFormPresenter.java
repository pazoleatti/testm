package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.editform;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.UpdateTableEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.EditFormTypeNameAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.EditFormTypeNameResult;
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
        String getFormTypeName();
    }

    DispatchAsync dispatchAsync;

    int formTypeId;
    String initFormTypeName;

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

    @Override
    public void onSave() {
        EditFormTypeNameAction action = new EditFormTypeNameAction();
        action.setFormTypeId(formTypeId);
        action.setNewFormTypeName(getView().getFormTypeName());
        dispatchAsync.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<EditFormTypeNameResult>() {
                    @Override
                    public void onSuccess(EditFormTypeNameResult result) {
                        initFormTypeName = getView().getFormTypeName();
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
            }
        });

    }
}

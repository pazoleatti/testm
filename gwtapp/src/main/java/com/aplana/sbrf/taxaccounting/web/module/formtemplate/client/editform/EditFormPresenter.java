package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.editform;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.UpdateTableEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.EditFormTypeAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.EditFormTypeResult;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.FormTypeTemplate;
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
        FormTypeTemplate getTypeData();
        boolean isChangeFilter();
        void edit(FormTypeTemplate type);
    }

    DispatchAsync dispatchAsync;


    FormTypeTemplate formTypeTemplate;

    @Inject
    public EditFormPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync) {
        super(eventBus, view);
        this.dispatchAsync = dispatchAsync;
        getView().setUiHandlers(this);
    }

    public void setFormTypeTemplate(FormTypeTemplate formTypeTemplate) {
        this.formTypeTemplate = formTypeTemplate;
        if (formTypeTemplate != null) {
            getView().edit(formTypeTemplate);
        }
    }

    @Override
    public void onSave() {
        EditFormTypeAction action = new EditFormTypeAction();
        FormTypeTemplate formTypeTemplate1 = getView().getTypeData();
        action.setFormTypeId(formTypeTemplate1.getFormTypeId());
        action.setNewFormTypeName(formTypeTemplate1.getFormTypeName());
        action.setNewFormTypeCode(formTypeTemplate1.getFormTypeCode());
        action.setIsIfrs(formTypeTemplate1.getIsIfrs());
        action.setIfrsName(formTypeTemplate1.getIfrsName());
        dispatchAsync.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<EditFormTypeResult>() {
                    @Override
                    public void onSuccess(EditFormTypeResult result) {
                        formTypeTemplate = getView().getTypeData();
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
                getView().edit(formTypeTemplate);
            }
        });

    }
}

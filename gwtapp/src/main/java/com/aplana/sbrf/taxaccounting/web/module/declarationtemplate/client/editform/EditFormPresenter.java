package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.editform;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.UpdateTableEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.EditDeclarationTypeNameAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.EditDeclarationTypeNameResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.GetDeclarationTypeAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.GetDeclarationTypeResult;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 * @author Eugene Stetsenko
 * Форма редактирования макета декларации
 */
public class EditFormPresenter extends PresenterWidget<EditFormPresenter.MyView> implements EditFormUiHandlers {

    public interface MyView extends View, HasUiHandlers<EditFormUiHandlers> {
        DeclarationType getDecTypeData();
        boolean isChangeFilter();
        void edit(DeclarationType type);
    }

    DispatchAsync dispatchAsync;

    int declarationTypeId;
    DeclarationType initDeclarationType;

    @Inject
    public EditFormPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync) {
        super(eventBus, view);
        this.dispatchAsync = dispatchAsync;
        getView().setUiHandlers(this);
    }

    public void setDeclarationTypeId(int declarationTypeId) {
        this.declarationTypeId = declarationTypeId;
    }

    public void setDeclarationTypeData(int typeId) {
        GetDeclarationTypeAction action = new GetDeclarationTypeAction();
        action.setDeclarationTypeId(typeId);
        dispatchAsync.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetDeclarationTypeResult>() {
            @Override
            public void onSuccess(GetDeclarationTypeResult result) {
                changeItem(result.getDeclarationType());
            }
        }, this));
    }

    @Override
    public void onSave() {
        EditDeclarationTypeNameAction action = new EditDeclarationTypeNameAction();
        action.setNewDeclarationType(getView().getDecTypeData());
        dispatchAsync.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<EditDeclarationTypeNameResult>() {
                    @Override
                    public void onSuccess(EditDeclarationTypeNameResult result) {
                        initDeclarationType = getView().getDecTypeData();
                        getView().edit(initDeclarationType);
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
                getView().edit(initDeclarationType);
            }
        });
    }

    private void changeItem(final DeclarationType type) {
        if (getView().isChangeFilter()) {
            Dialog.confirmMessage("Редактирование макета", "Сохранить изменения?", new DialogHandler() {
                @Override
                public void yes() {
                    onSave();
                    getView().edit(type);
                }

                @Override
                public void no() {
                    initDeclarationType = type;
                    getView().edit(type);
                }
            });
        } else {
            getView().edit(type);
        }
    }
}

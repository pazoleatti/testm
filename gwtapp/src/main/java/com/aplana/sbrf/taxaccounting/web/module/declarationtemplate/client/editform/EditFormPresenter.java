package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.editform;

import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.UpdateTableEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.EditDeclarationTypeNameAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.EditDeclarationTypeNameResult;
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
        void setDeclarationTypeName(String declarationTypeName);
        String getDeclarationTypeName();
    }

    DispatchAsync dispatchAsync;

    int declarationTypeId;
    String initDeclarationTypeName;

    @Inject
    public EditFormPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync) {
        super(eventBus, view);
        this.dispatchAsync = dispatchAsync;
        getView().setUiHandlers(this);
    }

    public void setDeclarationTypeId(int declarationTypeId) {
        this.declarationTypeId = declarationTypeId;
    }

    public void setDeclarationTypeName(String declarationTypeName) {
        this.initDeclarationTypeName = declarationTypeName;
        getView().setDeclarationTypeName(declarationTypeName);
    }

    @Override
    public void onSave() {
        EditDeclarationTypeNameAction action = new EditDeclarationTypeNameAction();
        action.setDeclarationTypeId(declarationTypeId);
        action.setNewDeclarationTypeName(getView().getDeclarationTypeName());
        dispatchAsync.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<EditDeclarationTypeNameResult>() {
                    @Override
                    public void onSuccess(EditDeclarationTypeNameResult result) {
                        initDeclarationTypeName = getView().getDeclarationTypeName();
                        UpdateTableEvent.fire(EditFormPresenter.this);
                    }
                }, this));
    }

    @Override
    public void onCancel() {
        getView().setDeclarationTypeName(initDeclarationTypeName);
    }
}

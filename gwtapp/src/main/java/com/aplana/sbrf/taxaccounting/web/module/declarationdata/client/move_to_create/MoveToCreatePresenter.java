package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.move_to_create;

import com.aplana.gwt.client.dialog.Dialog;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class MoveToCreatePresenter extends PresenterWidget<MoveToCreatePresenter.MyView> implements MoveToCreateUiHandlers {

    public interface MyView extends PopupView, HasUiHandlers<MoveToCreateUiHandlers> {
        String getText();
    }

    private Long declarationDataId;
    private EventBus eventBus;

    @Inject
    public MoveToCreatePresenter(final EventBus eventBus, final MoveToCreatePresenter.MyView view) {
        super(eventBus, view);
        this.eventBus = eventBus;
        getView().setUiHandlers(this);
    }

    public void setDeclarationDataId(Long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    @Override
    public void onContinue() {
        if (isTextAreaFill()) {
            eventBus.fireEvent(new NoteEvent(declarationDataId, getView().getText()));
            getView().hide();
        } else {
            Dialog.warningMessage("Внимание!", "Необходимо указать причину возврата");
        }
    }

    @Override
    public void onCancel() {
        getView().hide();
    }

    private boolean isTextAreaFill() {
        String text = getView().getText();
        if (text != null && !text.isEmpty()) {
            return true;
        }
        return false;
    }
}

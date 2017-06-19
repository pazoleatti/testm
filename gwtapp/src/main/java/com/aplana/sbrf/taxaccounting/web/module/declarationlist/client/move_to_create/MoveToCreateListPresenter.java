package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.move_to_create;

import com.aplana.gwt.client.dialog.Dialog;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.List;

public class MoveToCreateListPresenter extends PresenterWidget<MoveToCreateListPresenter.MyView> implements MoveToCreateListUiHandlers {

    public interface MyView extends PopupView, HasUiHandlers<MoveToCreateListUiHandlers> {
        String getText();
    }

    private List<Long> declarationDataIdList;
    private EventBus eventBus;
    private final DispatchAsync dispatcher;

    @Inject
    public MoveToCreateListPresenter(final EventBus eventBus, DispatchAsync dispatcher, final MoveToCreateListPresenter.MyView view) {
        super(eventBus, view);
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    public void setDeclarationDataIdList(List<Long> declarationDataIdList) {
        this.declarationDataIdList = declarationDataIdList;
    }

    @Override
    public void onContinue() {
        if (isTextAreaFill()) {

                    eventBus.fireEvent(new CommentEvent(getView().getText(), declarationDataIdList));
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

package com.aplana.sbrf.taxaccounting.web.module.audit.client.archive;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.Date;

/**
 * User: avanteev
 */
public class AuditArchiveDialogPresenter extends PresenterWidget<AuditArchiveDialogPresenter.MyView> implements AuditArchiveUiHandlers {

    @Inject
    public AuditArchiveDialogPresenter(EventBus eventBus, MyView view) {
        super(eventBus, view);
        getView().setUiHandlers(this);
    }

    @Override
    public void onArchiveButtonClick() {
        AuditArchiveDialogEvent.fire(this, getView().getToArchiveDate());
    }

    public interface MyView extends PopupView, HasUiHandlers<AuditArchiveUiHandlers> {
        /**
         * Получаем дату до которой надо архивировать
         * @return
         */
        Date getToArchiveDate();
    }
}

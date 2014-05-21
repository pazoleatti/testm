package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.renameDialog;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import java.util.Date;

/**
 * Презентер для диалогового окна "Период применения изменений в печатных формах"
 *
 * @author aivanov
 */
public class RenameDialogPresenter
        extends PresenterWidget<RenameDialogPresenter.MyView>
        implements RenameDialogUiHandlers {

    public interface MyView extends View, HasUiHandlers<RenameDialogUiHandlers> {
        /**
         * Окртытие диалога с вводом дат
         * @param buttonClickHandler обработчик нажатия кнопки ок с выбранными датами
         */
        void open(ConfirmButtonClickHandler buttonClickHandler);

        Date getDateFrom();

        Date getDateTo();
    }

    private DispatchAsync dispatcher;
    private PlaceManager placeManager;

    @Inject
    public RenameDialogPresenter(final EventBus eventBus, final MyView view,
                                 DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view);
        this.dispatcher = dispatcher;
        this.placeManager = placeManager;
        getView().setUiHandlers(this);
    }

    public void open(ConfirmButtonClickHandler buttonClickHandler) {
        getView().open(buttonClickHandler);
    }

    public Date getDateFrom(){
        return getView().getDateFrom();
    }

    public Date getDateTo(){
        return getView().getDateTo();
    }
}

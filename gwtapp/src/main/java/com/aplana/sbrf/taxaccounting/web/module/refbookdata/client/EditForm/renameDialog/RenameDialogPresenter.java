package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.renameDialog;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

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

        void cleanDates();
    }

    @Inject
    public RenameDialogPresenter(final EventBus eventBus, final MyView view) {
        super(eventBus, view);
        getView().setUiHandlers(this);
    }

    public void open(ConfirmButtonClickHandler buttonClickHandler) {
        getView().open(buttonClickHandler);
    }
}

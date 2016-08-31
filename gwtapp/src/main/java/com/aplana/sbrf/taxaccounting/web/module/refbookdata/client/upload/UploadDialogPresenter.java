package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.upload;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.OTimerEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;

import java.util.Date;

/**
 * Презентер для диалогового окна ""
 *
 * @author aivanov
 */
public class UploadDialogPresenter
        extends PresenterWidget<UploadDialogPresenter.MyView>
        implements UploadDialogUiHandlers {

    public interface MyView extends View, HasUiHandlers<UploadDialogUiHandlers> {
        /**
         * Окртытие диалога с вводом дат
         */
        void open(boolean isVersioned);

        /**
         * Запускает загрузку файла
         */
        void load();

        /**
         * Закрытие диалога с вводом дат
         */
        void hide();
    }

    private final DispatchAsync dispatcher;

    private long refBookId;
    private String refBookName;

    @Inject
    public UploadDialogPresenter(final EventBus eventBus, final MyView view, DispatchAsync dispatcher) {
        super(eventBus, view);
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    public void open(long refBookId, boolean isVersioned) {
        this.refBookId = refBookId;
        getView().open(isVersioned);
    }

    @Override
    public void createTask(final String uuid, final Date dateFrom, final Date dateTo, boolean force) {
        LoadRefBookAction action = new LoadRefBookAction();
        action.setUuid(uuid);
        action.setDateFrom(dateFrom);
        action.setDateTo(dateTo);
        action.setRefBookId(refBookId);
        action.setForce(force);
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<LoadRefBookResult>() {
            @Override
            public void onSuccess(LoadRefBookResult result) {
                LogCleanEvent.fire(UploadDialogPresenter.this);
                LogAddEvent.fire(UploadDialogPresenter.this, result.getUuid());
                if (result.getStatus().equals(LoadRefBookResult.CreateAsyncTaskStatus.LOCKED)) {
                    Dialog.confirmMessage(result.getRestartMsg(), new DialogHandler() {
                        @Override
                        public void yes() {
                            createTask(uuid, dateFrom, dateTo, true);
                        }
                    });
                } else if (result.getStatus().equals(LoadRefBookResult.CreateAsyncTaskStatus.CREATE)) {
                    getView().hide();
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                LogCleanEvent.fire(UploadDialogPresenter.this);
                super.onFailure(caught);
            }
        }, UploadDialogPresenter.this));
    }

    @Override
    public void preLoadCheck(String fileName, final Date dateFrom, final Date dateTo) {
        PreLoadCheckRefBookAction action = new PreLoadCheckRefBookAction();
        action.setDateFrom(dateFrom);
        action.setDateTo(dateTo);
        action.setRefBookId(refBookId);
        action.setFileName(fileName);
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<PreLoadCheckRefBookResult>() {
            @Override
            public void onSuccess(PreLoadCheckRefBookResult result) {
                LogCleanEvent.fire(UploadDialogPresenter.this);
                LogAddEvent.fire(UploadDialogPresenter.this, result.getUuid());
                if (result.isError()) {
                    Dialog.errorMessage("Загрузка из файла", "Параметры загрузки заполнены неверно!");
                } else {
                    getView().load();
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                LogCleanEvent.fire(UploadDialogPresenter.this);
                super.onFailure(caught);
            }
        }, UploadDialogPresenter.this));
    }

    @Override
    public void onStartLoad() {
        LockInteractionEvent.fire(this, true);
        OTimerEvent.fire(this);
    }

    @Override
    public void onEndLoad() {
        LockInteractionEvent.fire(this, false);
    }

}

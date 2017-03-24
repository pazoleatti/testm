package com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.shared.GetUserRoleUploadTransportDataAction;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.shared.GetUserRoleUploadTransportDataResult;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.shared.LoadAllAction;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.shared.LoadAllResult;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.StartLoadFileEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.*;

/**
 * Загрузка ТФ в каталог загрузки
 *
 * @author Dmitriy Levykin
 */
public class UploadTransportDataPresenter extends Presenter<UploadTransportDataPresenter.MyView,
        UploadTransportDataPresenter.MyProxy> implements UploadTransportDataUiHandlers {

    private final DispatchAsync dispatcher;

    @ProxyCodeSplit
    @NameToken(UploadTransportDataTokens.uploadTransportData)
    public interface MyProxy extends ProxyPlace<UploadTransportDataPresenter>, Place {
    }

    public interface MyView extends View, HasUiHandlers<UploadTransportDataUiHandlers> {
        void showUpload(boolean show);
        void showLoad(boolean show);
    }

    @Inject
    public UploadTransportDataPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                                        DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    public void onStartLoad(StartLoadFileEvent event) {
        // Чистим логи и блокируем форму
        LogCleanEvent.fire(this);
        LockInteractionEvent.fire(this, true);
    }

    @Override
    public void onEndLoad(EndLoadFileEvent event) {
        // Разблокируем форму и выводим логи
        LockInteractionEvent.fire(this, false);
        LogAddEvent.fire(UploadTransportDataPresenter.this, event.getUuid());
    }

    @Override
    public void onLoadAll(final boolean force) {
        LogCleanEvent.fire(this);
        LoadAllAction action = new LoadAllAction();
        action.setForce(force);
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<LoadAllResult>() {
            @Override
            public void onSuccess(LoadAllResult result) {
                LogAddEvent.fire(UploadTransportDataPresenter.this, result.getUuid());
                if (result.isFileSizeLimit()) {
                    Dialog.confirmMessage("Обработка ТФ налоговых форм/справочников", result.getDialogMsg(), new DialogHandler() {
                        @Override
                        public void yes() {
                            onLoadAll(true);
                            super.yes();
                        }
                    });
                } else if (result.getDialogMsg() != null && !result.getDialogMsg().isEmpty()) {
                    Dialog.errorMessage(result.getDialogMsg());
                }
            }
        }, this));
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
        super.prepareFromRequest(request);
        GetUserRoleUploadTransportDataAction action = new GetUserRoleUploadTransportDataAction();
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetUserRoleUploadTransportDataResult>() {
            @Override
            public void onSuccess(GetUserRoleUploadTransportDataResult result) {
                getView().showLoad(result.isCanLoad());
                getView().showUpload(result.isCanUpload());
            }
        }, UploadTransportDataPresenter.this));
    }

    @Override
    public void onSuccess() {
    }

    @Override
    public void onFailure() {
    }
}
package com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
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
import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

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
    public void onLoadAll() {
        LogCleanEvent.fire(this);
        LoadAllAction action = new LoadAllAction();
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<LoadAllResult>() {
            @Override
            public void onSuccess(LoadAllResult result) {
                LogAddEvent.fire(UploadTransportDataPresenter.this, result.getUuid());
            }
        }, this));
    }

    @Override
    public void onSuccess() {
        Dialog.infoMessage("Загрузка транспортных файлов в каталог загрузки", "Загрузка транспортных файлов в каталог загрузки завершена");
    }

    @Override
    public void onFailure() {
        Dialog.errorMessage("Загрузка транспортных файлов в каталог загрузки", "Транспортные файлы не загружены в каталог загрузки. Обратитесь к администратору!");
    }
}
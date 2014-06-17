package com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
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

    private static String ACTION_URL = "upload/transportDataController/upload/";

    @ProxyCodeSplit
    @NameToken(UploadTransportDataTokens.uploadTransportData)
    public interface MyProxy extends ProxyPlace<UploadTransportDataPresenter>, Place {
    }

    public interface MyView extends View, HasUiHandlers<UploadTransportDataUiHandlers> {
        public void setUploadActionUrl(String actionUrl);
    }

    @Inject
    public UploadTransportDataPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                                        DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        getView().setUiHandlers(this);
        getView().setUploadActionUrl(ACTION_URL);
    }

    @Override
    public void onStartLoad(StartLoadFileEvent event) {
        LogCleanEvent.fire(this);
        LockInteractionEvent.fire(this, true);
    }

    @Override
    public void onEndLoad(EndLoadFileEvent event) {
        LockInteractionEvent.fire(this, false);
        if (event.getUuid() != null) {
            LogAddEvent.fire(UploadTransportDataPresenter.this, event.getUuid());
        }
    }
}
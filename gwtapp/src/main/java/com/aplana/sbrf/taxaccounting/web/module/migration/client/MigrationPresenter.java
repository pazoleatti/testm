package com.aplana.sbrf.taxaccounting.web.module.migration.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.migration.shared.MigrationAction;
import com.aplana.sbrf.taxaccounting.web.module.migration.shared.MigrationResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

/**
 * Presenter для формы "Миграция исторических данных"
 *
 * @author Dmitriy Levykin
 */
public class MigrationPresenter extends Presenter<MigrationPresenter.MyView,
        MigrationPresenter.MyProxy> implements MigrationUiHandlers {

    @ProxyCodeSplit
    @NameToken(MigrationTokens.migration)
    public interface MyProxy extends ProxyPlace<MigrationPresenter>, Place {
    }

    private final DispatchAsync dispatcher;

    public interface MyView extends View, HasUiHandlers<MigrationUiHandlers> {
        public void setResult(String text);
    }

    @Inject
    public MigrationPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                              DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    public void runImport() {

        dispatcher.execute(new MigrationAction(), CallbackUtils
                .defaultCallback(new AbstractCallback<MigrationResult>() {
                    @Override
                    public void onSuccess(MigrationResult result) {
                         getView().setResult(result.getResult());
                    }
                }, this));
    }
}
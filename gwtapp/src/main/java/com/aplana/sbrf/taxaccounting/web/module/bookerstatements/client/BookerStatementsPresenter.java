package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
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
 * Presenter для формы "Загрузка бухгалтерской отчётности"
 *
 * @author Dmitriy Levykin
 */
public class BookerStatementsPresenter extends Presenter<BookerStatementsPresenter.MyView,
        BookerStatementsPresenter.MyProxy> implements BookerStatementsUiHandlers {

    @ProxyCodeSplit
    @NameToken(BookerStatementsTokens.bookerStatements)
    public interface MyProxy extends ProxyPlace<BookerStatementsPresenter>, Place {
    }

    private final DispatchAsync dispatcher;

    public interface MyView extends View, HasUiHandlers<BookerStatementsUiHandlers> {
    }

    @Inject
    public BookerStatementsPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                                     DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }
}
package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client;

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
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

/**
 * Презентер для формы "Назначение форм и деклараций"
 *
 * @author Stanislav Yasinskiy
 */
public class TaxFormNominationPresenter extends Presenter<TaxFormNominationPresenter.MyView, TaxFormNominationPresenter.MyProxy> {

    @ProxyCodeSplit
    @NameToken(TaxFormNominationToken.taxFormNomination)
    public interface MyProxy extends ProxyPlace<TaxFormNominationPresenter>, Place {
    }

    public interface MyView extends View, HasUiHandlers<TaxFormNominationUiHandlers> {
    }

    private final DispatchAsync dispatcher;

    @Inject
    public TaxFormNominationPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, DispatchAsync dispatcher) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
    }

}

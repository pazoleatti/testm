package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client;

import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetTaxFormTypesAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetTaxFormTypesResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ManualRevealCallback;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.List;

/**
 * Презентер для формы "Назначение форм и деклараций"
 *
 * @author Stanislav Yasinskiy
 */
public class TaxFormNominationPresenter
        extends Presenter<TaxFormNominationPresenter.MyView, TaxFormNominationPresenter.MyProxy>
        implements TaxFormNominationUiHandlers {

    @ProxyCodeSplit
    @NameToken(TaxFormNominationToken.taxFormNomination)
    public interface MyProxy extends ProxyPlace<TaxFormNominationPresenter>, Place {
    }

    public interface MyView extends View, HasUiHandlers<TaxFormNominationUiHandlers> {
        void setTaxFormKind(List<FormType> formTypes);
        void init(Boolean isForm);
        boolean isForm();
    }

    private final DispatchAsync dispatcher;

    @Inject
    public TaxFormNominationPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, DispatchAsync dispatcher) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        getView().init(Boolean.valueOf(request.getParameter("isForm", "")));

    }

    @Override
    public void reloadTaxFormKind(TaxType taxType) {
        GetTaxFormTypesAction action = new GetTaxFormTypesAction();
        action.setTaxType(taxType);
        action.setForm(getView().isForm());
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetTaxFormTypesResult>() {
                    @Override
                    public void onSuccess(GetTaxFormTypesResult result) {
                        getView().setTaxFormKind(result.getFormTypeList());
                    }
                }, this).addCallback(new ManualRevealCallback<GetTaxFormTypesResult>(TaxFormNominationPresenter.this)));
    }

}

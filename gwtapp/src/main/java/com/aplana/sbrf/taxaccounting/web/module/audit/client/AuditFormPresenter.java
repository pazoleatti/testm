package com.aplana.sbrf.taxaccounting.web.module.audit.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.filter.AuditFilterPresenter;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

/**
 * User: avanteev
 * Date: 2013
 */
public class AuditFormPresenter extends Presenter<AuditFormPresenter.MyView, AuditFormPresenter.MyProxy> {
    interface MyView extends View{}

    @ProxyCodeSplit
    @NameToken(AuditToken.AUDIT)
    interface MyProxy extends ProxyPlace<AuditFormPresenter> {}

    static final Object TYPE_auditFilterPresenter = new Object();

    private AuditFilterPresenter auditFilterPresenter;

    @Inject
    public AuditFormPresenter(EventBus eventBus, MyView view, MyProxy proxy, AuditFilterPresenter auditFilterPresenter) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.auditFilterPresenter = auditFilterPresenter;
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(),
                this);
    }

    @Override
    protected void onHide() {
        super.onHide();
        clearSlot(TYPE_auditFilterPresenter);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        setInSlot(TYPE_auditFilterPresenter, auditFilterPresenter);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        auditFilterPresenter.initFilterData();
    }
}

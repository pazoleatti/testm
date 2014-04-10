package com.aplana.sbrf.taxaccounting.web.module.refbooklist.client;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

/**
 * Презентер списка справочников, для конфигуратора.
 *
 * @author Fail Mukhametdinov
 */
public class AdminRefBookListPresenter extends AbstractRefBookListPresenter<AdminRefBookListPresenter.MyView, AdminRefBookListPresenter.MyProxy> {

    @Inject
    public AdminRefBookListPresenter(EventBus eventBus, MyView view, MyProxy myProxy, DispatchAsync dispatchAsync) {
        super(eventBus, view, myProxy, dispatchAsync);
    }

    @ProxyCodeSplit
    @NameToken(RefBookListTokens.REFBOOK_LIST_ADMIN)
    public interface MyProxy extends ProxyPlace<AdminRefBookListPresenter> {
    }

    public interface MyView extends AbstractRefBookListPresenter.MyView {
    }

    @Override
    protected boolean getOnlyVisible() {
        return false;
    }
}

package com.aplana.sbrf.taxaccounting.web.module.refbooklist.client;

        import com.google.inject.Inject;
        import com.google.web.bindery.event.shared.EventBus;
        import com.gwtplatform.dispatch.shared.DispatchAsync;
        import com.gwtplatform.mvp.client.annotations.NameToken;
        import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
        import com.gwtplatform.mvp.client.proxy.ProxyPlace;

/**
 * Презентер списка справочников
 *
 * @author Fail Mukhametdinov
 */

public class RefBookListPresenter extends AbstractRefBookListPresenter<RefBookListPresenter.MyView, RefBookListPresenter.MyProxy> {

    @Inject
    public RefBookListPresenter(EventBus eventBus, MyView view, MyProxy myProxy, DispatchAsync dispatchAsync) {
        super(eventBus, view, myProxy, dispatchAsync);
    }

    @ProxyCodeSplit
    @NameToken(RefBookListTokens.REFBOOK_LIST)
    public interface MyProxy extends ProxyPlace<RefBookListPresenter> {
    }

    interface MyView extends AbstractRefBookListPresenter.MyView {
    }
}
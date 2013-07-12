package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.client;

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
 * Presenter для формы настроек подразделений
 *
 * @author Dmitriy Levykin
 */
public class DepartmentConfigPresenter extends Presenter<DepartmentConfigPresenter.MyView, DepartmentConfigPresenter.MyProxy> {

    @ProxyCodeSplit
    @NameToken(DepartmentConfigTokens.departamentConfig)
    public interface MyProxy extends ProxyPlace<DepartmentConfigPresenter>, Place {
    }

    public interface MyView extends View, HasUiHandlers<DepartmentConfigUiHandlers> {
    }

    private final DispatchAsync dispatcher;
    private final PlaceManager placeManager;

    @Inject
    public DepartmentConfigPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        this.placeManager = placeManager;
    }
}

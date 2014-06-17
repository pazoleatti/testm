package com.aplana.sbrf.taxaccounting.web.module.home.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class HomePagePresenter extends
		Presenter<HomePagePresenter.MyView, HomePagePresenter.MyProxy> {
	/**
	 * {@link HomePagePresenter}'s proxy.
	 */
	@ProxyCodeSplit
	@NameToken(HomeNameTokens.homePage)
	public interface MyProxy extends ProxyPlace<HomePagePresenter> {
	}

	/**
	 * {@link HomePagePresenter}'s view.
	 */
	public interface MyView extends View {
	}

	@SuppressWarnings("unused")
	private PlaceManager placeManager;
	// static final Object TYPE_testPresenter = new Object();

	@Inject
	public HomePagePresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy, PlaceManager placeManager) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.placeManager = placeManager;
	}

	@Override
	protected void onReveal() {
		super.onReveal();
        TitleUpdateEvent.fire(this, "Домашняя страница");
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
		// setInSlot(TYPE_testPresenter, filterPresenter);
	}

	@Override
	protected void onHide() {
		super.onHide();
		// clearSlot(TYPE_testPresenter);
	}

}
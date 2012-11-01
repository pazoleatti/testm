package com.aplana.sbrf.taxaccounting.gwtp.module.about.client;

import com.aplana.sbrf.taxaccounting.gwtp.main.page.client.MainPagePresenter;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class AboutPagePresenter extends
		Presenter<AboutPagePresenter.MyView, AboutPagePresenter.MyProxy> {

	/**
	 * {@link AboutPagePresenter}'s proxy.
	 */
	@ProxyCodeSplit
	@NameToken(AboutNameTokens.aboutPage)
	public interface MyProxy extends ProxyPlace<AboutPagePresenter> {
	}

	/**
	 * {@link AboutPagePresenter}'s view.
	 */
	public interface MyView extends View {
	}

	@Inject
	public AboutPagePresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy) {
		super(eventBus, view, proxy);
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetMainContent,
				this);
	}
}

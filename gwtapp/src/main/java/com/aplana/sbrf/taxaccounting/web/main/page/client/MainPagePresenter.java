package com.aplana.sbrf.taxaccounting.web.main.page.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.entry.client.ScreenLockEvent;
import com.aplana.sbrf.taxaccounting.web.widget.menu.client.MainMenuPresenter;
import com.aplana.sbrf.taxaccounting.web.widget.signin.client.SignInPresenter;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;

public class MainPagePresenter extends
		Presenter<MainPagePresenter.MyView, MainPagePresenter.MyProxy>
		implements ScreenLockEvent.MyHandler {
	/**
	 * {@link MainPagePresenter}'s proxy.
	 */
	@ProxyStandard
	public interface MyProxy extends Proxy<MainPagePresenter> {
	}

	/**
	 * {@link MainPagePresenter}'s view.
	 */
	public interface MyView extends View {
		void showLoading(boolean visibile);
		void setTitle(String text);
		void setDesc(String text);
	}

	/**
	 * Use this in leaf presenters, inside their {@link #revealInParent} method.
	 */
	@ContentSlot
	static final Type<RevealContentHandler<?>> TYPE_SetMainContent = new Type<RevealContentHandler<?>>();

	static final Object TYPE_SignInContent = new Object();
	static final Object TYPE_MainMenuContent = new Object();

	static {
		RevealContentTypeHolder.setMainContent(TYPE_SetMainContent);
	}

	private final SignInPresenter signInPresenter;
	private final MainMenuPresenter mainMenuPresenter;
	
	@SuppressWarnings("unused")
	private final PlaceManager placeManager;
	
	@Inject
	public MainPagePresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy, SignInPresenter signInPresenter,
			MainMenuPresenter mainMenuPresenter, PlaceManager placeManager) {
		super(eventBus, view, proxy);
		this.signInPresenter = signInPresenter;
		this.mainMenuPresenter = mainMenuPresenter;
		this.placeManager = placeManager;
	}

	@Override
	protected void revealInParent() {
		RevealRootContentEvent.fire(this, this);
	}

	@Override
	protected void onReveal() {
		super.onReveal();
		setInSlot(TYPE_SignInContent, signInPresenter);
		setInSlot(TYPE_MainMenuContent, mainMenuPresenter);
	}

	@Override
	protected void onHide() {
		super.onHide();
		clearSlot(TYPE_SignInContent);
		clearSlot(TYPE_MainMenuContent);
	}

	@Override
	@ProxyEvent
	public void onScreenLock(ScreenLockEvent event) {
		if (event.isLock()) {
			getView().showLoading(true);
		} else {
			getView().showLoading(false);
		}
	}

}

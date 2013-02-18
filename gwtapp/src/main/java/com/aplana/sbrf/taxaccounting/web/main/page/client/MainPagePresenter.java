package com.aplana.sbrf.taxaccounting.web.main.page.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.ErrorEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.main.entry.client.ScreenLockEvent;
import com.aplana.sbrf.taxaccounting.web.main.page.shared.GetProjectVersion;
import com.aplana.sbrf.taxaccounting.web.main.page.shared.GetProjectVersionResult;
import com.aplana.sbrf.taxaccounting.web.widget.menu.client.MainMenuPresenter;
import com.aplana.sbrf.taxaccounting.web.widget.signin.client.SignInPresenter;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.*;

public class MainPagePresenter extends
		Presenter<MainPagePresenter.MyView, MainPagePresenter.MyProxy>
		implements ScreenLockEvent.MyHandler, TitleUpdateEvent.MyHandler,
		MessageEvent.MyHandler, NavigationHandler {
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

		void updateTitle(String title, String desc);

		void setProjectVersion(String version);
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

	private final MessageDialogPresenter messageDialogPresenter;

	private final DispatchAsync dispatchAsync;

	/**
	 * Флаг показывает, что заголовки обновлены через UpdateEvent и в обновлении
	 * их через GWTP (@Title, @TitleFunction) - нет необходимости.
	 */
	private boolean titleUpdatedFromEvent = false;

	private final PlaceManager placeManager;

	@Inject
	public MainPagePresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy, SignInPresenter signInPresenter,
			MainMenuPresenter mainMenuPresenter, PlaceManager placeManager,
			MessageDialogPresenter messageDialogPresenter, DispatchAsync dispatchAsync) {
		super(eventBus, view, proxy);
		this.signInPresenter = signInPresenter;
		this.mainMenuPresenter = mainMenuPresenter;
		this.placeManager = placeManager;
		this.messageDialogPresenter = messageDialogPresenter;
		this.dispatchAsync = dispatchAsync;
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
		this.dispatchAsync.execute(new GetProjectVersion(),
				new AbstractCallback<GetProjectVersionResult>() {
					@Override
					public void onReqSuccess(GetProjectVersionResult result) {
						getView().setProjectVersion(result.getProjectVersion());
					}
				});
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

	@Override
	public void setInSlot(Object slot, PresenterWidget<?> content) {
		if (TYPE_SetMainContent.equals(slot)) {
			if (!titleUpdatedFromEvent) {
				placeManager.getCurrentTitle(new SetPlaceTitleHandler() {
					@Override
					public void onSetPlaceTitle(String title) {
						updateTitle(title, null);
					}
				});
			}

		}
		// TODO Auto-generated method stub
		super.setInSlot(slot, content);
	}

	@Override
	@ProxyEvent
	public void onNavigation(NavigationEvent navigationEvent) {
		titleUpdatedFromEvent = false;
	}

	@Override
	@ProxyEvent
	public void onTitleUpdate(final TitleUpdateEvent event) {
		titleUpdatedFromEvent = true;
		updateTitle(event.getTitle(), event.getDesc());
	}

	/**
	 * 
	 * Обновляет/Добавляет/Сбрасывает заголовки страницы.
	 * 
	 * @param title
	 * @param desc
	 */
	private void updateTitle(String title, String desc) {
		getView().updateTitle(title, desc);

		StringBuilder pageTitleBuilder = new StringBuilder(
				"АС \"Учет налогов\"");
		if (Document.get() != null) {
			pageTitleBuilder.append(title != null ? " - " + title : "");
			pageTitleBuilder.append(desc != null ? " : " + desc : "");
			Document.get().setTitle(pageTitleBuilder.toString());
		}
	}

	@ProxyEvent
	@Override
	public void onPopUpMessage(MessageEvent event) {
		if (isVisible()) {
			messageDialogPresenter.setMessageEvent(event);
			addToPopupSlot(messageDialogPresenter);
		} else {
			ErrorEvent.fire(this, event);
		}

	}

}

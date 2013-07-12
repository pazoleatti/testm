package com.aplana.sbrf.taxaccounting.web.module.about.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.widget.version.client.ProjectVersionPresenter;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class AboutPresenter extends
		Presenter<AboutPresenter.MyView, AboutPresenter.MyProxy> {
	/**
	 * {@link AboutPresenter}'s proxy.
	 */
	@ProxyCodeSplit
	@NameToken(AboutNameTokens.ABOUT_PAGE)
	public interface MyProxy extends ProxyPlace<AboutPresenter> {
	}

	/**
	 * {@link AboutPresenter}'s view.
	 */
	public interface MyView extends View {
	}

	private final ProjectVersionPresenter versionPresenter;
	static final Object TYPE_ProjectVersionContent = new Object();

	@Inject
	public AboutPresenter(final EventBus eventBus, final MyView view,
						  final MyProxy proxy, ProjectVersionPresenter versionPresenter) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.versionPresenter = versionPresenter;
	}

	@Override
	protected void onReveal() {
		super.onReveal();
		TitleUpdateEvent.fire(this, "О программе");
		setInSlot(TYPE_ProjectVersionContent, versionPresenter);
	}

	@Override
	protected void onHide() {
		super.onHide();
		clearSlot(TYPE_ProjectVersionContent);
	}
}
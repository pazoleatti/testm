package com.aplana.sbrf.taxaccounting.web.module.error.client;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.ErrorEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class ErrorPagePresenter extends
		Presenter<ErrorPagePresenter.MyView, ErrorPagePresenter.MyProxy>
		implements ErrorEvent.MyHandler {

	@ProxyStandard
	@NameToken(ErrorNameTokens.ERROR)
	public interface MyProxy extends ProxyPlace<ErrorPagePresenter> {
	}

	public interface MyView extends View {
		void setMessage(String text);
		
		void setStackTrace(Throwable throwable);
		
		void setLog(List<LogEntry> log);
	}

	private PlaceManager placeManager;

	@Inject
	public ErrorPagePresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy, PlaceManager placeManager) {
		super(eventBus, view, proxy);
		this.placeManager = placeManager;
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(),
				this);
	}

	@Override
	@ProxyEvent
	public void onError(ErrorEvent event) {

		getView().setMessage(event.getMessage());
		if (event.getThrowable() != null) {
			getView().setStackTrace(event.getThrowable());
			getView().setLog(event.getLogEntries());
		}
		placeManager.unlock();
		placeManager.revealErrorPlace(placeManager.getCurrentPlaceRequest()
				.getNameToken());

	}

}

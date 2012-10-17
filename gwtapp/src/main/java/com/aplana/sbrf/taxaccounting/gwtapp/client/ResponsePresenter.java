package com.aplana.sbrf.taxaccounting.gwtapp.client;

import com.aplana.sbrf.taxaccounting.gwtapp.shared.SendTextToServer;
import com.aplana.sbrf.taxaccounting.gwtapp.shared.SendTextToServerResult;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.inject.Inject;

public class ResponsePresenter extends
Presenter<ResponsePresenter.MyView, ResponsePresenter.MyProxy> {
	/**
	 * {@link com.aplana.sbrf.taxaccounting.gwtapp.client.ResponsePresenter}'s proxy.
	 */
	@ProxyCodeSplit
	@NameToken(NAME_TOKEN)
	public interface MyProxy extends Proxy<ResponsePresenter>, Place {
	}

	/**
	 * {@link com.aplana.sbrf.taxaccounting.gwtapp.client.ResponsePresenter}'s view.
	 */
	public interface MyView extends View {
		Button getCloseButton();

		void setServerResponse(String serverResponse);

		void setTextToServer(String textToServer);
	}

	public static final String NAME_TOKEN = "response";

	public static final String textToServerParam = "textToServer";

	private final DispatchAsync dispatcher;
	private final PlaceManager placeManager;

	private String textToServer;

	@Inject
	public ResponsePresenter(EventBus eventBus, MyView view, MyProxy proxy,
			PlaceManager placeManager, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		this.placeManager = placeManager;
		this.dispatcher = dispatcher;
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
		textToServer = request.getParameter(textToServerParam, null);
	}

	@Override
	protected void onBind() {
		super.onBind();
		registerHandler(getView().getCloseButton().addClickHandler(
				new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						placeManager.revealPlace(new PlaceRequest(
								FormDataListPresenter.nameToken));
					}
				}));
	}

	@Override
	protected void onReset() {
		super.onReset();
		getView().setTextToServer(textToServer);
		getView().setServerResponse("Waiting for response...");
		dispatcher.execute(new SendTextToServer(textToServer),
				new AsyncCallback<SendTextToServerResult>() {
			@Override
			public void onFailure(Throwable caught) {
				getView().setServerResponse(
						"An error occured: " + caught.getMessage());
			}

			@Override
			public void onSuccess(SendTextToServerResult result) {
				getView().setServerResponse(result.getResponse());
			}
		});
	}

	@Override
	protected void revealInParent() {
		RevealRootContentEvent.fire(this, this);
	}
}

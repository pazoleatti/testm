package com.aplana.sbrf.taxaccounting.gwtapp.client;

import com.aplana.sbrf.taxaccounting.gwtapp.shared.FieldVerifier;
import com.aplana.sbrf.taxaccounting.gwtapp.shared.GetFormDataList;
import com.aplana.sbrf.taxaccounting.gwtapp.shared.RecordList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;

public class FormDataListPresenter extends Presenter<FormDataListPresenter.MyView, FormDataListPresenter.MyProxy> {
	/**
	 * {@link com.aplana.sbrf.taxaccounting.gwtapp.client.FormDataListPresenter}'s proxy.
	 */
	@ProxyStandard
	@NameToken(nameToken)
	public interface MyProxy extends Proxy<FormDataListPresenter>, Place {
	}

	/**
	 * {@link com.aplana.sbrf.taxaccounting.gwtapp.client.FormDataListPresenter}'s view.
	 */
	public interface MyView extends View {
		String getName();

		Button getSendButton();

		void resetAndFocus();

		void setError(String errorText);
	}

	public static final String nameToken = "main";

	private final PlaceManager placeManager;
	private final DispatchAsync dispatcher;


	@Inject
	public FormDataListPresenter(EventBus eventBus, MyView view, MyProxy proxy,
			PlaceManager placeManager, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		this.placeManager = placeManager;
		this.dispatcher = dispatcher;
	}

	@Override
	protected void onBind() {
		super.onBind();
		registerHandler(getView().getSendButton().addClickHandler(
				new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						sendNameToServer();
					}
				}));
	}

	@Override
	protected void onReset() {
		super.onReset();
		getView().resetAndFocus();
		
		loadFormDataList();
	}

	@Override
	protected void revealInParent() {
		RevealRootContentEvent.fire(this, this);
	}

	/**
	 * Send the name from the nameField to the server and wait for a response.
	 */
	protected void sendNameToServer() {
		// First, we validate the input.
		getView().setError("");
		String textToServer = getView().getName();
		if (!FieldVerifier.isValidName(textToServer)) {
			getView().setError("Please enter at least four characters");
			return;
		}

		// Then, we transmit it to the ResponsePresenter, which will do the server
		// call
		placeManager.revealPlace(new PlaceRequest(ResponsePresenter.NAME_TOKEN).with(
			ResponsePresenter.textToServerParam, textToServer));
	}
	
	protected void loadFormDataList() {
		System.out.println("--> HERE!!! 1");
		
		dispatcher.execute(new GetFormDataList(), new AsyncCallback<RecordList<String>>() {

			@Override
			public void onFailure(Throwable caught) {
				System.out.println("--> HERE!!! Failed: " + caught.getCause().getMessage());
				// TODO: log error
				caught.printStackTrace();
			}

			@Override
			public void onSuccess(RecordList<String> result) {
				// TODO: Заполнить табличку
				System.out.println("--> HERE!!! 2: " + result.getRecords().size());
			}
		});
	}
}

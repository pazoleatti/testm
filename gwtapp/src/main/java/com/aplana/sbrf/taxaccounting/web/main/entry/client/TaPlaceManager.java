package com.aplana.sbrf.taxaccounting.web.main.entry.client;

import com.aplana.sbrf.taxaccounting.web.module.error.client.ErrorNameTokens;
import com.gwtplatform.mvp.client.proxy.PlaceManagerImpl;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TokenFormatter;


import com.google.web.bindery.event.shared.EventBus;
import com.google.inject.Inject;


public class TaPlaceManager extends PlaceManagerImpl {
	private final PlaceRequest defaultPlaceRequest;

	@Inject
	public TaPlaceManager(final EventBus eventBus,
			final TokenFormatter tokenFormatter, @DefaultPlace String defaultNameToken) {
		super(eventBus, tokenFormatter);

		this.defaultPlaceRequest = new PlaceRequest(defaultNameToken);
	}

	@Override
	public void revealDefaultPlace() {
		revealPlace(defaultPlaceRequest);
	}
	
	
	@Override
	public void revealErrorPlace(String invalidHistoryToken) {
		revealPlace(new PlaceRequest(ErrorNameTokens.ERROR));
	}
	
	
}
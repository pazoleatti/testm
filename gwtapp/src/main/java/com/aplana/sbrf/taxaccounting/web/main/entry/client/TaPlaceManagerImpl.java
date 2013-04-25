package com.aplana.sbrf.taxaccounting.web.main.entry.client;


import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.module.error.client.ErrorNameTokens;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.History;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManagerImpl;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TokenFormatter;


public class TaPlaceManagerImpl extends PlaceManagerImpl implements TaPlaceManager{
	private final PlaceRequest defaultPlaceRequest;
	
	private boolean quietlyChange = false;

	@Inject
	public TaPlaceManagerImpl(final EventBus eventBus,
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
	
	
	public void navigateBackQuietly() {
		quietlyChange = true;
		History.back();
	}
	

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		if (quietlyChange){
			quietlyChange = false;
			return;
		}
		super.onValueChange(event);
	}
	
	
}
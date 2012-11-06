package com.aplana.sbrf.taxaccounting.web.main.entry.client;

import com.gwtplatform.mvp.client.proxy.PlaceManagerImpl;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TokenFormatter;


import com.google.web.bindery.event.shared.EventBus;
import com.google.inject.Inject;


public class PlaceManager extends PlaceManagerImpl {
  private final PlaceRequest defaultPlaceRequest;

  @Inject
  public PlaceManager(final EventBus eventBus,
      final TokenFormatter tokenFormatter, @DefaultPlace String defaultNameToken) {
    super(eventBus, tokenFormatter);

    this.defaultPlaceRequest = new PlaceRequest(defaultNameToken);
  }

  @Override
  public void revealDefaultPlace() {
    revealPlace(defaultPlaceRequest);
  }
}
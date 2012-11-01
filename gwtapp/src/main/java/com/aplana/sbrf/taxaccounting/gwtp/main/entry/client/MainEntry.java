package com.aplana.sbrf.taxaccounting.gwtp.main.entry.client;

import com.gwtplatform.mvp.client.DelayedBindRegistry;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;


public class MainEntry implements EntryPoint {
  public final ClientGinjector ginjector = GWT.create(ClientGinjector.class);

  @Override
  public final void onModuleLoad() {
    DelayedBindRegistry.bind(ginjector);
    ginjector.getPlaceManager().revealCurrentPlace();
  }
}

package com.aplana.sbrf.taxaccounting.gwtapp.client;

import com.gwtplatform.mvp.client.DelayedBindRegistry;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

public class TaxAccounting implements EntryPoint {
	public final MyGinjector ginjector = GWT.create(MyGinjector.class);

	public void onModuleLoad() {
		DelayedBindRegistry.bind(ginjector);
		ginjector.getPlaceManager().revealCurrentPlace();
	}
}

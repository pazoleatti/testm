package com.aplana.sample.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.gwtplatform.mvp.client.DelayedBindRegistry;

/**
 * @author Vitaliy Samolovskikh
 */
public class MainEntryPoint implements EntryPoint {
	public final SampleGinjector ginjector = GWT.create(SampleGinjector.class);

	public void onModuleLoad() {
		DelayedBindRegistry.bind(ginjector);

		ginjector.getPlaceManager().revealCurrentPlace();
	}
}

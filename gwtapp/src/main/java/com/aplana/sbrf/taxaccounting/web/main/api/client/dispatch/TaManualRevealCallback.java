package com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch;

import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.proxy.ManualRevealCallback;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

/**
 * Альтернатива ManualRevealCallback
 * с решением проблемы отката URL
 * 
 * http://jira.aplana.com/browse/SBRFACCTAX-2120
 * 
 * @author sgoryachkin
 *
 * @param <T>
 */
public final class TaManualRevealCallback<T> extends ManualRevealCallback<T> {
	
	private final TaPlaceManager placeManager;
	
	public static <T> AsyncCallback<T> create(Presenter<?, ? extends ProxyPlace<?>> presenter, TaPlaceManager placeManager) {
		return new TaManualRevealCallback<T>(presenter, placeManager);
	}

	private TaManualRevealCallback(
			Presenter<?, ? extends ProxyPlace<?>> presenter, TaPlaceManager placeManager) {
		super(presenter);
		this.placeManager = placeManager;
	}

	@Override
	public void onFailure(Throwable caught) {
		placeManager.navigateBackQuietly();
		super.onFailure(caught);
	}
}

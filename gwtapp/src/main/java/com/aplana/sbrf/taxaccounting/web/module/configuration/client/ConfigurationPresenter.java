package com.aplana.sbrf.taxaccounting.web.module.configuration.client;

import java.util.List;

import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.TaManualRevealCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.ConfigTuple;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.GetConfigurationAction;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.GetConfigurationResult;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.SaveConfigurationAction;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.SaveConfigurationResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class ConfigurationPresenter	extends	Presenter<ConfigurationPresenter.MyView, ConfigurationPresenter.MyProxy> implements ConfigurationUiHandlers{
	
	public static final String TOKEN = "!configuration";

	@ProxyStandard
	@NameToken(TOKEN)
	public interface MyProxy extends ProxyPlace<ConfigurationPresenter> {
	}

	public interface MyView extends View, HasUiHandlers<ConfigurationUiHandlers>{
		void setConfigData(List<ConfigTuple> data);
		List<ConfigTuple> getConfigData();
	}

	private final DispatchAsync dispatcher;
	private final TaPlaceManager placeManager;

	@Inject
	public ConfigurationPresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy, PlaceManager placeManager,
			DispatchAsync dispatcher) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		getView().setUiHandlers(this);
		this.placeManager = (TaPlaceManager) placeManager;
		this.dispatcher = dispatcher;
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		GetConfigurationAction action = new GetConfigurationAction();
		dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetConfigurationResult>() {

							@Override
							public void onSuccess(GetConfigurationResult result) {
								getView().setConfigData(result.getData());
							}
                        	
                        }, this).addCallback(TaManualRevealCallback.create(this, placeManager)));
	}

	@Override
	public boolean useManualReveal() {
		return true;
	}

	@Override
	public void save() {
		LogCleanEvent.fire(this);
		SaveConfigurationAction action = new SaveConfigurationAction();
		action.setData(getView().getConfigData());
		dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<SaveConfigurationResult>() {

			@Override
			public void onSuccess(SaveConfigurationResult result) {
				placeManager.revealCurrentPlace();
			}
			
		}, this));
	}

	@Override
	public void reload() {
		placeManager.revealCurrentPlace();
	}

}

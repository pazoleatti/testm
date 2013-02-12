package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.DeclarationSearchResultItem;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.DeclarationFilterPresenter;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

import java.util.List;

public class DeclarationListPresenterBase<Proxy_ extends ProxyPlace<?>> extends
		Presenter<DeclarationListPresenterBase.MyView, Proxy_> {

	public interface MyView extends View, HasUiHandlers<DeclarationListUiHandlers> {
		void setDeclarationsList(int start, long totalCount, List<DeclarationSearchResultItem> records);

		void assignDataProvider(int pageSize, AbstractDataProvider<DeclarationSearchResultItem> data);

		DeclarationSearchOrdering getSearchOrdering();

		boolean isAscSorting();
	}

	protected final DispatchAsync dispatcher;
	protected final PlaceManager placeManager;
	protected final DeclarationFilterPresenter filterPresenter;
	static final Object TYPE_filterPresenter = new Object();

	public DeclarationListPresenterBase(EventBus eventBus, MyView view, Proxy_ proxy,
	                             PlaceManager placeManager, DispatchAsync dispatcher,
	                             DeclarationFilterPresenter filterPresenter) {
		super(eventBus, view, proxy);
		this.placeManager = placeManager;
		this.dispatcher = dispatcher;
		this.filterPresenter = filterPresenter;
	}

	@Override
	public boolean useManualReveal() {
		return true;
	}

	@Override
	protected void onReveal() {
		super.onReveal();
		setInSlot(TYPE_filterPresenter, filterPresenter);
	}

	@Override
	protected void onHide() {
		super.onHide();
		clearSlot(TYPE_filterPresenter);
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(),
				this);
	}
}

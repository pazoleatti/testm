package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.creation.DeclarationCreationPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.DeclarationFilterPresenter;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.List;

public class DeclarationListPresenterBase<Proxy_ extends ProxyPlace<?>> extends
		Presenter<DeclarationListPresenterBase.MyView, Proxy_> {

	public interface MyView extends View, HasUiHandlers<DeclarationListUiHandlers> {
        void updateData(int pageNumber);

        void setTableData(int start, long totalCount, List<DeclarationDataSearchResultItem> records);

        void updateData();

		DeclarationDataSearchOrdering getSearchOrdering();

		boolean isAscSorting();

		void updateTitle(String title);

	}

	protected final DispatchAsync dispatcher;
	protected final PlaceManager placeManager;
	protected final DeclarationFilterPresenter filterPresenter;
	protected final DeclarationCreationPresenter creationPresenter;
	static final Object TYPE_filterPresenter = new Object();

	public DeclarationListPresenterBase(EventBus eventBus, MyView view, Proxy_ proxy,
	                             PlaceManager placeManager, DispatchAsync dispatcher,
	                             DeclarationFilterPresenter filterPresenter,
								 DeclarationCreationPresenter creationPresenter) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.placeManager = placeManager;
		this.dispatcher = dispatcher;
		this.filterPresenter = filterPresenter;
		this.creationPresenter = creationPresenter;
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

}

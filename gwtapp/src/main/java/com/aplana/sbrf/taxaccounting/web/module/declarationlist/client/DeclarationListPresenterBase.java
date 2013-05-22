package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.creation.DeclarationCreationPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.*;
import com.google.gwt.view.client.*;
import com.google.web.bindery.event.shared.*;
import com.gwtplatform.dispatch.shared.*;
import com.gwtplatform.mvp.client.*;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.*;

public class DeclarationListPresenterBase<Proxy_ extends ProxyPlace<?>> extends
		Presenter<DeclarationListPresenterBase.MyView, Proxy_> {

	public interface MyView extends View, HasUiHandlers<DeclarationListUiHandlers> {
		void setDeclarationsList(int start, long totalCount, List<DeclarationDataSearchResultItem> records);

		void assignDataProvider(int pageSize, AbstractDataProvider<DeclarationDataSearchResultItem> data);

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

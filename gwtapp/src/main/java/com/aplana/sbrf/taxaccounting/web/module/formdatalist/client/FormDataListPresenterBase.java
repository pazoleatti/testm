/**
 * Copyright 2011 ArcBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.aplana.sbrf.taxaccounting.model.FormDataSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterPresenter;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

import java.util.List;

/**
 * The base class of {@link ContactPagePresenter}. The goal of this class is
 * just to show that {@code @ProxyEvent} can be used in base classes. See Issue
 * 180.
 * 
 * @author Philippe Beaudoin
 * 
 * @param <Proxy_>
 *            The {@link Proxy} type.
 */
public abstract class FormDataListPresenterBase<Proxy_ extends Proxy<?>>
		extends Presenter<FormDataListPresenterBase.MyView, Proxy_> {

	/**
	 * View.
	 */
	public interface MyView extends View, HasUiHandlers<FormDataListUiHandlers> {
		
		void setFormDataList(int start, long totalCount, List<FormDataSearchResultItem> records);

		void assignDataProvider(int pageSize, AbstractDataProvider<FormDataSearchResultItem> data);

		FormDataSearchOrdering getSearchOrdering();

		boolean isAscSorting();
	}
	
	protected final PlaceManager placeManager;
	protected final DispatchAsync dispatcher;

	protected final FilterPresenter filterPresenter;
	static final Object TYPE_filterPresenter = new Object();
	

	public FormDataListPresenterBase(EventBus eventBus, MyView view,
			Proxy_ proxy, PlaceManager placeManager, DispatchAsync dispatcher,
			FilterPresenter filterPresenter) {
		super(eventBus, view, proxy);
		this.placeManager = placeManager;
		this.dispatcher = dispatcher;
		this.filterPresenter = filterPresenter;
	}
	
	/* (non-Javadoc)
	 * @see com.gwtplatform.mvp.client.Presenter#useManualReveal()
	 */
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

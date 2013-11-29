package com.aplana.sbrf.taxaccounting.web.module.members.client;

import com.aplana.sbrf.taxaccounting.model.MembersFilterData;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserFull;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.members.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.Title;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

/**
 * User: Eugene Stetsenko
 * Date: 2013
 */
public class MembersPresenter extends Presenter<MembersPresenter.MyView, MembersPresenter.MyProxy> implements MembersUiHandlers {

    private final DispatchAsync dispatcher;

    @Inject
    public MembersPresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatcher) {
        super(eventBus, view, proxy);
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

	@Override
	public void applyFilter() {
		getView().updateData(0);
	}

    @Override
    public void onPrintClicked() {
	    PrintAction action = new PrintAction();
		action.setMembersFilterData(getView().getFilter());
	    dispatcher.execute(action,
			    CallbackUtils.defaultCallback(new AbstractCallback<PrintResult>() {
				    @Override
				    public void onSuccess(PrintResult result) {
					    getView().getBlobFromServer(result.getUuid());
				    }
			    }, this));
    }

    @Title("Пользователи")
    @ProxyCodeSplit
    @NameToken(MembersTokens.MEMBERS)
    public interface MyProxy extends ProxyPlace<MembersPresenter> {
    }

    public interface MyView extends View, HasUiHandlers<MembersUiHandlers> {
        void setTaUserFullCellTable(PagingResult<TAUserFull> userFullList, int startIndex);
	    MembersFilterData getFilter();
	    void updateData();
	    void updateData(int pageNumber);
	    void setFilterData(FilterValues values);
	    void getBlobFromServer(String uuid);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
	    GetFilterValues action = new GetFilterValues();
	    dispatcher.execute(action,
		    CallbackUtils.defaultCallback(new AbstractCallback<FilterValues>() {
					    @Override
					    public void onSuccess(FilterValues result) {
						    getView().setFilterData(result);
					    }
				    }, this));
	    getView().updateData(0);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(),
                this);
    }

	@Override
	public void onRangeChange(final int start, int length) {
		MembersFilterData filter = getView().getFilter();
		filter.setCountOfRecords(length);
		filter.setStartIndex(start);
		GetMembersAction action = new GetMembersAction();
		action.setMembersFilterData(filter);
		dispatcher.execute(action,
				new AbstractCallback<GetMembersResult>() {
					@Override
					public void onSuccess(GetMembersResult result) {
						getView().setTaUserFullCellTable(result.getTaUserList(), result.getStartIndex());
					}
				}
		);
	}
}

package com.aplana.sbrf.taxaccounting.web.module.members.client;

import com.aplana.sbrf.taxaccounting.model.MembersFilterData;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserView;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
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
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

/**
 * User: Eugene Stetsenko
 * Date: 2013
 */
public class MembersPresenter extends Presenter<MembersPresenter.MyView, MembersPresenter.MyProxy> implements MembersUiHandlers {

    private final DispatchAsync dispatcher;

    private boolean isFormModified;
    private final PlaceManager placeManager;
    private TAUserView selectUser;

    @Inject
    public MembersPresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view, proxy);
        this.dispatcher = dispatcher;
        this.placeManager = placeManager;
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
        void setTaUserFullCellTable(PagingResult<TAUserView> userFullList, int startIndex);
	    MembersFilterData getFilter();
	    void updateData();
	    void updateData(int pageNumber);
	    void setFilterData(FilterValues values);
	    void getBlobFromServer(String uuid);
        void setMode(MembersView.FormMode mode);
        void setCanEdit(boolean canEdit);
        TAUserView getTAUserView();
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
	    GetFilterValues action = new GetFilterValues();
        getView().setMode(MembersView.FormMode.READ);
	    dispatcher.execute(action,
		    CallbackUtils.defaultCallback(new AbstractCallback<FilterValues>() {
					    @Override
					    public void onSuccess(FilterValues result) {
						    getView().setFilterData(result);
                            getView().updateData(0);
                            getView().setCanEdit(result.isCanEdit());
                            getView().setMode(MembersView.FormMode.READ);
                        }
				    }, this));
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

    @Override
    public void onSave() {
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
        final SaveUserAction action = new SaveUserAction();
        action.setTaUserView(getView().getTAUserView());
        dispatcher.execute(action,
                CallbackUtils.defaultCallback(new AbstractCallback<SaveUserResult>() {
                    @Override
                    public void onSuccess(SaveUserResult result) {
                        LogAddEvent.fire(MembersPresenter.this, result.getUuid());
                        selectUser = getView().getTAUserView();
                        getView().setMode(MembersView.FormMode.EDIT);
                        getView().updateData();
                        setIsFormModified(false);
                    }
                }, this)
        );
    }


    @Override
    public void setIsFormModified(boolean isFormModified) {
        this.isFormModified = isFormModified;
        if (isFormModified) {
            placeManager.setOnLeaveConfirmation("Вы подтверждаете отмену изменений?");
        } else {
            placeManager.setOnLeaveConfirmation(null);
        }
    }

    @Override
    public boolean isFormModified() {
        return isFormModified;
    }

    @Override
    public TAUserView getSelectUser() {
        return selectUser;
    }

    @Override
    public void setSelectUser(TAUserView selectUser) {
        this.selectUser = selectUser;
    }
}

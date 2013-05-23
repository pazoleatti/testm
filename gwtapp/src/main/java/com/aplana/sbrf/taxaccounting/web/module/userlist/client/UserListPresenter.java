package com.aplana.sbrf.taxaccounting.web.module.userlist.client;

import com.aplana.sbrf.taxaccounting.model.TAUserFull;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.module.userlist.shared.GetUserListAction;
import com.aplana.sbrf.taxaccounting.web.module.userlist.shared.GetUserListResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
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

import java.util.List;

/**
 * User: avanteev
 * Date: 2013
 */
public class UserListPresenter extends Presenter<UserListPresenter.MyView, UserListPresenter.MyProxy> implements UserListUiHandlers {

    private final DispatchAsync dispatcher;

    @Inject
    public UserListPresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatcher) {
        super(eventBus, view, proxy);
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    public void onPrintClicked() {
        Window.open(
                GWT.getHostPageBaseURL() + "download/downloadController/processSecUserDownload/","",""
        );
    }

    @Title("Пользователи АС Учет налогов")
    @ProxyCodeSplit
    @NameToken(UserListTokens.secuserPage)
    public interface MyProxy extends ProxyPlace<UserListPresenter> {
    }

    public interface MyView extends View, HasUiHandlers<UserListUiHandlers> {
        void setTaUserFullCellTable(List<TAUserFull> userFullList);

    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        dispatcher.execute(new GetUserListAction(),
                new AbstractCallback<GetUserListResult>() {
                    @Override
                    public void onSuccess(GetUserListResult result) {
                        getView().setTaUserFullCellTable(result.getTaUserList());
                    }
                }
        );
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(),
                this);
    }
}

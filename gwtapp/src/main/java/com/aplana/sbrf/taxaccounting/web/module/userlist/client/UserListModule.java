package com.aplana.sbrf.taxaccounting.web.module.userlist.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * User: avanteev
 * Date: 2013
 */
public class UserListModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(UserListPresenter.class, UserListPresenter.MyView.class, UserListView.class, UserListPresenter.MyProxy.class);
    }
}

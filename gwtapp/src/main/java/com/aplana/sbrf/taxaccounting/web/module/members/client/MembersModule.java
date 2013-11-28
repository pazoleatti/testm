package com.aplana.sbrf.taxaccounting.web.module.members.client;

import com.aplana.sbrf.taxaccounting.web.module.userlist.client.UserListPresenter;
import com.aplana.sbrf.taxaccounting.web.module.userlist.client.UserListView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * User: Eugene Stetsenko
 * Date: 2013
 */
public class MembersModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(MembersPresenter.class, MembersPresenter.MyView.class, MembersView.class, MembersPresenter.MyProxy.class);
    }
}

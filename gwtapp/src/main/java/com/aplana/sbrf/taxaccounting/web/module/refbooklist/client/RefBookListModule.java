package com.aplana.sbrf.taxaccounting.web.module.refbooklist.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Модуль для формы списка справочников
 *
 * @author Stanislav Yasinskiy
 * @author Fail Mukhametdinov
 */
public class RefBookListModule extends AbstractPresenterModule {

    @Override
    protected void configure() {

        bindPresenter(RefBookListPresenter.class, RefBookListPresenter.MyView.class,
                RefBookListView.class, RefBookListPresenter.MyProxy.class);
        bindPresenter(AdminRefBookListPresenter.class, AdminRefBookListPresenter.MyView.class,
                AdminRefBookListView.class, AdminRefBookListPresenter.MyProxy.class);

    }
}
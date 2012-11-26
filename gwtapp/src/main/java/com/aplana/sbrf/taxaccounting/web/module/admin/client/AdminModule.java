package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Тоже смутно понимаю зачем этот класс.
 * TODO: понять
 *
 * @author Vitalii Samolovskikh
 */
public class AdminModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(AdminPresenter.class, AdminPresenter.MyView.class, AdminView.class, AdminPresenter.MyProxy.class);
		bindPresenter(FormTemplatePresenter.class, FormTemplatePresenter.MyView.class, FormTemplateView.class, FormTemplatePresenter.MyProxy.class);
    }
}

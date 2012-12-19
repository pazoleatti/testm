package com.aplana.sbrf.taxaccounting.web.module.admin.client.gin;

import com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter.AdminPresenter;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.view.AdminView;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter.FormTemplatePresenter;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.view.FormTemplateView;
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

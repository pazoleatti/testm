package com.aplana.sbrf.taxaccounting.web.module.admin.client.gin;

import com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter.*;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.ui.UiModule;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.view.*;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Convenience method for binding a singleton presenter with its view and its
 * proxy, when using automatically generated proxy classes.
 *
 * @author Vitalii Samolovskikh
 */
public class AdminModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(AdminPresenter.class, AdminPresenter.MyView.class, AdminView.class, AdminPresenter.MyProxy.class);
		bindPresenter(FormTemplatePresenter.class, FormTemplatePresenter.MyView.class, FormTemplateView.class, FormTemplatePresenter.MyProxy.class);
		bindPresenter(FormTemplateMainPresenter.class, FormTemplateMainPresenter.MyView.class, FormTemplateMainView.class, FormTemplateMainPresenter.MyProxy.class);
		bindPresenter(FormTemplateScriptPresenter.class, FormTemplateScriptPresenter.MyView.class, FormTemplateScriptView.class, FormTemplateScriptPresenter.MyProxy.class);
		bindPresenter(FormTemplateEventPresenter.class, FormTemplateEventPresenter.MyView.class, FormTemplateEventView.class, FormTemplateEventPresenter.MyProxy.class);
		install(new UiModule());
    }

}

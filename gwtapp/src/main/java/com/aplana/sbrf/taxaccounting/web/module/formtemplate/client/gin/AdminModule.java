package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.gin;

import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.*;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.ui.UiModule;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view.*;
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
		bindPresenter(FormTemplateMainPresenter.class, FormTemplateMainPresenter.MyView.class, FormTemplateMainView.class, FormTemplateMainPresenter.MyProxy.class);
		bindPresenter(FormTemplateScriptCodePresenter.class, FormTemplateScriptCodePresenter.MyView.class, FormTemplateScriptCodeView.class, FormTemplateScriptCodePresenter.MyProxy.class);
		bindPresenter(FormTemplateColumnPresenter.class, FormTemplateColumnPresenter.MyView.class, FormTemplateColumnView.class, FormTemplateColumnPresenter.MyProxy.class);
		bindPresenter(FormTemplateRowPresenter.class, FormTemplateRowPresenter.MyView.class, FormTemplateRowView.class, FormTemplateRowPresenter.MyProxy.class);
		bindPresenter(FormTemplateInfoPresenter.class, FormTemplateInfoPresenter.MyView.class, FormTemplateInfoView.class, FormTemplateInfoPresenter.MyProxy.class);
		bindPresenter(FormTemplateStylePresenter.class, FormTemplateStylePresenter.MyView.class, FormTemplateStyleView.class, FormTemplateStylePresenter.MyProxy.class);
	    bindPresenter(FormTemplateHeaderPresenter.class, FormTemplateHeaderPresenter.MyView.class, FormTemplateHeaderView.class, FormTemplateHeaderPresenter.MyProxy.class);
		install(new UiModule());
    }

}

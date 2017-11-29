package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.editform.EditFormPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.editform.EditFormView;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event_script.EventScriptPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event_script.EventScriptView;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.filter.FilterDeclarationTemplatePresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.filter.FilterDeclarationTemplateView;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.ui.UiModule;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class DeclarationTemplateModule extends AbstractPresenterModule {
	@Override
	protected void configure() {
        bindPresenter(DeclarationTemplateInfoPresenter.class, DeclarationTemplateInfoPresenter.MyView.class,
                DeclarationTemplateInfoView.class, DeclarationTemplateInfoPresenter.MyProxy.class);
        bindPresenter(DeclarationTemplateScriptPresenter.class, DeclarationTemplateScriptPresenter.MyView.class,
                DeclarationTemplateScriptView.class, DeclarationTemplateScriptPresenter.MyProxy.class);
        bindPresenter(DeclarationTemplateSubreportPresenter.class, DeclarationTemplateSubreportPresenter.MyView.class,
                DeclarationTemplateSubreportView.class, DeclarationTemplateSubreportPresenter.MyProxy.class);
        bindPresenter(DeclarationTemplateChecksPresenter.class, DeclarationTemplateChecksPresenter.MyView.class,
                DeclarationTemplateChecksView.class, DeclarationTemplateChecksPresenter.MyProxy.class);
		bindPresenter(DeclarationTemplateListPresenter.class, DeclarationTemplateListPresenter.MyView.class,
				DeclarationTemplateListView.class, DeclarationTemplateListPresenter.MyProxy.class);
        bindPresenter(DeclarationTemplateFilesPresenter.class, DeclarationTemplateFilesPresenter.MyView.class,
                DeclarationTemplateFilesView.class, DeclarationTemplateFilesPresenter.MyProxy.class);
		bindPresenter(DeclarationTemplateMainPresenter.class, DeclarationTemplateMainPresenter.MyView.class,
                DeclarationTemplateMainView.class, DeclarationTemplateMainPresenter.MyProxy.class);
        bindPresenterWidget(FilterDeclarationTemplatePresenter.class, FilterDeclarationTemplatePresenter.MyView.class, FilterDeclarationTemplateView.class);
        bindSingletonPresenterWidget(EditFormPresenter.class, EditFormPresenter.MyView.class, EditFormView.class);
        bindSingletonPresenterWidget(EventScriptPresenter.class, EventScriptPresenter.MyView.class, EventScriptView.class);
        install(new UiModule());
    }

}
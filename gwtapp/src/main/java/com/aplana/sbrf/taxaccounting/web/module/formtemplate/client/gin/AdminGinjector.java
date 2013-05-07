package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.gin;

import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.*;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.ui.SimpleTabPanel;
import com.google.gwt.inject.client.AsyncProvider;

/**
 *This is required when using @ProxyCodeSplit.
 *
 * @author Vitalii Samolovskikh
 */
public interface AdminGinjector {
    AsyncProvider<AdminPresenter> getAdminPresenter();
	AsyncProvider<FormTemplateMainPresenter> getFormTemplateMainPresenter();
	AsyncProvider<FormTemplateScriptPresenter> getFormTemplateScriptPresenter();
	AsyncProvider<FormTemplateScriptCodePresenter> getFormTemplateScriptCodePresenter();
	AsyncProvider<FormTemplateEventPresenter> getFormTemplateEventPresenter();
	AsyncProvider<FormTemplateColumnPresenter> getFormTemplateColumnPresenter();
	AsyncProvider<FormTemplateRowPresenter> getFormTemplateRowPresenter();
	AsyncProvider<FormTemplateInfoPresenter> getFormTemplateInfoPresenter();
	AsyncProvider<FormTemplateStylePresenter> getFormTemplateStylePresenter();

	SimpleTabPanel getSimpleTabPanel();
}

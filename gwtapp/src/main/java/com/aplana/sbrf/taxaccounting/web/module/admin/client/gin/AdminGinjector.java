package com.aplana.sbrf.taxaccounting.web.module.admin.client.gin;

import com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter.*;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.ui.SimpleTabPanel;
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
	AsyncProvider<FormTemplateEventPresenter> getFormTemplateEventPresenter();
	AsyncProvider<FormTemplateColumnPresenter> getFormTemplateColumnPresenter();
	AsyncProvider<FormTemplateRowPresenter> getFormTemplateRowPresenter();

	SimpleTabPanel getSimpleTabPanel();
}

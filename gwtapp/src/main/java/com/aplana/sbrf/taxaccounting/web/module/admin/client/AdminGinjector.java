package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.google.gwt.inject.client.AsyncProvider;

/**
 * @author Vitalii Samolovskikh
 */
public interface AdminGinjector {
    AsyncProvider<AdminPresenter> getAdminPresenter();
	AsyncProvider<FormTemplatePresenter> getFormTemplatePresenter();
}

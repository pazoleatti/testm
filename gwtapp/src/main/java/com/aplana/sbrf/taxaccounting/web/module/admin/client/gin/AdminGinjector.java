package com.aplana.sbrf.taxaccounting.web.module.admin.client.gin;

import com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter.AdminPresenter;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter.FormTemplatePresenter;
import com.google.gwt.inject.client.AsyncProvider;

/**
 * Не знаю че за класс. Не представляю зачем он нужен, но без него не работает нифига.
 * TODO: понять и простить
 *
 * @author Vitalii Samolovskikh
 */
public interface AdminGinjector {
    AsyncProvider<AdminPresenter> getAdminPresenter();
	AsyncProvider<FormTemplatePresenter> getFormTemplatePresenter();
}

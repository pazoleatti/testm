package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.inject.client.GinModules;

/**
 * @author Vitalii Samolovskikh
 */
//@GinModules({AdminModule.class})
public interface AdminGinjector {

    AsyncProvider<AdminPresenter> getAdminPresenter();
}

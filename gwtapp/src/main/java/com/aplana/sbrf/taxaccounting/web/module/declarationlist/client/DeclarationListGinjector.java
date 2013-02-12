package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client;

import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.DeclarationFilterPresenter;
import com.google.gwt.inject.client.AsyncProvider;

public interface DeclarationListGinjector {

	AsyncProvider<DeclarationListPresenter> getDeclarationListPresenter();
	AsyncProvider<DeclarationFilterPresenter> getDeclarationFilterPresenter();

}

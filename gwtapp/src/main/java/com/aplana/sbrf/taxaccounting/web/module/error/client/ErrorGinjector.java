package com.aplana.sbrf.taxaccounting.web.module.error.client;

import com.aplana.sbrf.taxaccounting.web.module.error.client.ErrorPagePresenter;
import com.google.inject.Provider;


public interface ErrorGinjector {
    Provider<ErrorPagePresenter> getErrorPagePresenter();
}

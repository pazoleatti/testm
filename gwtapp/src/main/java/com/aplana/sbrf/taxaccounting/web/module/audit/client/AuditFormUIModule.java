package com.aplana.sbrf.taxaccounting.web.module.audit.client;

import com.aplana.sbrf.taxaccounting.web.module.audit.client.filter.AuditFilterPresenter;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.filter.AuditFilterView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * User: avanteev
 * Date: 2013
 */
public class AuditFormUIModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(AuditFormPresenter.class, AuditFormPresenter.MyView.class, AuditFormView.class, AuditFormPresenter.MyProxy.class);
        bindSingletonPresenterWidget(AuditFilterPresenter.class, AuditFilterPresenter.MyView.class, AuditFilterView.class);
    }
}

package com.aplana.sbrf.taxaccounting.web.module.audit.client;

import com.aplana.sbrf.taxaccounting.web.module.audit.client.archive.AuditArchiveDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.archive.AuditArchiveDialogView;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.filter.AuditFilterPresenter;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.filter.AuditFilterView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * User: avanteev
 * Date: 2013
 */
public class AuditClientUIModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(AuditClientPresenter.class, AuditClientPresenter.MyView.class, AuditClientView.class, AuditClientPresenter.MyProxy.class);
        bindSingletonPresenterWidget(AuditFilterPresenter.class, AuditFilterPresenter.MyView.class, AuditFilterView.class);
        bindSingletonPresenterWidget(AuditArchiveDialogPresenter.class, AuditArchiveDialogPresenter.MyView.class, AuditArchiveDialogView.class);
    }
}

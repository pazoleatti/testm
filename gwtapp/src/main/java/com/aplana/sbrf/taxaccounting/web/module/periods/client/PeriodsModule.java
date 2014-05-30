package com.aplana.sbrf.taxaccounting.web.module.periods.client;

import com.aplana.sbrf.taxaccounting.web.module.periods.client.deadlinedialog.DeadlineDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.deadlinedialog.DeadlineDialogView;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.editdialog.EditCorrectionDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.editdialog.EditCorrectionDialogView;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.editdialog.EditDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.editdialog.EditDialogView;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.opencorrectdialog.OpenCorrectDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.opencorrectdialog.OpenCorrectDialogView;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.opendialog.OpenDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.opendialog.OpenDialogView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class PeriodsModule extends AbstractPresenterModule {
	@Override
	protected void configure() {
		bindPresenter(PeriodsPresenter.class, PeriodsPresenter.MyView.class,
				PeriodsView.class, PeriodsPresenter.MyProxy.class);
		bindSingletonPresenterWidget(OpenDialogPresenter.class,
				OpenDialogPresenter.MyView.class, OpenDialogView.class);
        bindSingletonPresenterWidget(DeadlineDialogPresenter.class,
                DeadlineDialogPresenter.MyView.class, DeadlineDialogView.class);
        bindSingletonPresenterWidget(OpenCorrectDialogPresenter.class,
                OpenCorrectDialogPresenter.MyView.class, OpenCorrectDialogView.class);
        bindSingletonPresenterWidget(EditDialogPresenter.class,
                EditDialogPresenter.MyView.class, EditDialogView.class);
        bindSingletonPresenterWidget(EditCorrectionDialogPresenter.class,
                EditCorrectionDialogPresenter.MyView.class, EditCorrectionDialogView.class);

	}

}

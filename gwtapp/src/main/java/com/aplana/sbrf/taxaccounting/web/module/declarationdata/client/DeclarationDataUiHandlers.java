package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Date;

public interface DeclarationDataUiHandlers extends UiHandlers {
	void onRecalculateClicked(Date docDate, boolean force);
	void accept(boolean accepted, final boolean force);
	void delete();
	void check(boolean force);
    void viewReport(boolean force, ReportType reportType);
	void downloadXml();
	void onInfoClicked();
    TaxType getTaxType();
    void onTimerReport(final ReportType reportType, final boolean isTimer);
    void onOpenSourcesDialog();
    void revealPlaceRequest();
}

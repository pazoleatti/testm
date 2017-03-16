package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.DeclarationSubreport;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Date;

public interface DeclarationDataUiHandlers extends UiHandlers {
	void onRecalculateClicked(Date docDate, boolean force, boolean cancelTask);
	void accept(boolean accepted, boolean force, boolean cancelTask);
	void delete();
	void check(boolean force);
    void viewReport(boolean force, boolean create, DeclarationDataReportType type);
	void downloadXml();
	void onInfoClicked();
    TaxType getTaxType();
    void onTimerReport(final DeclarationDataReportType type, final boolean isTimer);
    void onOpenSourcesDialog();
    void revealPlaceRequest();
    void onTimerSubsreport(boolean isTimer);
    void onFilesCommentsDialog();
}

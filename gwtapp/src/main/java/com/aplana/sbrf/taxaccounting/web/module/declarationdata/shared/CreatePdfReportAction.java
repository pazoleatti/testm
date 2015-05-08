package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Действие добавления строки.
 *
 * @author lhaziev
 */
public class CreatePdfReportAction extends UnsecuredActionImpl<CreatePdfReportResult> implements ActionName {

    private long declarationDataId;
    private Boolean isForce;
    private boolean existPdf;

    public long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    public Boolean isForce() {
        return isForce;
    }

    public void setForce(Boolean isForce) {
        this.isForce = isForce;
    }

    public boolean isExistPdf() {
        return existPdf;
    }

    public void setExistPdf(boolean existPdf) {
        this.existPdf = existPdf;
    }

    @Override
	public String getName() {
		return "Создание задачи для генерации отчетов";
	}
}

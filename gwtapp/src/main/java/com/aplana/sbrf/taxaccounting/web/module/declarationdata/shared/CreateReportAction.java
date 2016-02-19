package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Запуск асинх задачи формирования отчетов
 *
 * @author lhaziev
 */
public class CreateReportAction extends UnsecuredActionImpl<CreateReportResult> implements ActionName {

    private long declarationDataId;
    private boolean isForce;
    private TaxType taxType;
    private String type;

    public long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    public boolean isForce() {
        return isForce;
    }

    public void setForce(boolean isForce) {
        this.isForce = isForce;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
	public String getName() {
		return "Формирование отчетов";
	}
}

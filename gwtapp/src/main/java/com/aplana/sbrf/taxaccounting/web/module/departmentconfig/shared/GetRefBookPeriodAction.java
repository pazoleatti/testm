package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;

/**
 * Получение даты окочания для указанных настроек
 * @author dloshkarev
 */
public class GetRefBookPeriodAction extends UnsecuredActionImpl<GetRefBookPeriodResult> implements ActionName {
    private Integer departmentId;
    private Integer reportPeriodId;
    private TaxType taxType;

    @Override
    public String getName() {
        return "Получение даты окочания для указанных настроек";
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setReportPeriodId(Integer reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }

    public Integer getReportPeriodId() {
        return reportPeriodId;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public TaxType getTaxType() {
        return taxType;
    }
}

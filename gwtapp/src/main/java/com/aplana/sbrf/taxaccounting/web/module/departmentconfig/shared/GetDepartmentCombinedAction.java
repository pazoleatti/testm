package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 *   Action формы настроек подразделений
 *   @author Dmitriy Levykin
 */
public class GetDepartmentCombinedAction extends UnsecuredActionImpl<GetDepartmentCombinedResult> implements ActionName {

    // Код выбранного подразделения
    private Integer departmentId;

    // Тип выбранного налогa
    private TaxType taxType;

    // Выбранный отчетный период
    private Integer reportPeriodId;

    private String oldUUID;

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public Integer getReportPeriodId() {
        return reportPeriodId;
    }

    public void setReportPeriodId(Integer reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }

    public String getOldUUID() {
        return oldUUID;
    }

    public void setOldUUID(String oldUUID) {
        this.oldUUID = oldUUID;
    }

    @Override
    public String getName() {
        return "Получение деталей подразделения";
    }
}

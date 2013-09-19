package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Загрузка бух отчетности на сервер и связь этой бух отчетности с подразделением и отчетным периодом
 * User: ekuvshinov
 */
public class ImportAction extends UnsecuredActionImpl<ImportResult> implements ActionName {
    private Integer reportPeriodId;
    private Integer departmentId;
    private int typeId; //0 - Оборотная ведомость по счетам бухгалтерского учёта кредитной организации (Ф-101); 1 - Ф-102.
    private String uuid;

    @Override
    public String getName() {
        return "Загрузка бух отчетности";
    }

    public Integer getReportPeriodId() {
        return reportPeriodId;
    }

    public void setReportPeriodId(Integer reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}

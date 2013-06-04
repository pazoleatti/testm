package com.aplana.sbrf.taxaccounting.web.module.audit.shared;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;

/**
 * User: avanteev
 */
public class GetAuditFilterDataListAction implements ActionName {
    private Integer reportPeriodIds;

    private Integer departmentId;

    /*Пример: Сведения о транспортных средствах, Расчет суммы налога*/
    private Integer formTypeId;

    /*Пример: Первичная, консалидированная, сводная, сводная банка*/
    private FormDataKind formDataKind;

    /*Пример: Наоговые формы, Транспортные*/
    private TaxType formDataTaxType;

    /**
     * Логин пользователя, информацию о котором хотим посмотреть
     */
    private String userLogin;

    public Integer getReportPeriodIds() {
        return reportPeriodIds;
    }

    public void setReportPeriodIds(Integer reportPeriodIds) {
        this.reportPeriodIds = reportPeriodIds;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getFormTypeId() {
        return formTypeId;
    }

    public void setFormTypeId(Integer formTypeId) {
        this.formTypeId = formTypeId;
    }

    public FormDataKind getFormDataKind() {
        return formDataKind;
    }

    public void setFormDataKind(FormDataKind formDataKind) {
        this.formDataKind = formDataKind;
    }

    public TaxType getFormDataTaxType() {
        return formDataTaxType;
    }

    public void setFormDataTaxType(TaxType formDataTaxType) {
        this.formDataTaxType = formDataTaxType;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    @Override
    public String getName() {
        return "Получение списка журнала аудита";
    }
}

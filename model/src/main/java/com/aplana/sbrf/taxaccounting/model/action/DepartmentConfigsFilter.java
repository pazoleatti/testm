package com.aplana.sbrf.taxaccounting.model.action;

import java.io.Serializable;
import java.util.Date;

/**
 * Фильтр для поиска/отбора настроек подразделений
 */
public class DepartmentConfigsFilter implements Serializable {
    private Integer departmentId;
    private Date relevanceDate;
    private String kpp;
    private String oktmo;
    private String taxOrganCode;

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Date getRelevanceDate() {
        return relevanceDate;
    }

    public void setRelevanceDate(Date relevanceDate) {
        this.relevanceDate = relevanceDate;
    }

    public String getKpp() {
        return kpp;
    }

    public void setKpp(String kpp) {
        this.kpp = kpp;
    }

    public String getOktmo() {
        return oktmo;
    }

    public void setOktmo(String oktmo) {
        this.oktmo = oktmo;
    }

    public String getTaxOrganCode() {
        return taxOrganCode;
    }

    public void setTaxOrganCode(String taxOrganCode) {
        this.taxOrganCode = taxOrganCode;
    }
}

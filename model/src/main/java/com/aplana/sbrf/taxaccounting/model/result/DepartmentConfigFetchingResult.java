package com.aplana.sbrf.taxaccounting.model.result;

import java.util.Date;

/**
 * Класс инкапсулирует данные настроек подразделений передаваемые на представление
 */
public class DepartmentConfigFetchingResult {
    private Date departmentConfigStartDate;
    private Date departmentConfigEndDate;

    private Integer rowOrd;
    private Integer departmentId;
    private String taxOrganCode;
    private String kpp;
    private String oktmo;
    private String presentPlace;
    private String name;
    private String phone;
    private String reorganization;
    private String reorgInn;
    private String reorgKpp;
    private Integer signatoryId;
    private String signatorySurName;
    private String signatoryFirstName;
    private String signatoryLastName;
    private String approveDocName;
    private String approveOrgName;
    private Date modifiedDate;
    private String modifiedBy;

    public Date getDepartmentConfigStartDate() {
        return departmentConfigStartDate;
    }

    public void setDepartmentConfigStartDate(Date departmentConfigStartDate) {
        this.departmentConfigStartDate = departmentConfigStartDate;
    }

    public Date getDepartmentConfigEndDate() {
        return departmentConfigEndDate;
    }

    public void setDepartmentConfigEndDate(Date departmentConfigEndDate) {
        this.departmentConfigEndDate = departmentConfigEndDate;
    }

    public Integer getRowOrd() {
        return rowOrd;
    }

    public void setRowOrd(Integer rowOrd) {
        this.rowOrd = rowOrd;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public String getTaxOrganCode() {
        return taxOrganCode;
    }

    public void setTaxOrganCode(String taxOrganCode) {
        this.taxOrganCode = taxOrganCode;
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

    public String getPresentPlace() {
        return presentPlace;
    }

    public void setPresentPlace(String presentPlace) {
        this.presentPlace = presentPlace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getReorganization() {
        return reorganization;
    }

    public void setReorganization(String reorganization) {
        this.reorganization = reorganization;
    }

    public String getReorgInn() {
        return reorgInn;
    }

    public void setReorgInn(String reorgInn) {
        this.reorgInn = reorgInn;
    }

    public String getReorgKpp() {
        return reorgKpp;
    }

    public void setReorgKpp(String reorgKpp) {
        this.reorgKpp = reorgKpp;
    }

    public Integer getSignatoryId() {
        return signatoryId;
    }

    public void setSignatoryId(Integer signatoryId) {
        this.signatoryId = signatoryId;
    }

    public String getSignatorySurName() {
        return signatorySurName;
    }

    public void setSignatorySurName(String signatorySurName) {
        this.signatorySurName = signatorySurName;
    }

    public String getSignatoryFirstName() {
        return signatoryFirstName;
    }

    public void setSignatoryFirstName(String signatoryFirstName) {
        this.signatoryFirstName = signatoryFirstName;
    }

    public String getSignatoryLastName() {
        return signatoryLastName;
    }

    public void setSignatoryLastName(String signatoryLastName) {
        this.signatoryLastName = signatoryLastName;
    }

    public String getApproveDocName() {
        return approveDocName;
    }

    public void setApproveDocName(String approveDocName) {
        this.approveDocName = approveDocName;
    }

    public String getApproveOrgName() {
        return approveOrgName;
    }

    public void setApproveOrgName(String approveOrgName) {
        this.approveOrgName = approveOrgName;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
}

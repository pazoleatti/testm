package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.SecuredEntity;

import java.util.Date;

/**
 * Запись настройки подразделений
 */
public class DepartmentConfig extends RefBookSimple<Long> implements SecuredEntity {
    private Date startDate;
    private Date endDate;
    private Long recordId;

    private Integer rowOrd;
    private RefBookDepartment department;
    private String kpp;
    private RefBookOktmo oktmo;
    private String taxOrganCode;
    private RefBookPresentPlace presentPlace;
    private String name;
    private String phone;
    private RefBookSignatoryMark signatoryMark;
    private String signatorySurName;
    private String signatoryFirstName;
    private String signatoryLastName;
    private String approveDocName;
    private String approveOrgName;
    private RefBookReorganization reorganization;
    private String reorgKpp;
    private String reorgInn;
    private long permissions;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Integer getRowOrd() {
        return rowOrd;
    }

    public void setRowOrd(Integer rowOrd) {
        this.rowOrd = rowOrd;
    }

    public RefBookDepartment getDepartment() {
        return department;
    }

    public void setDepartment(RefBookDepartment department) {
        this.department = department;
    }

    public String getKpp() {
        return kpp;
    }

    public void setKpp(String kpp) {
        this.kpp = kpp;
    }

    public RefBookOktmo getOktmo() {
        return oktmo;
    }

    public void setOktmo(RefBookOktmo oktmo) {
        this.oktmo = oktmo;
    }

    public String getTaxOrganCode() {
        return taxOrganCode;
    }

    public void setTaxOrganCode(String taxOrganCode) {
        this.taxOrganCode = taxOrganCode;
    }

    public RefBookPresentPlace getPresentPlace() {
        return presentPlace;
    }

    public void setPresentPlace(RefBookPresentPlace presentPlace) {
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

    public RefBookSignatoryMark getSignatoryMark() {
        return signatoryMark;
    }

    public void setSignatoryMark(RefBookSignatoryMark signatoryMark) {
        this.signatoryMark = signatoryMark;
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

    public RefBookReorganization getReorganization() {
        return reorganization;
    }

    public void setReorganization(RefBookReorganization reorganization) {
        this.reorganization = reorganization;
    }

    public String getReorgKpp() {
        return reorgKpp;
    }

    public void setReorgKpp(String reorgKpp) {
        this.reorgKpp = reorgKpp;
    }

    public String getReorgInn() {
        return reorgInn;
    }

    public void setReorgInn(String reorgInn) {
        this.reorgInn = reorgInn;
    }

    @Override
    public long getPermissions() {
        return permissions;
    }

    @Override
    public void setPermissions(long permissions) {
        this.permissions = permissions;
    }
}

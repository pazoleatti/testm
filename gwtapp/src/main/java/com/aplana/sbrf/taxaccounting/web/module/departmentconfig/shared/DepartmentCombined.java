package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;

import java.io.Serializable;

/**
 * Составная модель для параметров подразделений.<br>
 * Параметры могут быть для:
 * <li>- транспортного налога
 * <li>- налога на прибыль
 * <li>- учета контролируемых сделок
 *
 * @author Dmitriy Levykin
 */
public class DepartmentCombined implements Serializable {

    private Long recordId;

    // Общие параметры
    private Long departmentId;
    private Long dictRegionId;
    private Long okato;
    private String inn;
    private String kpp;
    private String taxOrganCode;
    private Long okvedCode;
    private String phone;
    private Long reorgFormCode;
    private String reorgInn;
    private String reorgKpp;
    private String name;

    // Во всех налогах
    private Long signatoryId;
    private String signatorySurname;
    private String signatoryFirstname;
    private String signatoryLastname;
    private String approveDocName;
    private String approveOrgName;
    private Long taxPlaceTypeCode;
    private String appVersion;
    private String formatVersion;

    // В налоге на прибыль и УКС
    private Long sumTax;
    private Long sumDividends;

    // Налог на прибыль
    private Long obligation;
    private Double taxRate;
    private Long type;

    // Тип налога
    private TaxType taxType;

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public DepartmentCombined() {
        super();
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getDictRegionId() {
        return dictRegionId;
    }

    public void setDictRegionId(Long dictRegionId) {
        this.dictRegionId = dictRegionId;
    }

    public Long getOkato() {
        return okato;
    }

    public void setOkato(Long okato) {
        this.okato = okato;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getKpp() {
        return kpp;
    }

    public void setKpp(String kpp) {
        this.kpp = kpp;
    }

    public String getTaxOrganCode() {
        return taxOrganCode;
    }

    public void setTaxOrganCode(String taxOrganCode) {
        this.taxOrganCode = taxOrganCode;
    }

    public Long getOkvedCode() {
        return okvedCode;
    }

    public void setOkvedCode(Long okvedCode) {
        this.okvedCode = okvedCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getReorgFormCode() {
        return reorgFormCode;
    }

    public void setReorgFormCode(Long reorgFormCode) {
        this.reorgFormCode = reorgFormCode;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSignatoryId() {
        return signatoryId;
    }

    public void setSignatoryId(Long signatoryId) {
        this.signatoryId = signatoryId;
    }

    public String getSignatorySurname() {
        return signatorySurname;
    }

    public void setSignatorySurname(String signatorySurname) {
        this.signatorySurname = signatorySurname;
    }

    public String getSignatoryFirstname() {
        return signatoryFirstname;
    }

    public void setSignatoryFirstname(String signatoryFirstname) {
        this.signatoryFirstname = signatoryFirstname;
    }

    public String getSignatoryLastname() {
        return signatoryLastname;
    }

    public void setSignatoryLastname(String signatoryLastname) {
        this.signatoryLastname = signatoryLastname;
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

    public Long getTaxPlaceTypeCode() {
        return taxPlaceTypeCode;
    }

    public void setTaxPlaceTypeCode(Long taxPlaceTypeCode) {
        this.taxPlaceTypeCode = taxPlaceTypeCode;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getFormatVersion() {
        return formatVersion;
    }

    public void setFormatVersion(String formatVersion) {
        this.formatVersion = formatVersion;
    }

    public Double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(Double taxRate) {
        this.taxRate = taxRate;
    }

    public Long getObligation() {
        return obligation;
    }

    public void setObligation(Long obligation) {
        this.obligation = obligation;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public Long getSumTax() {
        return sumTax;
    }

    public void setSumTax(Long sumTax) {
        this.sumTax = sumTax;
    }

    public Long getSumDividends() {
        return sumDividends;
    }

    public void setSumDividends(Long sumDividends) {
        this.sumDividends = sumDividends;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }
}

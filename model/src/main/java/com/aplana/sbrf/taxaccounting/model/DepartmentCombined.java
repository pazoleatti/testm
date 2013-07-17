package com.aplana.sbrf.taxaccounting.model;

/**
 * Составная модель подразделения,
 * включающая в себя общие и частные параметры
 *
 * @author Dmitriy Levykin
 */
public class DepartmentCombined extends Department {

    // Общие параметры
    private DepartmentParam commonParams;
    // Параметры по налогу на прибыль
    private DepartmentParamIncome incomeParams;
    // Параметры по транспортному налогу
    private DepartmentParamTransport transportParams;

    public DepartmentCombined() {
        super();
    }

    public DepartmentCombined(Department dep,
                              DepartmentParam commonParams,
                              DepartmentParamIncome incomeParams,
                              DepartmentParamTransport transportParams) {
        super();

        super.setDepartmentDeclarationTypes(dep.getDepartmentDeclarationTypes());
        super.setDepartmentFormTypes(dep.getDepartmentFormTypes());
        super.setDictRegionId(dep.getDictRegionId());
        super.setId(dep.getId());
        super.setName(dep.getName());
        super.setParentId(dep.getParentId());
        super.setSbrfCode(dep.getSbrfCode());
        super.setShortName(dep.getShortName());
        super.setTbIndex(dep.getTbIndex());
        super.setType(dep.getType());

        this.commonParams = commonParams;
        this.incomeParams = incomeParams;
        this.transportParams = transportParams;
    }

    public DepartmentParam getCommonParams() {
        return commonParams;
    }

    public DepartmentParamIncome getIncomeParams() {
        return incomeParams;
    }

    public DepartmentParamTransport getTransportParams() {
        return transportParams;
    }

    // Атрибуты DepartmentParam

    public String getCommonDictRegionId() {
        return commonParams.getDictRegionId();
    }

    public void setCommonDictRegionId(String dictRegionId) {
        commonParams.setDictRegionId(dictRegionId);
    }

    public String getCommonOkato() {
        return commonParams.getOkato();
    }

    public void setCommonOkato(String okato) {
        commonParams.setOkato(okato);
    }

    public String getCommonInn() {
        return commonParams.getInn();
    }

    public void setCommonInn(String inn) {
        commonParams.setInn(inn);
    }

    public String getCommonKpp() {
        return commonParams.getKpp();
    }

    public void setCommonKpp(String kpp) {
        commonParams.setKpp(kpp);
    }

    public String getCommonTaxOrganCode() {
        return commonParams.getTaxOrganCode();
    }

    public void setCommonTaxOrganCode(String taxOrganCode) {
        commonParams.setTaxOrganCode(taxOrganCode);
    }

    public String getCommonOkvedCode() {
        return commonParams.getOkvedCode();
    }

    public void setCommonOkvedCode(String okvedCode) {
        commonParams.setOkvedCode(okvedCode);
    }

    public String getCommonPhone() {
        return commonParams.getPhone();
    }

    public void setCommonPhone(String phone) {
        commonParams.setPhone(phone);
    }

    public String getCommonReorgFormCode() {
        return commonParams.getReorgFormCode();
    }

    public void setCommonReorgFormCode(String reorgFormCode) {
        commonParams.setReorgFormCode(reorgFormCode);
    }

    public String getCommonReorgInn() {
        return commonParams.getReorgInn();
    }

    public void setCommonReorgInn(String reorgInn) {
        commonParams.setReorgInn(reorgInn);
    }

    public String getCommonReorgKpp() {
        return commonParams.getReorgKpp();
    }

    public void setCommonReorgKpp(String reorgKpp) {
        commonParams.setReorgKpp(reorgKpp);
    }

    public String getCommonName() {
        return commonParams.getName();
    }

    public void setCommonName(String name) {
        commonParams.setName(name);
    }

    // Атрибуты DepartmentParamIncome

    public int getIncomeSignatoryId() {
        return incomeParams.getSignatoryId();
    }

    public void setIncomeSignatoryId(int signatoryId) {
        incomeParams.setSignatoryId(signatoryId);
    }

    public String getIncomeSignatorySurname() {
        return incomeParams.getSignatorySurname();
    }

    public void setIncomeSignatorySurname(String signatorySurname) {
        incomeParams.setSignatorySurname(signatorySurname);
    }

    public String getIncomeSignatoryFirstName() {
        return incomeParams.getSignatoryFirstName();
    }

    public void setIncomeSignatoryFirstName(String signatoryFirstName) {
        incomeParams.setSignatoryFirstName(signatoryFirstName);
    }

    public String getIncomeSignatoryLastName() {
        return incomeParams.getSignatoryLastName();
    }

    public void setIncomeSignatoryLastName(String signatoryLastName) {
        incomeParams.setSignatoryLastName(signatoryLastName);
    }

    public String getIncomeApproveDocName() {
        return incomeParams.getApproveDocName();
    }

    public void setIncomeApproveDocName(String approveDocName) {
        incomeParams.setApproveDocName(approveDocName);
    }

    public String getIncomeApproveOrgName() {
        return incomeParams.getApproveOrgName();
    }

    public void setIncomeApproveOrgName(String approveOrgName) {
        incomeParams.setApproveOrgName(approveOrgName);
    }

    public String getIncomeTaxPlaceTypeCode() {
        return incomeParams.getTaxPlaceTypeCode();
    }

    public void setIncomeTaxPlaceTypeCode(String taxPlaceTypeCode) {
        incomeParams.setTaxPlaceTypeCode(taxPlaceTypeCode);
    }

    public Double getIncomeTaxRate() {
        return incomeParams.getTaxRate();
    }

    public void setIncomeTaxRate(Double taxRate) {
        incomeParams.setTaxRate(taxRate);
    }

    public Long getIncomeExternalTaxSum() {
        return incomeParams.getExternalTaxSum();
    }

    public void setIncomeExternalTaxSum(Long externalTaxSum) {
        incomeParams.setExternalTaxSum(externalTaxSum);
    }

    public Long getIncomeSumDifference() {
        return incomeParams.getSumDifference();
    }

    public void setIncomeSumDifference(Long sumDifference) {
        incomeParams.setSumDifference(sumDifference);
    }

    public Long getIncomeCorrectionSum() {
        return incomeParams.getCorrectionSum();
    }

    public void setIncomeCorrectionSum(Long correctionSum) {
        incomeParams.setCorrectionSum(correctionSum);
    }

    public String getIncomeAppVersion() {
        return incomeParams.getAppVersion();
    }

    public void setIncomeAppVersion(String appVersion) {
        incomeParams.setAppVersion(appVersion);
    }

    public String getIncomeFormatVersion() {
        return incomeParams.getFormatVersion();
    }

    public void setIncomeFormatVersion(String formatVersion) {
        incomeParams.setFormatVersion(formatVersion);
    }

    // Атрибуты DepartmentParamTransport

    public int getTransportSignatoryId() {
        return transportParams.getSignatoryId();
    }

    public void setTransportSignatoryId(int signatoryId) {
        transportParams.setSignatoryId(signatoryId);
    }

    public String getTransportSignatorySurname() {
        return transportParams.getSignatorySurname();
    }

    public void setTransportSignatorySurname(String signatorySurname) {
        transportParams.setSignatorySurname(signatorySurname);
    }

    public String getTransportSignatoryFirstname() {
        return transportParams.getSignatoryFirstname();
    }

    public void setTransportSignatoryFirstname(String signatoryFirstname) {
        transportParams.setSignatoryFirstname(signatoryFirstname);
    }

    public String getTransportSignatoryLastname() {
        return transportParams.getSignatoryLasttname();
    }

    public void setTransportSignatoryLastname(String signatoryLasttname) {
        transportParams.setSignatoryLasttname(signatoryLasttname);
    }

    public String getTransportApproveDocName() {
        return transportParams.getApproveDocName();
    }

    public void setTransportApproveDocName(String approveDocName) {
        transportParams.setApproveDocName(approveDocName);
    }

    public String getTransportApproveOrgName() {
        return transportParams.getApproveOrgName();
    }

    public void setTransportApproveOrgName(String approveOrgName) {
        transportParams.setApproveOrgName(approveOrgName);
    }

    public String getTransportTaxPlaceTypeCode() {
        return transportParams.getTaxPlaceTypeCode();
    }

    public void setTransportTaxPlaceTypeCode(String taxPlaceTypeCode) {
        transportParams.setTaxPlaceTypeCode(taxPlaceTypeCode);
    }

    public String getTransportAppVersion() {
        return transportParams.getAppVersion();
    }

    public void setTransportAppVersion(String appVersion) {
        transportParams.setAppVersion(appVersion);
    }

    public String getTransportFormatVersion() {
        return transportParams.getFormatVersion();
    }

    public void setTransportFormatVersion(String formatVersion) {
        transportParams.setFormatVersion(formatVersion);
    }
}

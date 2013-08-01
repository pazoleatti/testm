package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Параметры подразделения по налогу на прибыль
 */
@Deprecated
public class DepartmentParamIncome implements Serializable {
	private static final long serialVersionUID = 1L;

	private int departmentId;
	private int signatoryId;
	private String signatorySurname;
	private String signatoryFirstName;
	private String signatoryLastName;
	private String approveDocName;
	private String approveOrgName;
	private String taxPlaceTypeCode;
	private Double taxRate;
	private Long externalTaxSum;
	private Long sumDifference;
	private Long correctionSum;
	private String appVersion;
	private String formatVersion;

	public DepartmentParamIncome() {
	}

	public int getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(int departmentId) {
		this.departmentId = departmentId;
	}

	public int getSignatoryId() {
		return signatoryId;
	}

	public void setSignatoryId(int signatoryId) {
		this.signatoryId = signatoryId;
	}

	public String getSignatorySurname() {
		return signatorySurname;
	}

	public void setSignatorySurname(String signatorySurname) {
		this.signatorySurname = signatorySurname;
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

	public String getTaxPlaceTypeCode() {
		return taxPlaceTypeCode;
	}

	public void setTaxPlaceTypeCode(String taxPlaceTypeCode) {
		this.taxPlaceTypeCode = taxPlaceTypeCode;
	}

	public Double getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(Double taxRate) {
		this.taxRate = taxRate;
	}

	public Long getExternalTaxSum() {
		return externalTaxSum;
	}

	public void setExternalTaxSum(Long externalTaxSum) {
		this.externalTaxSum = externalTaxSum;
	}

	public Long getSumDifference() {
		return sumDifference;
	}

	public void setSumDifference(Long sumDifference) {
		this.sumDifference = sumDifference;
	}

	public Long getCorrectionSum() {
		return correctionSum;
	}

	public void setCorrectionSum(Long correctionSum) {
		this.correctionSum = correctionSum;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DepartmentParamIncome that = (DepartmentParamIncome) o;

        if (departmentId != that.departmentId) return false;
        if (signatoryId != that.signatoryId) return false;
        if (appVersion != null ? !appVersion.equals(that.appVersion) : that.appVersion != null) return false;
        if (approveDocName != null ? !approveDocName.equals(that.approveDocName) : that.approveDocName != null)
            return false;
        if (approveOrgName != null ? !approveOrgName.equals(that.approveOrgName) : that.approveOrgName != null)
            return false;
        if (correctionSum != null ? !correctionSum.equals(that.correctionSum) : that.correctionSum != null)
            return false;
        if (externalTaxSum != null ? !externalTaxSum.equals(that.externalTaxSum) : that.externalTaxSum != null)
            return false;
        if (formatVersion != null ? !formatVersion.equals(that.formatVersion) : that.formatVersion != null)
            return false;
        if (signatoryFirstName != null ? !signatoryFirstName.equals(that.signatoryFirstName) : that.signatoryFirstName != null)
            return false;
        if (signatoryLastName != null ? !signatoryLastName.equals(that.signatoryLastName) : that.signatoryLastName != null)
            return false;
        if (signatorySurname != null ? !signatorySurname.equals(that.signatorySurname) : that.signatorySurname != null)
            return false;
        if (sumDifference != null ? !sumDifference.equals(that.sumDifference) : that.sumDifference != null)
            return false;
        if (taxPlaceTypeCode != null ? !taxPlaceTypeCode.equals(that.taxPlaceTypeCode) : that.taxPlaceTypeCode != null)
            return false;
        if (taxRate != null ? !taxRate.equals(that.taxRate) : that.taxRate != null) return false;

        return true;
    }

	@Override
	public int hashCode() {
		return departmentId; //TODO: затем добавить сюда отчетный период. 31 * departmentId + reportPeriodId (Marat Fayzullin 2013-07-29)
	}

}

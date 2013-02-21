package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Параметры подразделения по налогу на прибыль
 */
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
	private Long taxRate;
	private Long externalTaxSum;
	private Long sumDifference;
	private Long correctionSum;

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

	public Long getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(Long taxRate) {
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
}

package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Параметры подразделения по налогу на прибыль
 */
public class DepartmentParamIncome implements Serializable {
	private static final long serialVersionUID = 1L;

	private int departmentId;
	private char signatoryId;
	private String signatorySurname;
	private String signatoryFirstName;
	private String signatoryLastName;
	private String approveDocName;
	private String approveOrgName;
	private String taxPlaceTypeCode;
	private float taxRate;
	private long externalTaxSum;
	private long sumDifference;
	private long correctionSum;

	public DepartmentParamIncome() {
	}

	public int getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(int departmentId) {
		this.departmentId = departmentId;
	}

	public char getSignatoryId() {
		return signatoryId;
	}

	public void setSignatoryId(char signatoryId) {
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

	public float getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(float taxRate) {
		this.taxRate = taxRate;
	}

	public long getExternalTaxSum() {
		return externalTaxSum;
	}

	public void setExternalTaxSum(long externalTaxSum) {
		this.externalTaxSum = externalTaxSum;
	}

	public long getSumDifference() {
		return sumDifference;
	}

	public void setSumDifference(long sumDifference) {
		this.sumDifference = sumDifference;
	}

	public long getCorrectionSum() {
		return correctionSum;
	}

	public void setCorrectionSum(long correctionSum) {
		this.correctionSum = correctionSum;
	}
}

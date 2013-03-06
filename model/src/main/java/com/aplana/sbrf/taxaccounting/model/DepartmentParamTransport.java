package com.aplana.sbrf.taxaccounting.model;

public class DepartmentParamTransport {
	
	private int departmentId;
	private int signatoryId;
	private String signatorySurname;
	private String signatoryFirstname;
	private String signatoryLasttname;
	private String approveDocName;
	private String approveOrgName;
	private String taxPlaceTypeCode;
	private String appVersion;
	private String formatVersion;
	
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
	public String getSignatoryFirstname() {
		return signatoryFirstname;
	}
	public void setSignatoryFirstname(String signatoryFirstname) {
		this.signatoryFirstname = signatoryFirstname;
	}
	public String getSignatoryLasttname() {
		return signatoryLasttname;
	}
	public void setSignatoryLasttname(String signatoryLasttname) {
		this.signatoryLasttname = signatoryLasttname;
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

}

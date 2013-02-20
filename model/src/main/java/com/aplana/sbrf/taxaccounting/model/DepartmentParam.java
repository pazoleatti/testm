package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * общие сведения подразделения
 */
public class DepartmentParam implements Serializable {
	private static final long serialVersionUID = 1L;

	private int departmentId;
	private String dictRegionId;
	private String okato;
	private String inn;
	private String kpp;
	private String taxOrganCode;
	private String okvedCode;
	private String phone;
	private String reorgFormCode;
	private String reorgInn;
	private String reorgKpp;

	public DepartmentParam() {
	}

	public int getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(int departmentId) {
		this.departmentId = departmentId;
	}

	public String getDictRegionId() {
		return dictRegionId;
	}

	public void setDictRegionId(String dictRegionId) {
		this.dictRegionId = dictRegionId;
	}

	public String getOkato() {
		return okato;
	}

	public void setOkato(String okato) {
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

	public String getOkvedCode() {
		return okvedCode;
	}

	public void setOkvedCode(String okvedCode) {
		this.okvedCode = okvedCode;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getReorgFormCode() {
		return reorgFormCode;
	}

	public void setReorgFormCode(String reorgFormCode) {
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
}

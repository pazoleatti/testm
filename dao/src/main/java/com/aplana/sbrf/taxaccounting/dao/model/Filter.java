package com.aplana.sbrf.taxaccounting.dao.model;


import java.io.Serializable;
import java.util.List;

public class Filter implements Serializable {

	private static final long serialVersionUID = -961619115678926848L;

	private List<Long> period;

	private List<Long> department;

	private List<Long> formtype;

	private List<Long> kind;

	public List<Long> getPeriod() {
		return period;
	}

	public void setPeriod(List<Long> period) {
		this.period = period;
	}

	public List<Long> getDepartment() {
		return department;
	}

	public void setDepartment(List<Long> department) {
		this.department = department;
	}

	public List<Long> getFormtype() {
		return formtype;
	}

	public void setFormtype(List<Long> formtype) {
		this.formtype = formtype;
	}

	public List<Long> getKind() {
		return kind;
	}

	public void setKind(List<Long> kind) {
		this.kind = kind;
	}
}

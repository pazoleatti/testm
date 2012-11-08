package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.filter;

import java.io.Serializable;
import java.util.List;

/**
 * @author sgoryachkin
 *
 */
public class FilterData implements Serializable{
	private static final long serialVersionUID = -4400641241082281834L;

	private List<Long> period;

	private List<Long> department;

	private List<Long> formtype;

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

}

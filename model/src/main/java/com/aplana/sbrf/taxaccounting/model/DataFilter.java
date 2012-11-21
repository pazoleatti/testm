package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * @author sgoryachkin
 *
 */
public class DataFilter implements Serializable{
	private static final long serialVersionUID = -4400641241082281834L;

	private Long period;

	private Long department;

	private Long formtype;

    private Long kind;

	public Long getPeriod() {
		return period;
	}

	public void setPeriod(Long period) {
		this.period = period;
	}

	public Long getDepartment() {
		return department;
	}

	public void setDepartment(Long department) {
		this.department = department;
	}

	public Long getFormtype() {
		return formtype;
	}

	public void setFormtype(Long formtype) {
		this.formtype = formtype;
	}

    public Long getKind() {
        return kind;
    }

    public void setKind(Long kind) {
        this.kind = kind;
    }

}

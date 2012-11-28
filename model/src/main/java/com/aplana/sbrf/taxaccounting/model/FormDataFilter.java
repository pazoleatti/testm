package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * @author sgoryachkin
 *
 */
public class FormDataFilter implements Serializable{
	private static final long serialVersionUID = -4400641241082281834L;

	private Long period;

	private Long department;

	/*Пример: Первичная, консалидированная, сводная, сводная банка*/
	private Long formtype;

	/*Пример: Сведения о транспортных средствах, Расчет суммы налога, DEMO*/
    private Long kind;

	private TaxType taxType;

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

	public TaxType getTaxType() {
		return taxType;
	}

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}

}

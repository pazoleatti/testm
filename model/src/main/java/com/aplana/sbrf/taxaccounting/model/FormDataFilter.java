package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * @author sgoryachkin
 *
 */
public class FormDataFilter implements Serializable{
	private static final long serialVersionUID = -4400641241082281834L;

	private Long reportPeriodId;

	private Long departmentId;

	/*Пример: Сведения о транспортных средствах, Расчет суммы налога, DEMO*/
	private Long formTypeId;

	/*Пример: Первичная, консалидированная, сводная, сводная банка*/
    private Long formDataKind;

	private TaxType taxType;

	public Long getPeriod() {
		return reportPeriodId;
	}

	public void setPeriod(Long period) {
		this.reportPeriodId = period;
	}

	public Long getDepartment() {
		return departmentId;
	}

	public void setDepartment(Long department) {
		this.departmentId = department;
	}

	public Long getFormtype() {
		return formTypeId;
	}

	public void setFormtype(Long formtype) {
		this.formTypeId = formtype;
	}

    public Long getKind() {
        return formDataKind;
    }

    public void setKind(Long kind) {
        this.formDataKind = kind;
    }

	public TaxType getTaxType() {
		return taxType;
	}

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}

}

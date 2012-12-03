package com.aplana.sbrf.taxaccounting.dao.model;


import java.io.Serializable;
import java.util.List;

/**
	Данный класс используется сервисом DataHandlerServiceImpl.
	DataHandlerServiceImpl получает FormDataFilter с пользовательской стороны и преобразует его в FormDataDaoFilter,
	который дальше идет на DAO слой.
 */
public class FormDataDaoFilter implements Serializable {

	private static final long serialVersionUID = -961619115678926848L;

	private List<Long> reportPeriodId;

	private List<Long> departmentId;

	/*Пример: Сведения о транспортных средствах, Расчет суммы налога, DEMO*/
	private List<Long> formTypeId;

	/*Пример: Первичная, консалидированная, сводная, сводная банка*/
	private List<Long> formDataKind;

	private List<Long> formStates;

	public List<Long> getPeriod() {
		return reportPeriodId;
	}

	public void setPeriod(List<Long> period) {
		this.reportPeriodId = period;
	}

	public List<Long> getDepartment() {
		return departmentId;
	}

	public void setDepartment(List<Long> department) {
		this.departmentId = department;
	}

	public List<Long> getFormtype() {
		return formTypeId;
	}

	public void setFormtype(List<Long> formtype) {
		this.formTypeId = formtype;
	}

	public List<Long> getKind() {
		return formDataKind;
	}

	public void setKind(List<Long> kind) {
		this.formDataKind = kind;
	}

	public List<Long> getFormStates() {
		return formStates;
	}

	public void setFormStates(List<Long> formStates) {
		this.formStates = formStates;
	}
}

package com.aplana.sbrf.taxaccounting.model;


import java.io.Serializable;
import java.util.List;

/**
	Объект, представляющий условие фильтрации списка данных по налоговым формам.
 */
public class FormDataDaoFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Integer> reportPeriodIds;

	private List<Integer> departmentIds;

	private List<Integer> formTypeIds;

	private List<FormDataKind> formDataKinds;

	private List<WorkflowState> states;

	private List<TaxType> taxTypes;

	public List<Integer> getReportPeriodIds() {
		return reportPeriodIds;
	}

	public void setReportPeriodIds(List<Integer> reportPeriodIds) {
		this.reportPeriodIds = reportPeriodIds;
	}

	public List<Integer> getDepartmentIds() {
		return departmentIds;
	}

	public void setDepartmentIds(List<Integer> departmentIds) {
		this.departmentIds = departmentIds;
	}

	public List<Integer> getFormTypeIds() {
		return formTypeIds;
	}

	public void setFormTypeIds(List<Integer> formTypeIds) {
		this.formTypeIds = formTypeIds;
	}

	public List<FormDataKind> getFormDataKinds() {
		return formDataKinds;
	}

	public void setFormDataKind(List<FormDataKind> formDataKinds) {
		this.formDataKinds = formDataKinds;
	}

	public List<WorkflowState> getStates() {
		return states;
	}

	public void setStates(List<WorkflowState> states) {
		this.states = states;
	}

	public List<TaxType> getTaxTypes() {
		return taxTypes;
	}

	public void setTaxTypes(List<TaxType> taxTypes) {
		this.taxTypes = taxTypes;
	}
}

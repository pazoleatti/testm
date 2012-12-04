package com.aplana.sbrf.taxaccounting.dao.model;


import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;

import java.io.Serializable;
import java.util.List;

/**
	Данный класс используется сервисом DataHandlerServiceImpl.
	DataHandlerServiceImpl получает FormDataFilter с пользовательской стороны и преобразует его в FormDataDaoFilter,
	который дальше идет на DAO слой.
 */
public class FormDataDaoFilter implements Serializable {

	private static final long serialVersionUID = -961619115678926848L;

	private List<Integer> reportPeriodId;

	private List<Integer> departmentId;

	/*Пример: Сведения о транспортных средствах, Расчет суммы налога, DEMO*/
	private List<Integer> formTypeId;

	/*Пример: Первичная, консалидированная, сводная, сводная банка*/
	private List<FormDataKind> formDataKind;

	private List<WorkflowState> formStates;


	public List<Integer> getReportPeriodId() {
		return reportPeriodId;
	}

	public void setReportPeriodId(List<Integer> reportPeriodId) {
		this.reportPeriodId = reportPeriodId;
	}

	public List<Integer> getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(List<Integer> departmentId) {
		this.departmentId = departmentId;
	}

	public List<Integer> getFormTypeId() {
		return formTypeId;
	}

	public void setFormTypeId(List<Integer> formTypeId) {
		this.formTypeId = formTypeId;
	}

	public List<FormDataKind> getFormDataKind() {
		return formDataKind;
	}

	public void setFormDataKind(List<FormDataKind> formDataKind) {
		this.formDataKind = formDataKind;
	}

	public List<WorkflowState> getFormStates() {
		return formStates;
	}

	public void setFormStates(List<WorkflowState> formStates) {
		this.formStates = formStates;
	}
}

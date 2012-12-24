package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * @author sgoryachkin
 *
 */
public class FormDataFilter implements Serializable{
	private static final long serialVersionUID = -4400641241082281834L;

	private Integer reportPeriodId;

	private Integer departmentId;

	/*Пример: Сведения о транспортных средствах, Расчет суммы налога, DEMO*/
	private Integer formTypeId;

	/*Пример: Первичная, консалидированная, сводная, сводная банка*/
    private FormDataKind formDataKind;

	private TaxType taxType;

	private WorkflowState formState;

	/*Стартовый индекс списка записей */
	private int startIndex;

	/*Количество записей, которые нужно вернуть*/
	private int countOfRecords;

	private FormDataSearchOrdering searchOrdering;

	/*true, если сортируем по возрастанию, false - по убыванию*/
	private boolean ascSorting;

	public Integer getReportPeriodId() {
		return reportPeriodId;
	}

	public void setReportPeriodId(Integer reportPeriodId) {
		this.reportPeriodId = reportPeriodId;
	}

	public Integer getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}

	public Integer getFormTypeId() {
		return formTypeId;
	}

	public void setFormTypeId(Integer formTypeId) {
		this.formTypeId = formTypeId;
	}

	public FormDataKind getFormDataKind() {
		return formDataKind;
	}

	public void setFormDataKind(FormDataKind formDataKind) {
		this.formDataKind = formDataKind;
	}

	public TaxType getTaxType() {
		return taxType;
	}

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}

	public WorkflowState getFormState() {
		return formState;
	}

	public void setFormState(WorkflowState formState) {
		this.formState = formState;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getCountOfRecords() {
		return countOfRecords;
	}

	public void setCountOfRecords(int countOfRecords) {
		this.countOfRecords = countOfRecords;
	}

	public FormDataSearchOrdering getSearchOrdering() {
		return searchOrdering;
	}

	public void setSearchOrdering(FormDataSearchOrdering searchOrdering) {
		this.searchOrdering = searchOrdering;
	}

	public boolean isAscSorting() {
		return ascSorting;
	}

	public void setAscSorting(boolean ascSorting) {
		this.ascSorting = ascSorting;
	}
}

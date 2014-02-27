package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author sgoryachkin
 *
 */
public class FormDataFilter implements Serializable{
	private static final long serialVersionUID = -4400641241082281834L;

	private List<Integer> reportPeriodIds;

	private List<Integer> departmentIds;

	/*Пример: Сведения о транспортных средствах, Расчет суммы налога, DEMO*/
	private List<Long> formTypeId;

	/*Пример: Первичная, консалидированная, сводная, сводная банка*/
    private List<Long> formDataKind;

	private TaxType taxType;

	private WorkflowState formState;

	/*Стартовый индекс списка записей */
	private int startIndex;

	/*Количество записей, которые нужно вернуть*/
	private int countOfRecords;

	private FormDataSearchOrdering searchOrdering;

	/*true, если сортируем по возрастанию, false - по убыванию*/
	private boolean ascSorting;

	private Boolean returnState;

    // месяцы
    private Months formMonth;

	public List<Integer> getDepartmentIds() {
		return departmentIds;
	}

	public void setDepartmentIds(List<Integer> departmentIds) {
		this.departmentIds = departmentIds;
	}

	public List<Long> getFormTypeId() {
		return formTypeId;
	}

	public void setFormTypeId(List<Long> formTypeId) {
		this.formTypeId = formTypeId;
	}

    public List<Long> getFormDataKind() {
        return formDataKind;
    }

    public void setFormDataKind(List<Long> formDataKind) {
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

	public Boolean getReturnState() {
		return returnState;
	}

	public void setReturnState(Boolean returnState) {
		this.returnState = returnState;
	}

	public List<Integer> getReportPeriodIds() {
		return reportPeriodIds;
	}

	public void setReportPeriodIds(List<Integer> reportPeriodIds) {
		this.reportPeriodIds = reportPeriodIds;
	}

    public Months getFormMonth() {
        return formMonth;
    }

    public void setFormMonth(Months formMonth) {
        this.formMonth = formMonth;
    }
}

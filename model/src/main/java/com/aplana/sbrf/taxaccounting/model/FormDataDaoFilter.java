package com.aplana.sbrf.taxaccounting.model;


import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
	Объект, представляющий условие фильтрации списка данных по налоговым формам.
 */
public class FormDataDaoFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Integer> reportPeriodIds;

	private List<Integer> comparativePeriodId;

	private List<Integer> departmentIds;

	private List<Long> formTypeIds;

	private List<FormDataKind> formDataKinds;

	private List<WorkflowState> states;

	private List<TaxType> taxTypes;

	private Boolean returnState;

    private Boolean correctionTag;

    private Date correctionDate;

	public List<Integer> getReportPeriodIds() {
		return reportPeriodIds;
	}

	public void setReportPeriodIds(List<Integer> reportPeriodIds) {
		this.reportPeriodIds = reportPeriodIds;
	}

	public List<Integer> getComparativePeriodId() {
		return comparativePeriodId;
	}

	public void setComparativePeriodId(List<Integer> comparativePeriodId) {
		this.comparativePeriodId = comparativePeriodId;
	}

	public List<Integer> getDepartmentIds() {
		return departmentIds;
	}

	public void setDepartmentIds(List<Integer> departmentIds) {
		this.departmentIds = departmentIds;
	}

	public List<Long> getFormTypeIds() {
		return formTypeIds;
	}

	public void setFormTypeIds(List<Long> formTypeIds) {
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

	public Boolean getReturnState() {
		return returnState;
	}

	public void setReturnState(Boolean returnState) {
		this.returnState = returnState;
	}

    /**
     * Признак корректирующего периода
     */
    public Boolean getCorrectionTag() {
        return correctionTag;
    }

    /**
     * Признак корректирующего периода
     */
    public void setCorrectionTag(Boolean correctionTag) {
        this.correctionTag = correctionTag;
    }

    public Date getCorrectionDate() {
        return correctionDate;
    }

    /**
     * Устанавливает дату корректировки. Действительно только при установленом {@link #correctionTag}
     * @param correctionDate Дата корректировки
     */
    public void setCorrectionDate(Date correctionDate) {
        this.correctionDate = correctionDate;
    }
}

package com.aplana.sbrf.taxaccounting.model;


import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
	Объект, представляющий условие фильтрации списка данных по налоговым формам.
 */
public class FormDataDaoFilter implements Serializable {

    /**
	 * Способ фильтрации по правам доступа 
	 * (используется, чтобы предотвратить попадание в поиск результатов, на которые у пользователя нет прав)
	 */
	public static enum AccessFilterType {
        /**
         * Все существующие формы (используется для Контролёра УНП)
         */
        ALL,
		/**
		 * Формы, относящиеся исключительно к перечисленным подразделениям (используется для контролеров)
		 */
		AVAILABLE_DEPARTMENTS,
        /**
         * Формы указанных типов, относящиеся исключительно к перечисленным подразделениям (используется для операторов)
         */
        AVAILABLE_DEPARTMENTS_WITH_KIND
	}

	private static final long serialVersionUID = 1L;

	private List<Integer> reportPeriodIds;

	private List<Integer> departmentIds;

    private List<Integer> availableDepartmentIds;

	private List<Long> formTypeIds;

	private List<FormDataKind> formDataKinds;

    private List<FormDataKind> availableFormDataKinds;

	private List<WorkflowState> states;

	private List<TaxType> taxTypes;

	private AccessFilterType accessFilterType;

	private Boolean returnState;

    private Boolean correctionTag;

    private Date correctionDate;

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

	/**
	 * Получает способ, по которому будут отбрасываться записи, недоступные пользователю
	 * Используется совместно с {@link #availableDepartmentIds} и {@link #availableFormDataKinds}
	 * @return способ, по которому будут отбрасываться записи, недоступные пользователю
	 */
	public AccessFilterType getAccessFilterType() {
		return accessFilterType;
	}

	/**
	 * Задать способ, по которому будут отбрасываться записи, недоступные пользователю
	 * @param accessFilter способ, по которому будут отбрасываться записи, недоступные пользователю
	 */
	public void setAccessFilterType(AccessFilterType accessFilter) {
		this.accessFilterType = accessFilter;
	}

	public Boolean getReturnState() {
		return returnState;
	}

	public void setReturnState(Boolean returnState) {
		this.returnState = returnState;
	}

    /**
     * Подразделения, доступные пользователю (работает только с AccessFilterType.AVAILABLE_DEPARTMENTS)
     */
    public List<Integer> getAvailableDepartmentIds() {
        return availableDepartmentIds;
    }

    /**
     * Подразделения, доступные пользователю (работает только с AccessFilterType.AVAILABLE_DEPARTMENTS)
     */
    public void setAvailableDepartmentIds(List<Integer> availableDepartmentIds) {
        this.availableDepartmentIds = availableDepartmentIds;
    }

    /**
     * Типы форм, доступные пользователю (работает только с AccessFilterType.AVAILABLE_DEPARTMENTS_WITH_KIND для оператора)
     */
    public List<FormDataKind> getAvailableFormDataKinds() {
        return availableFormDataKinds;
    }

    /**
     * Типы форм, доступные пользователю (работает только с AccessFilterType.AVAILABLE_DEPARTMENTS_WITH_KIND для оператора)
     */
    public void setAvailableFormDataKinds(List<FormDataKind> availableFormDataKinds) {
        this.availableFormDataKinds = availableFormDataKinds;
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

package com.aplana.sbrf.taxaccounting.model;


import java.io.Serializable;
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
		 * Формы, относящиеся исключительно к подразделению пользователя (используется для Операторов)
		 */
		USER_DEPARTMENT,
		/**
		 * Формы, относящиеся к подразделению пользователя и источники для этих форм (используется для Контролёров)
		 */
		USER_DEPARTMENT_AND_SOURCES
	}

	private static final long serialVersionUID = 1L;

	private List<Integer> reportPeriodIds;

	private List<Integer> departmentIds;

	private List<Integer> formTypeIds;

	private List<FormDataKind> formDataKinds;

	private List<WorkflowState> states;

	private List<TaxType> taxTypes;
	
	private int userDepartmentId;
	
	private AccessFilterType accessFilterType;

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

	/**
	 * Получает идентификатор подразделения, к которому относится пользователь, выполняющий запрос
	 * Используется совместно с {@link #accessFilterType}
	 * @return идентификатор подразделения, к которому относится пользователь, выполняющий запрос
	 */
	public int getUserDepartmentId() {
		return userDepartmentId;
	}

	/**
	 * Задать идентификатор подразделения, к которому относится пользователь, выполняющий запрос
	 * @param userDepartmentId идентификатор подразделения пользователя
	 */
	public void setUserDepartmentId(int userDepartmentId) {
		this.userDepartmentId = userDepartmentId;
	}

	/**
	 * Получает способ, по которому будут отбрасываться записи, недоступные пользователю
	 * Используется совместно с {@link #userDepartmentId}
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
}

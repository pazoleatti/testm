package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * DTO-Класс, содержащий информацию о параметрах налоговых форм и связанных с ним объектов в "плоском" виде
 * Используется для того, чтобы отображать результаты поисковых запросов по налоговым формам в таблицах, без необходимости
 * запрашивать из БД сведения по связанным объектам (название подразделения, вид налога и т.п.)
 */
public class FormDataSearchResultItem implements Serializable {

	private static final long serialVersionUID = 1L;

	public FormDataSearchResultItem() {
	}

	// Идентификатор записи с данными налоговой формы
	private Long formDataId;
	// Тип налоговой формы
	private FormDataKind formDataKind;
	// Стадия жизненного цикла
	private WorkflowState state;
	// Идентификатор описания налоговой формы
	private Integer formTemplateId;
	// Идентификатор вида налоговой формы
	private Integer formTypeId;
	// Название вида налоговой формы
	private String formTypeName;
	// Вид налога
	private TaxType taxType;
	// Идентификатор подразделения
	private Integer departmentId;
	// Название подразделения
	private String departmentName;
	// Тип подразделения
	private DepartmentType departmentType;
	// Идентификатор отчётного периода
	private Integer reportPeriodId;
	// Название отчётного периода
	private String reportPeriodName;
    // Очередность отчетного периода для ежемесячных НФ
    private Integer periodOrder;
	
	public Long getFormDataId() {
		return formDataId;
	}
	public void setFormDataId(Long formDataId) {
		this.formDataId = formDataId;
	}
	public FormDataKind getFormDataKind() {
		return formDataKind;
	}
	public void setFormDataKind(FormDataKind formDataKind) {
		this.formDataKind = formDataKind;
	}
	public WorkflowState getState() {
		return state;
	}
	public void setState(WorkflowState state) {
		this.state = state;
	}
	public Integer getFormTemplateId() {
		return formTemplateId;
	}
	public void setFormTemplateId(Integer formTemplateId) {
		this.formTemplateId = formTemplateId;
	}
	public Integer getFormTypeId() {
		return formTypeId;
	}
	public void setFormTypeId(Integer formTypeId) {
		this.formTypeId = formTypeId;
	}
	public String getFormTypeName() {
		return formTypeName;
	}
	public void setFormTypeName(String formTypeName) {
		this.formTypeName = formTypeName;
	}
	public TaxType getTaxType() {
		return taxType;
	}
	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}
	public Integer getDepartmentId() {
		return departmentId;
	}
	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}
	public String getDepartmentName() {
		return departmentName;
	}
	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
	public DepartmentType getDepartmentType() {
		return departmentType;
	}
	public void setDepartmentType(DepartmentType departmentType) {
		this.departmentType = departmentType;
	}
	public Integer getReportPeriodId() {
		return reportPeriodId;
	}
	public void setReportPeriodId(Integer reportPeriodId) {
		this.reportPeriodId = reportPeriodId;
	}
	public String getReportPeriodName() {
		return reportPeriodName;
	}
	public void setReportPeriodName(String reportPeriodName) {
		this.reportPeriodName = reportPeriodName;
	}
    public Integer getPeriodOrder() {
        return periodOrder;
    }
    public void setPeriodOrder(Integer periodOrder) {
        this.periodOrder = periodOrder;
    }
}

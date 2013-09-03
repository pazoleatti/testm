package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * DTO-Класс, содержащий информацию о параметрах декларации и связанных с ним объектов в "плоском" виде
 * Используется для того, чтобы отображать результаты поисковых запросов по декларациям в таблицах, без необходимости
 * запрашивать из БД сведения по связанным объектам (название подразделения, вид налога и т.п.)
 */
public class DeclarationDataSearchResultItem implements Serializable {

	private static final long serialVersionUID = -43124124124124L;

	public DeclarationDataSearchResultItem() {
	}

	// Идентификатор записи с данными декларации
	private Long declarationDataId;
	// Идентификатор шаблона декларации
	private Integer declarationTemplateId;
	// True - декларация принята, False - нет
	private boolean isAccepted;
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
    // Год отчетного периода
    private Integer reportPeriodYear;
	// Идентификатор вида декларации
	private String declarationType;

	public Long getDeclarationDataId() {
		return declarationDataId;
	}

	public void setDeclarationDataId(Long declarationDataId) {
		this.declarationDataId = declarationDataId;
	}

	public Integer getDeclarationTemplateId() {
		return declarationTemplateId;
	}

	public void setDeclarationTemplateId(Integer declarationTemplateId) {
		this.declarationTemplateId = declarationTemplateId;
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

	public boolean isAccepted() {
		return isAccepted;
	}

	public void setAccepted(boolean accepted) {
		isAccepted = accepted;
	}

	public String getDeclarationType() {
		return declarationType;
	}

	public void setDeclarationType(String declarationType) {
		this.declarationType = declarationType;
	}

    public Integer getReportPeriodYear() {
        return reportPeriodYear;
    }

    public void setReportPeriodYear(Integer reportPeriodYear) {
        this.reportPeriodYear = reportPeriodYear;
    }
}

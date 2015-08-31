package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;

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
    // Отчетный период подразделения
    private Integer departmentReportPeriodId;
	// Название отчётного периода
	private String reportPeriodName;
    // Год отчётного периода
    private Integer reportPeriodYear;
    // Срок сдачи корректировки
    private Date correctionDate;
    // Очередность отчетного периода для ежемесячных НФ
    private Integer reportPeriodMonth;
    // Признак возврата
    private Boolean returnSign;
    //Количество записей
    private int count;
    //Иерархические имена
    private String hierarchicalDepName;
    //Название периода сравнения
    private Integer comparativePeriodId;
    //Название периода сравнения
    private String comparativPeriodName;
    /** Признак расчета значений нф нарастающим итогом (false - не нарастающим итогом, true - нарастающим итогом, пустое - форма без периода сравнения) */
    private boolean accruing;

    public String getHierarchicalDepName() {
        return hierarchicalDepName;
    }

    public void setHierarchicalDepName(String hierarchicalDepName) {
        this.hierarchicalDepName = hierarchicalDepName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

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
    public Integer getReportPeriodMonth() {
        return reportPeriodMonth;
    }
    public void setReportPeriodMonth(Integer reportPeriodMonth) {
        this.reportPeriodMonth = reportPeriodMonth;
    }
    public Integer getReportPeriodYear() {
        return reportPeriodYear;
    }
    public void setReportPeriodYear(Integer reportPeriodYear) {
        this.reportPeriodYear = reportPeriodYear;
    }
    public Boolean getReturnSign() {
        return returnSign;
    }
    public void setReturnSign(Boolean returnSign) {
        this.returnSign = returnSign;
    }

    public Integer getDepartmentReportPeriodId() {
        return departmentReportPeriodId;
    }

    public void setDepartmentReportPeriodId(Integer departmentReportPeriodId) {
        this.departmentReportPeriodId = departmentReportPeriodId;
    }

    public Date getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(Date correctionDate) {
        this.correctionDate = correctionDate;
    }

    public Integer getComparativePeriodId() {
        return comparativePeriodId;
    }

    public void setComparativePeriodId(Integer comparativePeriodId) {
        this.comparativePeriodId = comparativePeriodId;
    }

    public String getComparativPeriodName() {
        return comparativPeriodName;
    }

    public void setComparativPeriodName(String comparativPeriodName) {
        this.comparativPeriodName = comparativPeriodName;
    }

    public boolean isAccruing() {
        return accruing;
    }

    public void setAccruing(boolean accruing) {
        this.accruing = accruing;
    }
}

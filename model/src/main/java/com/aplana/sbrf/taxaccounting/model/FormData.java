package com.aplana.sbrf.taxaccounting.model;

/**
 * Данные по отчётной форме
 *
 * @author dsultanbekov
 * @author sgoryachkin
 *
 */
public class FormData extends IdentityObject<Long> {
	private static final long serialVersionUID = 1L;

	private FormDataKind kind;
	private Integer departmentId;
	private Integer reportPeriodId;
    private Integer departmentReportPeriodId;
    /** Номер месяца */
    private Integer periodOrder;
    /** Признак формы ручного ввода */
    private boolean manual;
    /** Признак расчета значений нарастающим итогом (false - не нарастающим итогом, true - нарастающим итогом, пустое - форма без периода сравнения) */
    private boolean accruing;

	private int formTemplateId;
	FormTemplate formTemplate;
	private FormType formType;

    // Номер предыдущей формы
    private Integer previousRowNumber;

	public FormData() {
	}

	public FormData(FormTemplate form) {
		initFormTemplateParams(form);
	}

	public Integer getDepartmentId() {
		return departmentId;
	}

	/**
	 * Задать идентификатор {@link Department подразделения} к которому
	 * относится налоговая форма
	 *
	 * @param departmentId
	 *            идентификатор подразделения
	 */
	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}

	/**
	 * Возвращает идентификатор отчётного периода, к которому относится
	 * налоговая форма
	 *
	 * @return идентификатор отчётного периода
	 */
	public Integer getReportPeriodId() {
		return reportPeriodId;
	}

	/**
	 * Задать идентификатор отчётного периода, к которому относится налоговая
	 * форма
	 *
	 * @param reportPeriodId
	 *            значение идентификатора отчётного периода
	 */
	public void setReportPeriodId(int reportPeriodId) {
		this.reportPeriodId = reportPeriodId;
	}

    public Integer getDepartmentReportPeriodId() {
        return departmentReportPeriodId;
    }

    public void setDepartmentReportPeriodId(Integer departmentReportPeriodId) {
        this.departmentReportPeriodId = departmentReportPeriodId;
    }

    public FormDataKind getKind() {
		return kind;
	}

	public void setKind(FormDataKind kind) {
		this.kind = kind;
	}

	/**
	 * Задать налоговую форму, параметрами которой будут инициализированы поля
	 * formTemplateId и formColumns. Этот метод нужно обязательно вызвать перед
	 * любым обращением к данным формы. Вызвать этот метод можно только один раз
	 * для каждого объекта, попытка вызвать его повторно приведёт к исключению
	 * IllegalStateException. При создании объекта FormData с помощью
	 * конструктора, принимающего Form, этот метод будет вызван автоматически.
	 *
	 * @param formTemplate
	 *            описание шаблона налоговой формы
	 * @throws IllegalStateException
	 *             если значение поля form уже было проинициализировано
	 */
	public final void initFormTemplateParams(FormTemplate formTemplate) {
		this.formTemplateId = formTemplate.getId();
		this.formType = formTemplate.getType();
		this.formTemplate = formTemplate;
	}

	public void setFormType(FormType formType) {
		this.formType = formType;
	}

	public int getFormTemplateId() {
		return formTemplateId;
	}

	public FormTemplate getFormTemplate() {
		return formTemplate;
	}

	public void setFormTemplateId(int formTemplateId) {
        this.formTemplateId = formTemplateId;
    }

	public FormType getFormType() {
		return formType;
	}

    public Integer getPeriodOrder() {
        return periodOrder;
    }

    public void setPeriodOrder(Integer periodOrder) {
        this.periodOrder = periodOrder;
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    public boolean isAccruing() {
        return accruing;
    }

    public void setAccruing(boolean accruing) {
        this.accruing = accruing;
    }
}
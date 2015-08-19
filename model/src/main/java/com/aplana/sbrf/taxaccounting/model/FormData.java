package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;

import java.util.List;

/**
 * Данные по отчётной форме
 * 
 * @author dsultanbekov
 * @author sgoryachkin
 * 
 */
public class FormData extends IdentityObject<Long> {
	private static final long serialVersionUID = 1L;

	private WorkflowState state;
	private FormDataKind kind;
	private Integer departmentId;
	private Integer reportPeriodId;
    private Integer departmentReportPeriodId;
    /** Номер месяца */
    private Integer periodOrder;
    /** Признак формы ручного ввода */
    private boolean manual;
	/** Признак возврата */
	private boolean returnSign;
    /** Статус актуальности сортировки НФ (false - Сортировка неактуальна ; true - Сортировка актуальна) */
    private boolean sorted;
    /** Период сравнения - ссылка на DepartmentReportPeriod. Может быть null */
    private Integer comparativPeriodId;
    /** Признак расчета значений нарастающим итогом (false - не нарастающим итогом, true - нарастающим итогом, пустое - форма без периода сравнения) */
    private boolean accruing;

	private int formTemplateId;
	private List<Column> formColumns;
	private List<FormStyle> formStyles;

	private List<DataRow<HeaderCell>> headers;
	private FormType formType;

	private FormDataPerformer performer;
	private List<FormDataSigner> signers;

    // Номер предыдущей формы
    private Integer previousRowNumber;

	public FormData() {
	}

	public FormData(FormTemplate form) {
		initFormTemplateParams(form);
	}

	public List<FormStyle> getFormStyles() {
		return formStyles;
	}

	public WorkflowState getState() {
		return state;
	}

	/**
	 * Установить стадию жизненного цикла объекта FormData Данный метод можно
	 * вызвать только один раз для каждого инстанса FormData, предполагается,
	 * что это будет делаться в сервисном слое или в DAO. Для того, чтобы
	 * изменить стадию у уже существующего объекта нужно использовать методы
	 * 
	 * @{link FormDataWorkflowService} и затем перечитать состояние объекта из
	 *        БД при помощи DAO
	 * 
	 * @param state
	 *            объект, задающий стадию жизненного цикла
	 */
	public void setState(WorkflowState state) {
		if (this.state != null) {
			throw new IllegalStateException(
					"Value of state field is already initialized");
		}
		this.state = state;
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
		this.formColumns = formTemplate.getColumns();
		this.formTemplateId = formTemplate.getId();
		this.formType = formTemplate.getType();
		this.formStyles = formTemplate.getStyles();
		this.headers = formTemplate.getHeaders();
	}

	public void setFormType(FormType formType) {
		this.formType = formType;
	}

	public int getFormTemplateId() {
		return formTemplateId;
	}

    public void setFormTemplateId(int formTemplateId) {
        this.formTemplateId = formTemplateId;
    }

	public FormType getFormType() {
		return formType;
	}

	public List<Column> getFormColumns() {
		return formColumns;
	}
	
	/**
	 * Получить информацию об {@link FormDataPerformer исполнителе налоговой
	 * формы}
	 * 
	 * @return информация об исполнителе налоговой формы
	 */
	public FormDataPerformer getPerformer() {
		return performer;
	}

	/**
	 * Задать информацию об {@link FormDataPerformer исполнителе налоговой
	 * формы}
	 * 
	 * @param performer
	 *            информация об исполнителе налоговой формы
	 */
	public void setPerformer(FormDataPerformer performer) {
		this.performer = performer;
	}

	/**
	 * Получить список подписантов налоговой формы
	 * 
	 * @return список {@link FormDataSigner подписантов} налоговой формы
	 */
	public List<FormDataSigner> getSigners() {
		return signers;
	}

	/**
	 * Задать список подписантов налоговой формы
	 * 
	 * @param signers
	 *            список {@link FormDataSigner подписантов} налоговой формы
	 */
	public void setSigners(List<FormDataSigner> signers) {
		this.signers = signers;
	}

	public List<DataRow<HeaderCell>> getHeaders() {
		return headers;
	}

	public void setHeaders(List<DataRow<HeaderCell>> headers) {
		this.headers = headers;
	}

    /**
     * Фабричный метод создание новой строки
     * с установленными ячейками
     * @return
     */
    public DataRow<Cell> createDataRow(){
        List<Cell> cells = FormDataUtils.createCells(formColumns, formStyles);
        return new DataRow<Cell>(cells);
    }

	/**
	 * Фабричный метод создания новой строки
	 * с ячейками, позволяющими хранить результаты стандартных проверок
	 * @return
	 */
	public DataRow<Cell> createStoreMessagingDataRow(){
		List<Cell> cells = FormDataUtils.createCells(formColumns, formStyles);
		for (Cell cell : cells) {
			cell.setCheckMode(true);
		}
		return new DataRow<Cell>(cells);
	}
   
	public boolean isReturnSign() {
		return returnSign;
	}

	public void setReturnSign(boolean returnSign) {
		this.returnSign = returnSign;
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

    public Integer getPreviousRowNumber() {
        return previousRowNumber;
    }

    public void setPreviousRowNumber(Integer previousRowNumber) {
        this.previousRowNumber = previousRowNumber;
    }

    public boolean isSorted() {
        return sorted;
    }

    public void setSorted(boolean sorted) {
        this.sorted = sorted;
    }

    public Integer getComparativPeriodId() {
        return comparativPeriodId;
    }

    public void setComparativPeriodId(Integer comparativPeriodId) {
        this.comparativPeriodId = comparativPeriodId;
    }

    public boolean isAccruing() {
        return accruing;
    }

    public void setAccruing(boolean accruing) {
        this.accruing = accruing;
    }

    @Override
    public String toString() {
        return "FormData{" +
                "state=" + state +
                ", kind=" + kind +
                ", departmentId=" + departmentId +
                ", reportPeriodId=" + reportPeriodId +
                ", departmentReportPeriodId=" + departmentReportPeriodId +
                ", periodOrder=" + periodOrder +
                ", manual=" + manual +
                ", returnSign=" + returnSign +
                ", sorted=" + sorted +
                ", comparativPeriodId=" + comparativPeriodId +
                ", accruing=" + accruing +
                ", formTemplateId=" + formTemplateId +
                ", formType=" + formType +
                ", performer=" + performer +
                ", previousRowNumber=" + previousRowNumber +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FormData formData = (FormData) o;

        if (accruing != formData.accruing) return false;
        if (formTemplateId != formData.formTemplateId) return false;
        if (manual != formData.manual) return false;
        if (returnSign != formData.returnSign) return false;
        if (sorted != formData.sorted) return false;
        if (comparativPeriodId != null ? !comparativPeriodId.equals(formData.comparativPeriodId) : formData.comparativPeriodId != null)
            return false;
        if (departmentId != null ? !departmentId.equals(formData.departmentId) : formData.departmentId != null)
            return false;
        if (departmentReportPeriodId != null ? !departmentReportPeriodId.equals(formData.departmentReportPeriodId) : formData.departmentReportPeriodId != null)
            return false;
        if (formColumns != null ? !formColumns.equals(formData.formColumns) : formData.formColumns != null)
            return false;
        if (formStyles != null ? !formStyles.equals(formData.formStyles) : formData.formStyles != null) return false;
        if (formType != null ? !formType.equals(formData.formType) : formData.formType != null) return false;
        if (headers != null ? !headers.equals(formData.headers) : formData.headers != null) return false;
        if (kind != formData.kind) return false;
        if (performer != null ? !performer.equals(formData.performer) : formData.performer != null) return false;
        if (periodOrder != null ? !periodOrder.equals(formData.periodOrder) : formData.periodOrder != null)
            return false;
        if (previousRowNumber != null ? !previousRowNumber.equals(formData.previousRowNumber) : formData.previousRowNumber != null)
            return false;
        if (reportPeriodId != null ? !reportPeriodId.equals(formData.reportPeriodId) : formData.reportPeriodId != null)
            return false;
        if (signers != null ? !signers.equals(formData.signers) : formData.signers != null) return false;
        if (state != formData.state) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = state != null ? state.hashCode() : 0;
        result = 31 * result + (kind != null ? kind.hashCode() : 0);
        result = 31 * result + (departmentId != null ? departmentId.hashCode() : 0);
        result = 31 * result + (reportPeriodId != null ? reportPeriodId.hashCode() : 0);
        result = 31 * result + (departmentReportPeriodId != null ? departmentReportPeriodId.hashCode() : 0);
        result = 31 * result + (periodOrder != null ? periodOrder.hashCode() : 0);
        result = 31 * result + (manual ? 1 : 0);
        result = 31 * result + (returnSign ? 1 : 0);
        result = 31 * result + (sorted ? 1 : 0);
        result = 31 * result + (comparativPeriodId != null ? comparativPeriodId.hashCode() : 0);
        result = 31 * result + (accruing ? 1 : 0);
        result = 31 * result + formTemplateId;
        result = 31 * result + (formColumns != null ? formColumns.hashCode() : 0);
        result = 31 * result + (formStyles != null ? formStyles.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (formType != null ? formType.hashCode() : 0);
        result = 31 * result + (performer != null ? performer.hashCode() : 0);
        result = 31 * result + (signers != null ? signers.hashCode() : 0);
        result = 31 * result + (previousRowNumber != null ? previousRowNumber.hashCode() : 0);
        return result;
    }
}
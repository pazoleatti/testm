package com.aplana.sbrf.taxaccounting.model;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;

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
	
	/**
	 * Признак возврата
	 */
	private boolean returnSign;

	private int formTemplateId;
	private List<Column> formColumns;
	private List<FormStyle> formStyles;

	private List<DataRow<HeaderCell>> headers;
	private FormType formType;

	private FormDataPerformer performer;
	private List<FormDataSigner> signers;

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
	public void initFormTemplateParams(FormTemplate formTemplate) {
		if (this.formColumns != null) {
			throw new IllegalStateException(
					"FormTemplate-related properties was already initialized in this FormData instance");
		}
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
    
   
	public boolean isReturnSign() {
		return returnSign;
	}

	public void setReturnSign(boolean returnSign) {
		this.returnSign = returnSign;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FormData [state=");
		builder.append(state);
		builder.append(", kind=");
		builder.append(kind);
		builder.append(", departmentId=");
		builder.append(departmentId);
		builder.append(", reportPeriodId=");
		builder.append(reportPeriodId);
		builder.append(", returnSign=");
		builder.append(returnSign);
		builder.append(", formTemplateId=");
		builder.append(formTemplateId);
		builder.append(", formColumns=");
		builder.append(formColumns);
		builder.append(", formStyles=");
		builder.append(formStyles);
		builder.append(", headers=");
		builder.append(headers);
		builder.append(", formType=");
		builder.append(formType);
		builder.append(", performer=");
		builder.append(performer);
		builder.append(", signers=");
		builder.append(signers);
		builder.append("]");
		return builder.toString();
	}



}
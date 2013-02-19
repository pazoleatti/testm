package com.aplana.sbrf.taxaccounting.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Данные по отчётной форме
 * 
 * @author dsultanbekov
 */
/**
 * @author sgoryachkin
 *
 */
public class FormData extends IdentityObject<Long> {
	private static final long serialVersionUID = 1L;

	private WorkflowState state;
	private Date acceptanceDate;
	private FormDataKind kind;
	private Integer departmentId;
	private Integer reportPeriodId;


	private int formTemplateId;
	private List<Column> formColumns;
	private List<FormStyle> formStyles;
	
	private List<DataRow> dataRows;
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
	 * @{link FormDataWorkflowService} и затем перечитать состояние объекта из
	 * БД при помощи DAO
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
		dataRows = new ArrayList<DataRow>();
		this.formStyles = formTemplate.getStyles();
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

	public List<DataRow> getDataRows() {
		return dataRows;
	}

	/**
	 * Добавляет строку в таблицу данных. 
	 * Каждая строка может содержать уникальный алиас, для возможности идентификации её в скриптах
	 * @param rowAlias значение, задающее алиас.
	 * в большинстве случае должен быть строкой, но для удобства написания скриптов, принимает Object.
	 * Значением алиаса будет результат операции <code>rowAlias.toString()</code>
	 * @return добавленная строка с установленным алиасом
	 */
	public DataRow appendDataRow(Object rowAlias) {
		DataRow row = new DataRow(
				rowAlias == null ? null : rowAlias.toString(), formColumns,
				formStyles);
		synchronized (dataRows) {
			dataRows.add(row);
			row.setOrder(dataRows.size() + 1);
		}
		return row;
	}

	public DataRow appendDataRow() {
		return appendDataRow(null);
	}

	public DataRow getDataRow(String rowAlias) {
		if (rowAlias == null) {
			throw new NullPointerException("Row alias cannot be null");
		}
		for (DataRow row : dataRows) {
			if (rowAlias.equals(row.getAlias())) {
				return row;
			}
		}
		throw new IllegalArgumentException("Wrong row alias requested: "
				+ rowAlias);
	}
	
	/**
	 * Возвращает индекс строки, имеющий заданный алиас (с нуля).
	 * @param rowAlias алиас строки
	 * @return индекс строки
	 * @throws NullPointerException если rowAlias null
	 * @throws IllegalArgumentException если такого алиас не существует в объекте FormData
	 */
	public int getDataRowIndex(String rowAlias) {
		if (rowAlias == null) {
			throw new NullPointerException("Row alias cannot be null");
		}
		for (int index = 0; index < dataRows.size(); ++index) {
			DataRow row = dataRows.get(index);
			if (rowAlias.equals(row.getAlias())) {
				return index;
			}
		}
		throw new IllegalArgumentException("Wrong row alias requested: " + rowAlias);
	}
	

	/**
	 * Получить информацию об {@link FormDataPerformer исполнителе налоговой формы}
	 * @return информация об исполнителе налоговой формы
	 */
	public FormDataPerformer getPerformer() {
		return performer;
	}

	/**
	 * Задать информацию об {@link FormDataPerformer исполнителе налоговой формы}
	 * @param performer информация об исполнителе налоговой формы
	 */
	public void setPerformer(FormDataPerformer performer) {
		this.performer = performer;
	}

	/**
	 * Получить список подписантов налоговой формы
	 * @return список {@link FormDataSigner подписантов} налоговой формы
	 */
	public List<FormDataSigner> getSigners() {
		return signers;
	}

	/**
	 * Задать список подписантов налоговой формы
	 * @param signers список {@link FormDataSigner подписантов} налоговой формы
	 */
	public void setSigners(List<FormDataSigner> signers) {
		this.signers = signers;
	}

	/**
	 * Получить дату прехода в состояние ACCEPTED
	 * @return
	 */
	public Date getAcceptanceDate() {
		return acceptanceDate;
	}

	/**
	 * Установить дату перехода в состояние ACCEPTED
	 * @param acceptedDate
	 */
	public void setAcceptanceDate(Date acceptanceDate) {
		this.acceptanceDate = acceptanceDate;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FormData [state=");
		builder.append(state);
		builder.append(", acceptanceDate=");
		builder.append(acceptanceDate);
		builder.append(", kind=");
		builder.append(kind);
		builder.append(", departmentId=");
		builder.append(departmentId);
		builder.append(", reportPeriodId=");
		builder.append(reportPeriodId);
		builder.append(", formTemplateId=");
		builder.append(formTemplateId);
		builder.append(", formColumns=");
		builder.append(formColumns);
		builder.append(", formStyles=");
		builder.append(formStyles);
		builder.append(", dataRows=");
		builder.append(dataRows);
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
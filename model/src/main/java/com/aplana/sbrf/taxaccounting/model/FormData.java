package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Данные по отчётной форме
 * @author dsultanbekov
 */
public class FormData implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long id;
	private WorkflowState state;
	private FormDataKind kind;
	private Integer departmentId;

	private int formTemplateId;
	private List<Column> formColumns;
	private List<DataRow> dataRows;
	private FormType formType;

	public FormData() {
	}
	
	public FormData(Form form) {
		initFormTemplateParams(form);
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public WorkflowState getState() {
		return state;
	}

	/**
	 * Установить стадию жизненного цикла объекта FormData
	 * Данный метод можно вызвать только один раз для каждого инстанса FormData, предполагается, что это будет
	 * делаться в сервисном слое или в DAO.
	 * Для того, чтобы изменить стадию у уже существующего объекта нужно использовать методы @{link FormDataWorkflowService} и затем
	 * перечитать состояние объекта из БД при помощи DAO
	 * @param state объект, задающий стадию жизненного цикла
	 */
	public void setState(WorkflowState state) {
		if (this.state != null) {
			throw new IllegalStateException("Value of state field is already initialized");
		}
		this.state = state;
	}

	public Integer getDepartmentId() {
		return departmentId;
	}

	/**
	 * Задать идентификатор {@link Department подразделения} к которому относится налоговая форма
	 * @param departmentId идентификатор подразделения
	 */
	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}
	
	public FormDataKind getKind() {
		return kind;
	}

	public void setKind(FormDataKind kind) {
		this.kind = kind;
	}

	/**
	 * Задать налоговую форму, параметрами которой будут инициализированы поля formTemplateId и formColumns. 
	 * Этот метод нужно обязательно вызвать перед любым обращением к данным формы.
	 * Вызвать этот метод можно только один раз для каждого объекта, попытка вызвать его повторно приведёт к 
	 * исключению IllegalStateException. При создании объекта FormData с помощью конструктора, принимающего
	 * Form, этот метод будет вызван автоматически.
	 * @param formTemplate описание шаблона налоговой формы
	 * @throws IllegalStateException если значение поля form уже было проинициализировано 
	 */
	public void initFormTemplateParams(Form formTemplate) {
		if (this.formColumns != null) {
			throw new IllegalStateException("FormTemplate-related properties was already initialized in this FormData instance");
		}
		this.formColumns = formTemplate.getColumns();
		this.formTemplateId = formTemplate.getId();
		this.formType = formTemplate.getType();
		dataRows = new ArrayList<DataRow>();
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
	 * Добавляет строку в таблицу данных
	 * Каждая строка обязательно должна содержать уникальный алиас, для возможности
	 * идентификации её в скриптах
	 * @param rowAlias значение, задающее алиас.
	 * в большинстве случае должен быть строкой, но для удобства написания скриптов, принимает Object.
	 * Значением алиаса будет результат операции <code>rowAlias.toString()</code>
	 * @return добавленная строка с установленным алиасом
	 */
	public DataRow appendDataRow(Object rowAlias) {
		DataRow row = new DataRow(rowAlias == null ? null : rowAlias.toString(), formColumns);
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
		for (DataRow row: dataRows) {
			if (rowAlias.equals(row.getAlias())) {
				return row;
			}
		}
		throw new IllegalArgumentException("Wrong row alias requested: " + rowAlias);
	}
}

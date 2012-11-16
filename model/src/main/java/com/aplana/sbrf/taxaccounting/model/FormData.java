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
	private int stateId;
	
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

	public int getStateId() {
		return stateId;
	}

	public void setStateId(int stateId) {
		this.stateId = stateId;
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
		dataRows.add(row);
		row.setOrder(dataRows.size() + 1);
		return row;
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

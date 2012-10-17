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
	private Form form;
	private List<DataRow> dataRows;

	public FormData() {
	}
	
	public FormData(Form form) {
		setForm(form);
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Form getForm() {
		return form;
	}

	/**
	 * Установить налоговую форму.
	 * Этот метод нужно обязательно вызвать перед любым обращением к данным формы
	 * Вызвать этот метод можно только один раз для каждого объекта, попытка вызвать его повторно приведёт к 
	 * исключению IllegalStateException. 
	 * @param form налоговая форма
	 * @throws IllegalStateException если значение поля form уже было проинициализировано 
	 */
	public void setForm(Form form) {
		if (this.form != null) {
			throw new IllegalStateException("Form field is already initialized in this FormData instance");
		}
		dataRows = new ArrayList<DataRow>();
		this.form = form;
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
		DataRow row = new DataRow(rowAlias == null ? null : rowAlias.toString(), form);		
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

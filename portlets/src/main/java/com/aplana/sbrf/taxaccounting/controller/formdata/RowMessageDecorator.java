package com.aplana.sbrf.taxaccounting.controller.formdata;

import com.aplana.sbrf.taxaccounting.log.LogMessageDecorator;

/**
 * Декоратор сообщений журнала, используемый при выполеннии проверок по строкам таблицы
 * Добавляет к каждому сообщению имя проверки и номер строки, в которой она произошла
 */
public class RowMessageDecorator implements LogMessageDecorator {
	private int rowIndex;
	private String operationName;
	
	@Override
	public String getDecoratedMessage(String message) {
		return operationName + " [Строка " + rowIndex + "]: " + message;
	}

	public int getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

}

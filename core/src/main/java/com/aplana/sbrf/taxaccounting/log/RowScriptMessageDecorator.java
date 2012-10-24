package com.aplana.sbrf.taxaccounting.log;

/**
 * Декоратор сообщений журнала, используемый при выполеннии проверок по строкам таблицы
 * Добавляет к каждому сообщению имя проверки и номер строки, в которой она произошла
 */
public class RowScriptMessageDecorator extends ScriptMessageDecorator {
	private int rowIndex;
	
	@Override
	public String getDecoratedMessage(String message) {
		return scriptName + " [Строка " + rowIndex + "]: " + message;
	}

	public int getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}
}

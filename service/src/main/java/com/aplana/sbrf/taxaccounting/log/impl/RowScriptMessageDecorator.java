package com.aplana.sbrf.taxaccounting.log.impl;

/**
 * Декоратор сообщений журнала, используемый при выполеннии проверок по строкам таблицы
 * Добавляет к каждому сообщению имя скрипта и номер строки, в которой сообщение возникло.
 * Перед использованием нужно задать номер текущей строки в налоговой форме при помощи метода {@link #setRowIndex(int)}
 */
public class RowScriptMessageDecorator extends ScriptMessageDecorator {
	private int rowIndex;
	
	@Override
	public String getDecoratedMessage(String message) {
		return scriptName + " [Строка " + rowIndex + "]: " + message;
	}

	/**
	 * Получить номер строки в налоговой форме 
	 * @return номер строки в налоговой форме
	 */
	public int getRowIndex() {
		return rowIndex;
	}

	/**
	 * Задать номер строки в налоговой форме
	 * @param rowIndex номер строки в налоговой форме
	 */
	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}
}

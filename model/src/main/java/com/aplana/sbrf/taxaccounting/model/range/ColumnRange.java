package com.aplana.sbrf.taxaccounting.model.range;

/**
 * Описывает диапазон ячеек в рамках одного столбца
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 25.01.13 15:30
 */

public class ColumnRange extends Range {

	/**
	 *  Конструктор по умолчанию
	 *
	 * @param col индекс столбца, диапазон ячеек которого требуется получить
	 * @param rowFrom индекс строки (начало диапазона)
	 * @param rowTo индекс строки (конец диапазона)
	 */
	public ColumnRange(int col, int rowFrom, int rowTo) {
		super(col, rowFrom, col, rowTo);
	}

}

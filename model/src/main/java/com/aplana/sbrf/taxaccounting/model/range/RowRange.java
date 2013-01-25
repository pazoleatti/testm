package com.aplana.sbrf.taxaccounting.model.range;

/**
 * Описывает диапазон ячеек в рамках одной строки
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 25.01.13 15:30
 */

public class RowRange extends Range {

	/**
	 *  Конструктор по умолчанию
	 *
	 * @param row индекс строки, диапазон ячеек которой требуется получить
	 * @param colFrom индекс столбца (начало диапазона)
	 * @param colTo индекс столбца (конец диапазона)
	 */
	public RowRange(int row, int colFrom, int colTo) {
		super(colFrom, row, colTo, row);
	}

}

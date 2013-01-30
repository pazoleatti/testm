package com.aplana.sbrf.taxaccounting.model.script.range;

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
	 * @param colFromAlias псевдоним столбца (начало диапазона)
	 * @param colToAlias псевдоним столбца (конец диапазона)
	 */
	public RowRange(int row, String colFromAlias, String colToAlias) {
		super(colFromAlias, row, colToAlias, row);
	}

}

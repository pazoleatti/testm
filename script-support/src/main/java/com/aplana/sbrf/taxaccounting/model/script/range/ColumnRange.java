package com.aplana.sbrf.taxaccounting.model.script.range;

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
	 * @param colAlias псевдоним столбца, диапазон ячеек которого требуется получить
	 * @param rowFrom индекс строки (начало диапазона)
	 * @param rowTo индекс строки (конец диапазона)
	 */
	public ColumnRange(String colAlias, int rowFrom, int rowTo) {
		super(colAlias, rowFrom, colAlias, rowTo);
	}

}

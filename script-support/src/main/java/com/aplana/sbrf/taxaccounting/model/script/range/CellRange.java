package com.aplana.sbrf.taxaccounting.model.script.range;

/**
 * Вырожденный диапазон ячеек в виде одной ячейки
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 06.02.13 15:13
 */

public class CellRange extends Range {

	/**
	 *  Конструктор по умолчанию
	 *
	 * @param colAlias псевдоним столбца
	 * @param rowIndex индекс строки
	 */
	public CellRange(String colAlias, int rowIndex) {
		super(colAlias, rowIndex, colAlias, rowIndex);
	}

}

package com.aplana.sbrf.taxaccounting.service.script.util;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.range.Range;

import java.math.BigDecimal;
import java.util.List;

/**
 * Библиотека функций для вызова из скриптов
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 22.01.13 16:34
 */

public class ScriptUtils {

	private static final String WRONG_COLUMN_TYPE = "В указанном диапазоне столбцов \"%s\" - \"%s\" должны" +
			"быть только столбцы численного типа. Столбец \"%s\" имеет неверный тип.";

	private static final String WRONG_COLUMN_RANGE = "Указанный диапазон столбцов %d - %d выходит за границы таблицы." +
			"В таблице количество столбцов = %d";

	/**
	 * Вычисляет сумму указаных в диапазоне чисел. Null значения воспринимаются как 0
	 *
	 * @param formData таблица значений
	 * @param range диапазон ячеек для суммирования
	 * @return сумма диапазона
	 */
	public static double summ(FormData formData, Range range) {
		checks(formData, range);

		double sum = 0;
		List<DataRow> rows = formData.getDataRows();
		List<Column> cols = formData.getFormColumns();
		for (int i = range.getRowFrom(); i < range.getRowTo(); i++) {
			DataRow row = rows.get(i);
			for (int j = range.getColFrom(); j < range.getColTo(); j++) {
				BigDecimal value = (BigDecimal)row.get(cols.get(j).getAlias());
				if (value != null) {
					sum += value.doubleValue();
				}
			}
		}
		return sum;
	}

	public static double round(double value, int precision) {
		return value; //TODO реализовать округление
	}

	/**
	 * Осуществляет проверки допустимости указанного диапазона по отношению к текущей таблице данных
	 *
	 * @param formData таблица значений
	 * @param range проверяемый диапазон ячеек
	 */
	private static void checks(FormData formData, Range range) {
		checkColumnsRange(formData, range);
		checkNumericColumns(formData, range);
	}

	/**
	 * Проверяет допустимые границы диапазона для таблицы
	 *
	 * @param formData таблица значений
	 * @param range проверяемый диапазон ячеек
	 */
	private static void checkColumnsRange(FormData formData, Range range) {
		if (range.getColTo() >= formData.getFormColumns().size()) {
			throw new IllegalArgumentException(String.format(WRONG_COLUMN_RANGE, range.getColFrom(), range.getColTo(),
					formData.getFormColumns().size()));
		}
	}

	/**
	 * Проверяет, что в указанном диапазоне только числовые столбцы
	 *
	 * @param formData таблица значений
	 * @param range проверяемый диапазон ячеек
	 */
	private static void checkNumericColumns(FormData formData, Range range) {
		List<Column> cols = formData.getFormColumns();
		for (Column col : cols) {
			if (!(col instanceof NumericColumn))
				throw new IllegalArgumentException(String.format(WRONG_COLUMN_TYPE,
						cols.get(range.getColFrom()).getName(),
						cols.get(range.getColTo()).getName(),
						col.getName()));
		}
	}

	/**
	 * Возвращает по алиасу столбец
	 *
	 * @param formData таблица значений
	 * @param alias алиас столбца
	 * @return столбец таблицы
	 */
	public static Column getColumn(FormData formData, String alias) {
		for (Column col : formData.getFormColumns()) {
			if (col.getAlias().equals(alias)) {
				return col;
			}
		}
		return null;
	}

}

package com.aplana.sbrf.taxaccounting.service.script.util;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.aplana.sbrf.taxaccounting.model.script.range.Range;

import java.math.BigDecimal;
import java.util.List;

/**
 * Библиотека скриптовых функций
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 22.01.13 16:34
 */

public class ScriptUtils {

	private static final String WRONG_COLUMN_TYPE = "В указанном диапазоне столбцов \"%s\" - \"%s\" должны " +
			"быть только столбцы численного типа. Столбец \"%s\" имеет неверный тип.";

	private static final String WRONG_COLUMN_RANGE = "Указанный диапазон столбцов %d - %d выходит за границы таблицы. " +
			"В таблице количество столбцов = %d";

	private static final String WRONG_ROW_RANGE = "Указанный диапазон строк %d - %d выходит за границы таблицы. " +
			"В таблице количество строк = %d";

	private static final Log logger = LogFactory.getLog(ScriptUtils.class);

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
		for (int i = range.getRowFrom(); i <= range.getRowTo(); i++) {
			DataRow row = rows.get(i - 1);
			for (int j = range.getColFrom(); j <= range.getColTo(); j++) {
				Column col = cols.get(j - 1);
				BigDecimal value = (BigDecimal)row.get(col.getAlias());
				if (value != null) {
					sum += value.doubleValue();
				}
			}
		}
		return sum;
	}

	/**
	 * Округляет число до требуемой точности. Например, round(3.12345, 3) = 3.123, round(1.5, 0) = 2
	 *
	 * @param value округляемое число
	 * @param precision точность округления, знаки после запятой
	 * @return округленное число
	 */
	public static double round(double value, int precision) {
		double factor = Math.pow(10, precision);
		return Math.round(value * factor) / factor;
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
	 * @throws IndexOutOfBoundsException если указанный дипазон выходит за границы таблицы
	 */
	private static void checkColumnsRange(FormData formData, Range range) {
		if (range.getColTo() > formData.getFormColumns().size() || range.getColFrom() < 1) {
			throw new IndexOutOfBoundsException(String.format(WRONG_COLUMN_RANGE, range.getColFrom(), range.getColTo(),
					formData.getFormColumns().size()));
		}
		if (range.getRowTo() > formData.getDataRows().size() || range.getRowFrom() < 1) {
			throw new IndexOutOfBoundsException(String.format(WRONG_ROW_RANGE, range.getRowFrom(), range.getRowTo(),
					formData.getDataRows().size()));
		}
	}

	/**
	 * Проверяет, что в указанном диапазоне только числовые столбцы
	 *
	 * @param formData таблица значений
	 * @param range проверяемый диапазон ячеек
	 * @throws IllegalArgumentException если в диапазоне есть нечисловые столбцы
	 */
	private static void checkNumericColumns(FormData formData, Range range) {
		List<Column> cols = formData.getFormColumns();
		for (int j = range.getColFrom(); j <= range.getColTo(); j++) {
			Column col = cols.get(j - 1);
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

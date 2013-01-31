package com.aplana.sbrf.taxaccounting.service.script.util;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;

import com.aplana.sbrf.taxaccounting.model.script.range.Rect;
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

	/**
	 * Вычисляет сумму указаных в диапазоне чисел. Null значения воспринимаются как 0.
	 * Является аналогом Excel функции "СУММ" в нотации "СУММ(диапазон)"
	 * @see <a href="http://office.microsoft.com/ru-ru/excel-help/HP010342931.aspx?CTT=1">СУММ(число1,[число2],...])</a>
	 *
	 * @param formData таблица значений
	 * @param range диапазон ячеек для суммирования
	 * @return сумма диапазона
	 */
	public static double summ(FormData formData, Range range) {
		checkNumericColumns(formData, range);

		double sum = 0;
		List<DataRow> rows = formData.getDataRows();
		List<Column> cols = formData.getFormColumns();
		Rect rect = range.getRangeRect(formData);
		for (int i = rect.y1; i <= rect.y2; i++) {
			for (int j = rect.x1; j <= rect.x2; j++) {
				Column col = cols.get(j);
				BigDecimal value = (BigDecimal) rows.get(i).get(col.getAlias());
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
	 * Проверяет, что в указанном диапазоне только числовые столбцы
	 *
	 * @param formData таблица значений
	 * @param range проверяемый диапазон ячеек
	 * @throws IllegalArgumentException если в диапазоне есть нечисловые столбцы
	 */
	static void checkNumericColumns(FormData formData, Range range) {
		List<Column> cols = formData.getFormColumns();
		Rect rect = range.getRangeRect(formData);
		for (int j = rect.x1; j <= rect.x2; j++) {
			Column col = cols.get(j);
			if (!(col instanceof NumericColumn))
				throw new IllegalArgumentException(String.format(WRONG_COLUMN_TYPE,
						cols.get(rect.x1).getName(),
						cols.get(rect.x2).getName(),
						col.getName()));
		}
	}

	/**
	 * Суммирует ячейки второго диапазона только для тех строк, для которых выполняется условие фильтрации. В данном
	 * случае под условием фильтрации подразумевается равенство значений строк первого диапазона заранее заданному
	 * значению. Является аналогом Excel функции "СУММЕСЛИ" в нотации "СУММЕСЛИ(диапазон, критерий, диапазон_суммирования)"
	 * @see <a href="http://office.microsoft.com/ru-ru/excel-help/HP010342932.aspx?CTT=1">СУММЕСЛИ(диапазон, критерий, [диапазон_суммирования])</a>
	 *
	 * @param formData таблица данных
	 * @param conditionRange диапазон по которому осуществляется отбор строк (фильтрация)
	 * @param filterValue значение фильтра
	 * @param summRange диапазон суммирования
	 * @return сумма ячеек
	 */
	public double summIfEquals(FormData formData, Range conditionRange, Object filterValue, Range summRange) {
		Rect summRect = summRange.getRangeRect(formData);
		Rect condRange = conditionRange.getRangeRect(formData);
		if (!summRect.isSameSize(condRange))
			throw new IllegalArgumentException("Диапазоны указаны неверно");

		double sum = 0;
		List<DataRow> summRows = formData.getDataRows();
		List<Column> summCols = formData.getFormColumns();
		List<DataRow> condRows = formData.getDataRows();
		List<Column> condCols = formData.getFormColumns();
		for (int i = 0; i < condRange.getHeight(); i++) {
			for (int j = 0; j < condRange.getWidth(); j++) {
				BigDecimal condValue = (BigDecimal) condRows.get(condRange.y1 + i).get(condCols.get(condRange.x1 + j).getAlias());
				if (condValue != null && condValue.equals(filterValue)) {
					BigDecimal summValue = (BigDecimal) summRows.get(summRect.y1 + i).get(summCols.get(summRect.y1 + j).getAlias());
					if (summValue != null) {
						sum += summValue.doubleValue();
					}
				}
			}
		}
		return sum;
	}

	public static double summ(BigDecimal A, BigDecimal B) {
		double a = A == null ? 0 : A.doubleValue();
		double b = B == null ? 0 : B.doubleValue();
		return a + b;
	}

	public static double substract(BigDecimal A, BigDecimal B) {
		double a = A == null ? 0 : A.doubleValue();
		double b = B == null ? 0 : B.doubleValue();
		return a - b;
	}

}

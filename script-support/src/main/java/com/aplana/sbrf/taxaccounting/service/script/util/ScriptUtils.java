package com.aplana.sbrf.taxaccounting.service.script.util;

import com.aplana.sbrf.taxaccounting.model.Cell;
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

	private static final String NOT_SAME_RANGES = "Диапазоны имеют разную размерность";

	private static final String CELL_NOT_FOUND = "Ячейка (\"%s\"; \"%s\") не найдена";

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
				BigDecimal value = (BigDecimal) rows.get(i).get(cols.get(j).getAlias());
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
	 * @throws IllegalArgumentException диапазоны имеют разную размерность
	 */
	public static double summIfEquals(FormData formData, Range conditionRange, Object filterValue, Range summRange) {
		Rect summRect = summRange.getRangeRect(formData);
		Rect condRect = conditionRange.getRangeRect(formData);
		if (!summRect.isSameSize(condRect))
			throw new IllegalArgumentException(NOT_SAME_RANGES);

		double sum = 0;
		List<DataRow> summRows = formData.getDataRows();
		List<Column> summCols = formData.getFormColumns();
		List<DataRow> condRows = formData.getDataRows();
		List<Column> condCols = formData.getFormColumns();
		for (int i = 0; i < condRect.getHeight(); i++) {
			for (int j = 0; j < condRect.getWidth(); j++) {
				Object condValue = condRows.get(condRect.y1 + i).get(condCols.get(condRect.x1 + j).getAlias());
				if (condValue != null && condValue.equals(filterValue)) {
					BigDecimal summValue = (BigDecimal) summRows.get(summRect.y1 + i).get(summCols.get(summRect.x1 + j).getAlias());
					if (summValue != null) {
						sum += summValue.doubleValue();
					}
				}
			}
		}
		return sum;
	}

	/**
	 * Вычисляет сумму значений двух ячеек
	 *
	 * @param A первая ячейка
	 * @param B вторая ячейка
	 * @return сумма значений
	 */
	public static double summ(Cell A, Cell B) {
		double a = A.getNumericValue() == null ? 0 : A.getNumericValue().doubleValue();
		double b = B.getNumericValue() == null ? 0 : B.getNumericValue().doubleValue();
		return a+b;
	}

	/**
	 * Вычисляет разность между значениями двух ячеек
	 *
	 * @param A первая ячейка
	 * @param B вторая ячейка
	 * @return разность
	 */
	public static double substract(Cell A, Cell B) {
		double a = A.getValue() == null ? 0.0 : A.getNumericValue().doubleValue();
		double b = B.getValue() == null ? 0.0 : B.getNumericValue().doubleValue();
		return a-b;
	}

	/**
	 * Поиск ячейки таблицы по алиасам строки и столбца
	 *
	 * @param formData таблица данных
	 * @param rowAlias алиас строки
	 * @param columnAlias алиас столбца
	 * @return найденная ячейка
	 * @throws IllegalArgumentException указаны неправильные алиасы
	 *
	 */
	public static Cell getCell(FormData formData, String rowAlias, String columnAlias) {
		DataRow row = formData.getDataRow(rowAlias);
		if (row != null) {
			Cell cell = row.getCell(columnAlias);
			if (cell != null) {
				return cell;
			}
		}
		throw new IllegalArgumentException(String.format(CELL_NOT_FOUND, rowAlias, columnAlias));
	}

	/**
	 *	Функция копирует данные из одной таблицы в другую
	 *	@param fromFrom таблица - источник
	 *	@param toForm таблица - приемник
	 *	@param fromRange диапазон для копирования из источника
	 *	@param toRange диапазон для
	 *  @throws IllegalArgumentException указаны неправильные диапазоны ячеек
	 */
	public static void copyCellValues(FormData fromFrom, FormData toForm, Range fromRange, Range toRange){
		Rect fromRect = fromRange.getRangeRect(fromFrom);
		Rect toRect = toRange.getRangeRect(toForm);
		if (!fromRect.isSameSize(toRect))
			throw new IllegalArgumentException(NOT_SAME_RANGES);

		List<DataRow> fromRows = fromFrom.getDataRows();
		List<Column> fromCols = fromFrom.getFormColumns();
		List<DataRow> toRows = toForm.getDataRows();
		List<Column> toCols = toForm.getFormColumns();
		for (int i = 0; i < fromRect.getHeight(); i++) {
			for (int j = 0; j < fromRect.getWidth(); j++) {
				Object value = fromRows.get(fromRect.y1 + i).get(fromCols.get(fromRect.x1 + j).getAlias());
				Cell cell = toRows.get(toRect.y1 + i).getCell(toCols.get(toRect.x1 + j).getAlias());
				cell.setValue(value);
			}
		}
	}

}

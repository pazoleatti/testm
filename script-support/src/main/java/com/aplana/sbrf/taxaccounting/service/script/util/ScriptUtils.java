package com.aplana.sbrf.taxaccounting.service.script.util;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.script.range.Range;
import com.aplana.sbrf.taxaccounting.model.script.range.Rect;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Библиотека скриптовых функций
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 22.01.13 16:34
 */

public final class ScriptUtils {

	private static final String WRONG_COLUMN_TYPE = "В указанном диапазоне столбцов \"%s\" - \"%s\" должны " +
			"быть только столбцы численного типа. Столбец \"%s\" имеет неверный тип.";

	private static final String NOT_SAME_RANGES = "Диапазоны имеют разную размерность";

	private static final String CELL_NOT_FOUND = "Ячейка (\"%s\"; \"%s\") не найдена";

    private static final String WRONG_NUMBER = "Строка %d, колонка %d содержит нечисловое значение \"%s\"!";

    private static final String WRONG_DATE = "Строка %d, колонка %d содержит значение \"%s\", которое не " +
            "соответствует дате в формате \"%s\"!";

	/**
	 * Запрещаем создавать экземляры класса
	 */
	private ScriptUtils() {
	}

	/**
	 * Вычисляет сумму указаных в диапазоне чисел. Null значения воспринимаются как 0.
	 * Является аналогом Excel функции "СУММ" в нотации "СУММ(диапазон)"
	 * @see <a href="http://office.microsoft.com/ru-ru/excel-help/HP010342931.aspx?CTT=1">СУММ(число1,[число2],...])</a>
	 *
	 * @param formData таблица значений
	 * @param range диапазон ячеек для суммирования
	 * @return сумма диапазона
	 */
	public static double summ(FormData formData, List<DataRow<Cell>> dataRows, Range range) {
		checkNumericColumns(formData, dataRows, range);

		double sum = 0;
		List<DataRow<Cell>> rows = dataRows;
		List<Column> cols = formData.getFormColumns();
		Rect rect = range.getRangeRect(formData, dataRows);
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
	static void checkNumericColumns(FormData formData, List<DataRow<Cell>> dataRows, Range range) {
		List<Column> cols = formData.getFormColumns();
		Rect rect = range.getRangeRect(formData, dataRows);
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
	public static double summIfEquals(FormData formData, List<DataRow<Cell>> dataRows, Range conditionRange, Object filterValue, Range summRange) {
		Rect summRect = summRange.getRangeRect(formData, dataRows);
		Rect condRect = conditionRange.getRangeRect(formData, dataRows);
		if (!summRect.isSameSize(condRect))
			throw new IllegalArgumentException(NOT_SAME_RANGES);

		double sum = 0;
		List<DataRow<Cell>> summRows = dataRows;
		List<Column> summCols = formData.getFormColumns();
		List<DataRow<Cell>> condRows = dataRows;
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
	 * @param cellA первая ячейка
	 * @param cellB вторая ячейка
	 * @return сумма значений
	 */
	public static double summ(Cell cellA, Cell cellB) {
		double a = cellA.getNumericValue() == null ? 0 : cellA.getNumericValue().doubleValue();
		double b = cellB.getNumericValue() == null ? 0 : cellB.getNumericValue().doubleValue();
		return a+b;
	}

	/**
	 * Вычисляет разность между значениями двух ячеек
	 *
	 * @param cellA первая ячейка
	 * @param cellB вторая ячейка
	 * @return разность
	 */
	public static double substract(Cell cellA, Cell cellB) {
		double a = cellA.getValue() == null ? 0.0 : cellA.getNumericValue().doubleValue();
		double b = cellB.getValue() == null ? 0.0 : cellB.getNumericValue().doubleValue();
		return a-b;
	}

	/**
	 * Поиск ячейки таблицы по алиасам строки и столбца
	 *
	 * @param formData таблица данных
	 * @param columnAlias алиас столбца
	 * @param rowAlias алиас строки
	 * @return найденная ячейка
	 * @throws IllegalArgumentException указаны неправильные алиасы
	 *
	 */
	public static Cell getCell(FormData formData, List<DataRow<Cell>> dataRows,  String columnAlias, String rowAlias) {
		DataRow<Cell> row = FormDataUtils.getDataRowByAlias(dataRows, rowAlias);
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
	public static void copyCellValues(FormData fromFrom, List<DataRow<Cell>> fromDataRows,  FormData toForm, List<DataRow<Cell>> toDataRows, Range fromRange, Range toRange){
		Rect fromRect = fromRange.getRangeRect(fromFrom, fromDataRows);
		Rect toRect = toRange.getRangeRect(toForm, toDataRows);
		if (!fromRect.isSameSize(toRect))
			throw new IllegalArgumentException(NOT_SAME_RANGES);

		List<DataRow<Cell>> fromRows = fromDataRows;
		List<Column> fromCols = fromFrom.getFormColumns();
		List<DataRow<Cell>> toRows = toDataRows;
		List<Column> toCols = toForm.getFormColumns();
		for (int i = 0; i < fromRect.getHeight(); i++) {
			for (int j = 0; j < fromRect.getWidth(); j++) {
				Object value = fromRows.get(fromRect.y1 + i).get(fromCols.get(fromRect.x1 + j).getAlias());
				Cell cell = toRows.get(toRect.y1 + i).getCell(toCols.get(toRect.x1 + j).getAlias());
				cell.setValue(value);
			}
		}
	}

    /**
     * Получение числа из строки при импорте
     * @param value Строковое значение
     * @param indexRow Строка в импортируемом файле
     * @param indexColumn Колонка в импортируемом файле
     * @param logger
     * @param required Обязательность. При установке будет выкидываться исключение, иначе возвращатся null
     * @return
     */
    public static BigDecimal parseNumber(String value, int indexRow, int indexColumn, Logger logger, boolean required) {
        if (value == null) {
            return null;
        }
        String tmp = value.trim();
        if (tmp.isEmpty()) {
            return null;
        }
        tmp = tmp.replaceAll(",", ".").replace(" ", "");
        if (tmp.matches("-?\\d+(\\.\\d+)?")) {
            return new BigDecimal(tmp);
        } else {
            String msg = String.format(WRONG_NUMBER, indexRow, indexColumn, value);
            if (required == true) {
                throw new ServiceException(msg);
            } else {
                if (logger != null) {
                    logger.warn(msg);
                }
                return null;
            }
        }
    }

    /**
     * Получение даты из строки при импорте
     * @param value
     * @param format
     * @param indexRow
     * @param indexColumn
     * @param logger
     * @param required
     * @return
     */
    public static Date parseDate(String value, String format, int indexRow, int indexColumn, Logger logger, boolean required) {
        if (value == null || format == null) {
            return null;
        }
        String tmp = value.trim();
        if (tmp.isEmpty()) {
            return null;
        }
        Date retVal = null;
        try {
            retVal = new SimpleDateFormat(format).parse(tmp);
        } catch (ParseException ex) {
        }
        if (retVal == null) {
            String msg = String.format(WRONG_DATE, indexRow, indexColumn, value, format);
            if (required == true) {
                throw new ServiceException(msg);
            } else {
                if (logger != null) {
                    logger.warn(msg);
                }
                return null;
            }
        }
        return retVal;
    }
}

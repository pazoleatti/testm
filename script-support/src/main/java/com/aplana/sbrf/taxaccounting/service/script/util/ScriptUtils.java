package com.aplana.sbrf.taxaccounting.service.script.util;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.script.range.Range;
import com.aplana.sbrf.taxaccounting.model.script.range.Rect;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import com.aplana.sbrf.taxaccounting.service.script.ImportService;
import com.aplana.sbrf.taxaccounting.service.script.RefBookService;
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import org.apache.poi.ss.util.CellReference;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Библиотека скриптовых функций
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 22.01.13 16:34
 */

public final class ScriptUtils {

    private static final String WRONG_COLUMN_TYPE = "В указанном диапазоне граф «%s» - «%s» должны " +
            "быть только графы численного типа. Графа «%s» имеет неверный тип.";

    private static final String NOT_SAME_RANGES = "Диапазоны имеют разную размерность";

    private static final String CELL_NOT_FOUND = "Ячейка («%s», «%s») не найдена";

    private static final String WRONG_NUMBER = "Проверка файла: Строка %d, столбец %s содержит нечисловое значение «%s»!";

    private static final String WRONG_DATE = "Проверка файла: Строка %d, столбец %s содержит значение «%s», которое не " +
            "соответствует дате в формате «%s»!";

    private static final String WRONG_HEADER_EQUALS = "Заголовок таблицы не соответствует требуемой структуре. " +
            "Ожидается «%s» вместо «%s»!";

    private static final String WRONG_HEADER_COL_SIZE = "Заголовок таблицы не соответствует требуемой структуре. " +
            "Количество граф менее ожидаемого!";

    private static final String WRONG_HEADER_ROW_SIZE = "Заголовок таблицы не соответствует требуемой структуре. " +
            "Количество строк в заголовке менее ожидаемого!";

    private static final String GROUP_WRONG_ITOG = "Группа «%s» не имеет строки подитога!";

    private static final String GROUP_WRONG_ITOG_ROW = "Строка %d: Строка подитога не относится к какой-либо группе!";

    private static final String GROUP_WRONG_ITOG_SUM = "Строка %d: Неверное итоговое значение по группе «%s» в графе «%s»";

    private static final String WRONG_NON_EMPTY = "Строка %d: Графа «%s» не заполнена!";

    private static final String WRONG_CALC = "Строка %d: Неверное значение граф: %s!";

    private static final String WRONG_TOTAL = "Итоговые значения рассчитаны неверно в графе «%s»!";

    private static final String WRONG_SUBTOTAL = "Неверное итоговое значение по коду '%s' графы «%s»!";

    private static final String IMPORT_IS_NOT_PROVIDED = "Импорт данных не предусмотрен!";

    private static final String WRONG_DATA_PARSE = "Отсутствие значения после обработки потока данных!";

    private static final String EMPTY_FILE_NAME = "Имя файла не должно быть пустым!";
    private static final String EMPTY_INPUT_STREAM = "Поток данных пуст!";

    private static final String WRONG_XLS_FORMAT = "Выбранный файл не соответствует формату xls/xlsx/xlsm!";
    private static final String WRONG_RNU_FORMAT = "Выбранный файл не соответствует формату rnu!";

    private static final String WRONG_FIXED_VALUE = "Строка %d: Графа «%s» содержит значение «%s», не соответствующее значению «%s» данной графы в макете налоговой формы!";
    private static final String EMPTY_VALUE = "Строка %d: Графа «%s» содержит пустое значение, не соответствующее значению «%s» данной графы в макете налоговой формы!";
    private static final String EMPTY_EXPECTED_VALUE = "Строка %d: Графа «%s» содержит значение «%s», не соответствующее пустому значению данной графы в макете налоговой формы!";

    private static final String IMPORT_ROW_PREFIX = "Строка файла %d: %s";

    @SuppressWarnings("unused")
    private static final String TRANSPORT_FILE_SUM_ERROR = "Итоговая сумма в графе %s строки %s в транспортном файле некорректна. Загрузка файла не выполнена.";

    private static final String ROW_FILE_WRONG = "Строка файла %s содержит некорректное значение.";

    // Ссылочный, независимая графа: Не найдена версия справочника, соответствующая значению в файле
    public static final String REF_BOOK_NOT_FOUND_IMPORT_ERROR = "Проверка файла: Строка %d, столбец %s: В справочнике «%s» в атрибуте «%s» не найдено значение «%s», актуальное на дату %s!";
    // Ссылочный, зависимая графа: Значение в файле отличается от того, которое должно быть в зависимой графе
    public static final String REF_BOOK_REFERENCE_NOT_FOUND_IMPORT_ERROR = "Проверка файла: Строка %d, столбец %s содержит значение «%s», отсутствующее в справочнике «%s»!";
    // Ссылочный: Найдено несколько записей справочника, соответствующих значению в файле
    public static final String REF_BOOK_TOO_MANY_FOUND_IMPORT_ERROR = "Проверка файла: Строка %d, столбец %s: В справочнике «%s» в атрибуте «%s» найдено более одного значения «%s», актуального на дату %s!";

    private static final String WRONG_XLS_COLUMN_INDEX = "Номер столбца должен быть больше ноля!";

    // разделитель между идентификаторами в ключе для кеширования записей справочника
    private static final String SEPARATOR = "_";
    public static final String CHECK_OVERFLOW_MESSAGE = "Строка %d: Значение графы «%s» превышает допустимую разрядность (%d знаков). Графа «%s» рассчитывается как «%s»!";

    /**
     * Интерфейс для переопределения алгоритма расчета
     */
    public interface CalcAliasRow {
        DataRow<Cell> calc(int index, List<DataRow<Cell>> dataRows);
    }

    /**
     * Интерфейс для получения строки со значениями в группе
     */
    public interface GroupString {
        String getString(DataRow<Cell> row);
    }

    /**
     * Интерфейс для проверки сумм в итоговых строках
     */
    public interface CheckGroupSum {
        String check(DataRow<Cell> row1, DataRow<Cell> row2);
    }

    /**
     * Запрещаем создавать экземляры класса
     */
    private ScriptUtils() {
    }

    /**
     * Вычисляет сумму указаных в диапазоне чисел. Null значения воспринимаются как 0.
     * Является аналогом Excel функции "СУММ" в нотации "СУММ(диапазон)"
     *
     * @param formData таблица значений
     * @param range    диапазон ячеек для суммирования
     * @return сумма диапазона
     * @see <a href="http://office.microsoft.com/ru-ru/excel-help/HP010342931.aspx?CTT=1">СУММ(число1,[число2],...])</a>
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
     * @param value     округляемое число
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
     * @param range    проверяемый диапазон ячеек
     * @throws IllegalArgumentException если в диапазоне есть нечисловые столбцы
     */
    static void checkNumericColumns(FormData formData, List<DataRow<Cell>> dataRows, Range range) {
        List<Column> cols = formData.getFormColumns();
        Rect rect = range.getRangeRect(formData, dataRows);
        for (int j = rect.x1; j <= rect.x2; j++) {
            Column col = cols.get(j);
            if (!(ColumnType.NUMBER.equals(col.getColumnType())))
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
     *
     * @param formData       таблица данных
     * @param conditionRange диапазон по которому осуществляется отбор строк (фильтрация)
     * @param filterValue    значение фильтра
     * @param summRange      диапазон суммирования
     * @return сумма ячеек
     * @throws IllegalArgumentException диапазоны имеют разную размерность
     * @see <a href="http://office.microsoft.com/ru-ru/excel-help/HP010342932.aspx?CTT=1">СУММЕСЛИ(диапазон, критерий, [диапазон_суммирования])</a>
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
    @SuppressWarnings("unused")
    public static double summ(Cell cellA, Cell cellB) {
        double a = cellA.getNumericValue() == null ? 0 : cellA.getNumericValue().doubleValue();
        double b = cellB.getNumericValue() == null ? 0 : cellB.getNumericValue().doubleValue();
        return a + b;
    }

    /**
     * Вычисляет разность между значениями двух ячеек
     *
     * @param cellA первая ячейка
     * @param cellB вторая ячейка
     * @return разность
     */
    @SuppressWarnings("unused")
    public static double substract(Cell cellA, Cell cellB) {
        double a = cellA.getValue() == null ? 0.0 : cellA.getNumericValue().doubleValue();
        double b = cellB.getValue() == null ? 0.0 : cellB.getNumericValue().doubleValue();
        return a - b;
    }

    /**
     * Поиск ячейки таблицы по алиасам строки и столбца
     *
     * @param formData    таблица данных
     * @param columnAlias алиас столбца
     * @param rowAlias    алиас строки
     * @return найденная ячейка
     * @throws IllegalArgumentException указаны неправильные алиасы
     */
    public static Cell getCell(FormData formData, List<DataRow<Cell>> dataRows, String columnAlias, String rowAlias) {
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
     * Функция копирует данные из одной таблицы в другую
     *
     * @param fromFrom  таблица - источник
     * @param toForm    таблица - приемник
     * @param fromRange диапазон для копирования из источника
     * @param toRange   диапазон для
     * @throws IllegalArgumentException указаны неправильные диапазоны ячеек
     */
    @SuppressWarnings("unused")
    public static void copyCellValues(FormData fromFrom, List<DataRow<Cell>> fromDataRows, FormData toForm, List<DataRow<Cell>> toDataRows, Range fromRange, Range toRange) {
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
                cell.setValue(value, null);
            }
        }
    }

    /**
     * Получение числа из строки при импорте
     *
     * @param value       Строковое значение
     * @param indexRow    Строка в импортируемом файле
     * @param indexColumn Колонка в импортируемом файле
     * @param logger
     * @param required    Обязательность. При установке будет выкидываться исключение, иначе возвращатся null
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
            String msg = String.format(WRONG_NUMBER, indexRow, getXLSColumnName(indexColumn), value);
            if (required) {
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
     *
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

        // формат строки и шаблона не совпадают (excel может подставить в ячейку "yyyy" значение "01.01.yyyy")
        if (!tmp.contains(".") && format.contains(".")) {
            BigDecimal tmpNum = parseNumber(tmp, indexRow, indexColumn, logger, required);
            if (tmpNum != null && !tmpNum.equals(0)) {
                return new GregorianCalendar(tmpNum.intValue(), Calendar.JANUARY, 1).getTime();
            }
        }
        if (tmp.contains(".") && !format.contains(".")) {
            BigDecimal tmpNum = parseNumber(tmp.substring(tmp.lastIndexOf('.') + 1), indexRow, indexColumn, logger, required);
            if (tmpNum != null && !tmpNum.equals(0)) {
                return new GregorianCalendar(tmpNum.intValue(), Calendar.JANUARY, 1).getTime();
            }
        }

        Date retVal = null;
        try {
            retVal = parseDate(format, tmp);
        } catch (ParseException ex) {
        }
        if (retVal == null) {
            String msg = String.format(WRONG_DATE, indexRow, getXLSColumnName(indexColumn), value, format);
            if (required) {
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

    /**
     * Возвращает дату по строгому шаблону, иначе дата вида 01.13.2014 становится 01.01.2015
     *
     * @param format
     * @param value
     * @return
     * @throws ParseException
     */
    public static Date parseDate(String format, String value) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Date date = simpleDateFormat.parse(value);
        if (!simpleDateFormat.format(date).equals(value)) {
            throw new ParseException(String.format("Строка %s не соответствует формату даты %s", value, format), 0);
        }
        return date;
    }

    /**
     * Перевод даты в нужный формат
     *
     * @param date
     * @param format
     * @return
     */
    @SuppressWarnings("unused")
    public static String formatDate(Date date, String format) {
        if (date == null || format == null) {
            return null;
        }
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * Удаление всех строк с алиасами (подитоги и т.п.)
     *
     * @param dataRows
     * @return true если удаления были
     */
    @SuppressWarnings("unused")
    public static boolean deleteAllAliased(List<DataRow<Cell>> dataRows) {
        List<DataRow<Cell>> delList = new LinkedList<DataRow<Cell>>();
        boolean changed = false;
        for (DataRow<Cell> row : dataRows) {
            if (row.getAlias() != null) {
                delList.add(row);
                changed = true;
            }
        }
        dataRows.removeAll(delList);
        return changed;
    }

    /**
     * Добавление строк с алиасами (подитоги и т.п.)
     *
     * @param dataRows
     * @param calcAliasRow
     */
    @SuppressWarnings("unused")
    public static void addAllAliased(List<DataRow<Cell>> dataRows, CalcAliasRow calcAliasRow,
                                     List<String> groupColumns) {
        for (int i = 0; i < dataRows.size(); i++) {
            DataRow<Cell> row = dataRows.get(i);
            DataRow<Cell> nextRow = null;
            if (i < dataRows.size() - 1) {
                nextRow = dataRows.get(i + 1);
            }
            if (row.getAlias() != null) {
                continue;
            }
            if (nextRow == null || isDiffRow(row, nextRow, groupColumns)) {
                DataRow<Cell> aliasedRow = calcAliasRow.calc(i, dataRows);
                dataRows.add(++i, aliasedRow);
            }
        }
    }

    /**
     * Сравнение двух строк
     *
     * @param row
     * @param nextRow
     * @param groupColumns
     * @return
     */
    public static boolean isDiffRow(DataRow row, DataRow nextRow, List<String> groupColumns) {
        for (String alias : groupColumns) {
            Object v1 = row.getCell(alias).getValue();
            Object v2 = nextRow.getCell(alias).getValue();
            if (v1 == null && v2 == null) {
                continue;
            }
            if (v1 == null || v1 != null && !v1.equals(v2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Сортировка строк без учета группировок и наличия итоговых строк
     *
     * @return список неразличимых строк (важен для подитогов)
     */
    static Set<DataRow<Cell>> sortRowsSimple(List<DataRow<Cell>> dataRows) {
        final Set<DataRow<Cell>> set = new HashSet<DataRow<Cell>>();
        if (dataRows == null) {
            return set;
        }

        Collections.sort(dataRows, new Comparator<DataRow<Cell>>() {
            @Override
            public int compare(DataRow<Cell> row1, DataRow<Cell> row2) {
                List<Cell> cellList1 = new ArrayList<Cell>(row1.size());
                List<Cell> cellList2 = new ArrayList<Cell>(row2.size());

                if (cellList1.size() != cellList2.size()) {
                    throw new RuntimeException("Ошибка сортировки, в строках разное количество граф!");
                }

                for (String key : row1.keySet()) {
                    cellList1.add(row1.getCell(key));
                    cellList2.add(row2.getCell(key));
                }

                // Сравнение по графам
                int retVal = compareCell(cellList1, cellList2);
                if (retVal == 0) {
                    set.add(row1);
                    set.add(row2);
                }
                return retVal;
            }

            // Сравнение по графам
            private int compareCell(List<Cell> cellList1, List<Cell> cellList2) {
                if (cellList1.isEmpty() || cellList2.isEmpty()) {
                    // Достигли сравнение по всем графам — строки неразличимы
                    return 0;
                }

                Cell cell1 = cellList1.get(0);
                Cell cell2 = cellList2.get(0);

                cellList1.remove(0);
                cellList2.remove(0);

                if (cell1 == null || cell2 == null) {
                    throw new RuntimeException("Ошибка сортировки, одна из ячеек не задана!");
                }

                Column column = cell1.getColumn();

                if (column.getColumnType() == ColumnType.NUMBER
                        || column.getColumnType() == ColumnType.STRING
                        || column.getColumnType() == ColumnType.REFBOOK
                        || column.getColumnType() == ColumnType.REFERENCE) {

                    Comparable value1 = null;
                    Comparable value2 = null;

                    switch (column.getColumnType()) {
                        case NUMBER:
                            value1 = cell1.getNumericValue();
                            value2 = cell2.getNumericValue();
                            break;
                        case STRING:
                            value1 = cell1.getStringValue();
                            value2 = cell2.getStringValue();
                            break;
                        case REFERENCE:
                        case REFBOOK:
                            value1 = cell1.getRefBookDereference();
                            value2 = cell2.getRefBookDereference();
                            break;
                        case DATE:
                            value1 = cell1.getDateValue();
                            value2 = cell2.getDateValue();
                            break;
                    }

                    if (value1 != null || value2 != null) {
                        if (value1 == null ^ value2 == null) {
                            // Если одна из них null, то null всегда в конец
                            return value1 == null ? 1 : -1;
                        }

                        int compareResult = value1.compareTo(value2);
                        if (compareResult != 0) {
                            // Если значения совпадают, то сравнение должно пойти по остальным
                            return compareResult;
                        }
                    }
                }

                // Сравнение по остальным
                return compareCell(cellList1, cellList2);
            }
        });
        return set;
    }

    /**
     * Сортировка строк НФ
     *
     * @param refBookService   Сервис работы со справочниками
     * @param logger           Логгер
     * @param dataRows         Список строк НФ
     * @param subTotalDataRows Список подитоговых строк (может отсутствовать)
     * @param totalRow         Итоговая строка (может отсутствовать)
     * @param subTotalLast     Положение подитоговой строки, true — в конце, false — в начале (может отсутствовать)
     */
    @SuppressWarnings("unused")
    static void sortRows(RefBookService refBookService, Logger logger, List<DataRow<Cell>> dataRows,
                         List<DataRow<Cell>> subTotalDataRows,
                         DataRow<Cell> totalRow, Boolean subTotalLast) {
        if (dataRows == null || dataRows.isEmpty()) {
            return;
        }

        // Подитоговая строка → список строк
        final Map<DataRow<Cell>, List<DataRow<Cell>>> rowsMap = new HashMap<DataRow<Cell>, List<DataRow<Cell>>>();

        // Массовое разыменование строк НФ
        DataRow<Cell> firstRow = dataRows.get(0);
        List<Column> columnList = new ArrayList<Column>(firstRow.size());
        for (String key : firstRow.keySet()) {
            columnList.add(firstRow.getCell(key).getColumn());
        }
        refBookService.dataRowsDereference(logger, dataRows, columnList);

        // Если есть подитоговые строки
        if (subTotalDataRows != null) {
            if (subTotalLast == null) {
                throw new IllegalArgumentException("При наличии подитоговых строк необходимо указать параметр subTotalLast!");
            }

            // Сортировка подитоговых строк, после сортировки получаем список неразличимых строк
            final Set<DataRow<Cell>> indistinguishableSet = sortRowsSimple(subTotalDataRows);

            // Набор подитоговых строк
            Set<DataRow<Cell>> subTotalDataRowSet = new HashSet<DataRow<Cell>>(subTotalDataRows);

            boolean first = true;
            List<DataRow<Cell>> restList = null;

            // Подготовка групп
            List<DataRow<Cell>> currentList = new LinkedList<DataRow<Cell>>();
            for (DataRow<Cell> dataRow : dataRows) {
                // Итоговая строка пропускается, она всегда последняя
                if (totalRow == dataRow) {
                    continue;
                }
                // Встретилась итоговая строка, нужно или закончить группу или начать этой строкой
                if (subTotalDataRowSet.contains(dataRow)) {
                    // Если подитоговые строки перед группой, то это начало группы
                    if (!subTotalLast) {
                        if (first && !currentList.isEmpty()) {
                            // Остались строки без группы, их в начало, перед первой подитоговой строкой
                            restList = currentList;
                        }
                        first = false;
                        currentList = new LinkedList<DataRow<Cell>>();
                    }
                    rowsMap.put(dataRow, currentList);
                    // Если подитоговые строки после группы, то это конец группы
                    if (subTotalLast) {
                        currentList = new LinkedList<DataRow<Cell>>();
                    }
                } else {
                    // Обычная строка добавляется в текущий список
                    currentList.add(dataRow);
                }
            }

            // Очистка строк перед заполнением в новом порядке
            dataRows.clear();

            if (!indistinguishableSet.isEmpty()) {
                // Досортировка по первой строке группы
                Collections.sort(subTotalDataRows, new Comparator<DataRow<Cell>>() {
                    @Override
                    public int compare(DataRow<Cell> o1, DataRow<Cell> o2) {
                        if (!indistinguishableSet.contains(o1) || !indistinguishableSet.contains(o2)) {
                            return 0;
                        }
                        List<DataRow<Cell>> list1 = rowsMap.get(o1);
                        List<DataRow<Cell>> list2 = rowsMap.get(o2);

                        sortRowsSimple(list1);
                        sortRowsSimple(list2);

                        DataRow<Cell> firstRow1 = (list1 != null && !list1.isEmpty()) ? list1.get(0) : null;
                        DataRow<Cell> firstRow2 = (list2 != null && !list2.isEmpty()) ? list2.get(0) : null;

                        if (firstRow1 == null && firstRow2 == null) {
                            // Если обе подитоговые строки без обычных строк, то они неразличимы
                            return 0;
                        }

                        if (firstRow1 == null ^ firstRow2 == null) {
                            return firstRow1 == null ? 1 : -1;
                        }

                        List<DataRow<Cell>> list = Arrays.asList(firstRow1, firstRow2);
                        sortRowsSimple(list);
                        return list.indexOf(firstRow1) == 0 ? -1 : 1;
                    }
                });
            }

            if (restList != null) {
                // Остались строки без группы, их в начало, перед первой подитоговой строкой
                sortRowsSimple(restList);
                dataRows.addAll(restList);
            }

            // Заполнение по группам и сортировка внутри групп
            for (DataRow<Cell> subTotalDataRow : subTotalDataRows) {
                // Строки группы
                List<DataRow<Cell>> dataRowList = rowsMap.get(subTotalDataRow);
                // Сортировка внутри группы
                sortRowsSimple(dataRowList);

                if (subTotalLast) {
                    dataRows.addAll(dataRowList);
                    dataRows.add(subTotalDataRow);
                } else {
                    dataRows.add(subTotalDataRow);
                    dataRows.addAll(dataRowList);
                }
            }

            if (subTotalLast && !currentList.isEmpty()) {
                // Остались строки без группы, их в конец, перед итоговой строкой
                dataRows.addAll(currentList);
            }
        } else {
            if (totalRow != null) {
                dataRows.remove(totalRow);
            }
            // Сортировка строк
            sortRowsSimple(dataRows);
        }

        // Итоговая строка добавляется в конец списка
        if (totalRow != null) {
            dataRows.add(totalRow);
        }
    }

    /**
     * Сортировка строк (должна использоваться только для группировки). Не разыменовывает строки, т.к. не требуется.
     */
    @SuppressWarnings("unused")
    public static void sortRows(List<DataRow<Cell>> dataRows, final List<String> groupColumns) {
        Collections.sort(dataRows, new Comparator<DataRow<Cell>>() {
            @Override
            public int compare(DataRow<Cell> o1, DataRow<Cell> o2) {
                if (o1.getAlias() != null && o2.getAlias() == null) {
                    return 1;
                }
                if (o1.getAlias() == null && o2.getAlias() != null) {
                    return -1;
                }
                if (o1.getAlias() != null && o2.getAlias() != null) {
                    return 0;
                }

                for (String alias : groupColumns) {
                    Object v1 = o1.getCell(alias).getValue();
                    Object v2 = o2.getCell(alias).getValue();
                    if (v1 == null && v2 == null) {
                        continue;
                    }
                    if (v1 == null && v2 != null) {
                        return 1;
                    }
                    if (v1 != null && v2 == null) {
                        return -1;
                    }
                    if (v1 instanceof Comparable) {
                        int result = ((Comparable) v1).compareTo(v2);
                        if (result != 0) {
                            return result;
                        }
                    } else {
                        continue;
                    }
                }
                return 0;
            }
        });
    }

    /**
     * Проверка итоговых строк
     */
    @SuppressWarnings("unused")
    public static void checkItogRows(List<DataRow<Cell>> dataRows, List<DataRow<Cell>> testItogRows, List<DataRow<Cell>> itogRows,
                                     List<String> groupColums, Logger logger, GroupString groupString,
                                     CheckGroupSum checkGroupSum) {
        if (testItogRows.size() > itogRows.size()) {
            // Итоговые строки были удалены
            for (int i = 0; i < dataRows.size() - 1; i++) {
                DataRow<Cell> row = dataRows.get(i);
                DataRow<Cell> nextRow = dataRows.get(i + 1);
                if (row.getAlias() == null) {
                    if (nextRow == null || nextRow.getAlias() == null && isDiffRow(row, nextRow, groupColums)) {
                        String groupCols = groupString.getString(row);
                        if (groupCols != null) {
                            logger.error(GROUP_WRONG_ITOG, groupCols);
                        }
                    }
                }
            }
            // Последняя строка должна быть подитоговой
            if (dataRows.size() > 0) {
                DataRow<Cell> lastRow = dataRows.get(dataRows.size() - 1);
                if (lastRow.getAlias() == null) {
                    String groupCols = groupString.getString(lastRow);
                    if (groupCols != null) {
                        logger.error(GROUP_WRONG_ITOG, groupCols);
                    }
                }
            }
        } else if (testItogRows.size() < itogRows.size()) {
            // Неитоговые строки были удалены
            for (int i = 0; i < dataRows.size(); i++) {
                if (dataRows.get(i).getAlias() != null) {
                    if (i < 1 || dataRows.get(i - 1).getAlias() != null) {
                        logger.error(GROUP_WRONG_ITOG_ROW, dataRows.get(i).getIndex());
                    }
                }
            }
        } else {
            for (int i = 0; i < testItogRows.size(); i++) {
                DataRow<Cell> testItogRow = testItogRows.get(i);
                DataRow<Cell> realItogRow = itogRows.get(i);
                int itg = Integer.valueOf(testItogRow.getAlias().replaceAll("itg#", ""));
                if (dataRows.get(itg).getAlias() != null) {
                    logger.error(GROUP_WRONG_ITOG_ROW, dataRows.get(i).getIndex());
                } else {
                    String groupCols = groupString.getString(dataRows.get(itg));
                    if (groupCols != null) {
                        String checkStr = checkGroupSum.check(testItogRow, realItogRow);
                        if (checkStr != null) {
                            logger.error(String.format(GROUP_WRONG_ITOG_SUM, realItogRow.getIndex(), groupCols, checkStr));
                        }
                    }
                }
            }
        }
    }

    /**
     * Заголовок колонки по алиасу
     */
    public static String getColumnName(DataRow<Cell> row, String alias) {

        if (row == null || alias == null) {
            return "";
        }
        Cell cell = row.getCell(alias);
        if (cell == null) {
            return "";
        }
        // убрал замену одного процента на два, так как после ввода rowError стало неактуально
        // logger.error("%s", msg) корректно обрабатывает процента
        // напрямую вызов logger.error("...%...") происходить не должен
        return cell.getColumn().getName();
    }

    /**
     * Проверка заголовка импортируемого файла на соответствие размерности
     *
     * @param currentColSize   - количество столбцов в текущих данных
     * @param currentRowSize   - количество строк в текущих данных
     * @param referenceColSize - количество ожидаемых столбцов
     * @param referenceRowSize - количество ожидаемых строк
     */
    @SuppressWarnings("unused")
    public static void checkHeaderSize(int currentColSize, int currentRowSize, int referenceColSize, int referenceRowSize) {
        if (currentColSize < referenceColSize) {
            throw new ServiceException(WRONG_HEADER_COL_SIZE);
        }
        if (currentRowSize < referenceRowSize) {
            throw new ServiceException(WRONG_HEADER_ROW_SIZE);
        }
    }

    /**
     * Сравнение строки с эталонной
     *
     * @return null если строки совпадают, иначе текст ошибки
     */
    public static void checkHeaderEquals(Map<Object, String> headerMapping) {
        for (Object currentString : headerMapping.keySet()) {
            String referenceString = headerMapping.get(currentString);
            if (currentString == null || referenceString == null) {
                continue;
            }
            // замена двойного процента на одинарный приводит к равенству неравных заголовков - убрал
            // обратное экранирование процентов неактуально
            String s1 = currentString.toString().trim().replaceAll("  ", " ");
            String s2 = referenceString.trim().replaceAll("  ", " ");

            if (s1.equalsIgnoreCase(s2)) {
                continue;
            }
            throw new ServiceException(WRONG_HEADER_EQUALS, s2, s1);
        }
    }

    /**
     * Проверка пустых значений
     *
     * @param row
     * @param index
     * @param nonEmptyColums
     * @param logger
     * @param required
     */
    @SuppressWarnings("unused")
    public static void checkNonEmptyColumns(DataRow<Cell> row, int index, List<String> nonEmptyColums, Logger logger,
                                            boolean required) {
        for (String alias : nonEmptyColums) {
            Cell rowCell = row.getCell(alias);
            if (rowCell.getValue() == null || rowCell.getValue().toString().isEmpty()) {
                String msg = String.format(WRONG_NON_EMPTY, index, getColumnName(row, alias));
                if (required) {
                    rowError(logger, row, msg);
                } else {
                    rowWarning(logger, row, msg);
                }
            }
        }
    }

    /**
     * Арифметическая проверка
     *
     * @param row
     * @param calcColumns
     * @param calcValues
     * @param logger
     * @param required
     */
    @SuppressWarnings("unused")
    public static void checkCalc(DataRow<Cell> row, List<String> calcColumns, Map<String, Object> calcValues,
                                 Logger logger, boolean required) {
        List<String> errorColumns = new LinkedList<String>();
        for (String alias : calcColumns) {
            if (calcValues.get(alias) == null && row.getCell(alias).getValue() == null) {
                continue;
            }
            if (calcValues.get(alias) == null || row.getCell(alias).getValue() == null
                    || ((BigDecimal) calcValues.get(alias)).compareTo((BigDecimal) row.getCell(alias).getValue()) != 0) {
                errorColumns.add('«' + getColumnName(row, alias) + '»');
            }
        }
        if (!errorColumns.isEmpty()) {
            String msg = String.format(WRONG_CALC, row.getIndex(),
                    StringUtils.collectionToDelimitedString(errorColumns, ", "));
            if (required) {
                rowError(logger, row, msg);
            } else {
                rowWarning(logger, row, msg);
            }
        }
    }

    /**
     * Возвращает DataRow по алиасу.
     */
    @SuppressWarnings("unused")
    public static DataRow getDataRow(List<DataRow<Cell>> dataRows, String rowAlias) {
        if (rowAlias == null) {
            throw new NullPointerException("Row alias cannot be null");
        }
        for (DataRow<Cell> row : dataRows) {
            if (rowAlias.equals(row.getAlias())) {
                return row;
            }
        }
        throw new IllegalArgumentException("Wrong row alias requested: "
                + rowAlias);
    }

    /**
     * Получает значение самой ячейки или главной, если она в объединении
     */
    public static Object getOwnerValue(DataRow<Cell> dataRow, String alias) {
        Cell cell = dataRow.getCell(alias);
        return ((cell.hasValueOwner()) ? cell.getValueOwner().getValue() : cell.getValue());
    }

    /**
     * Расчет итогового значения, являющегося суммой по ячейкам одноименной графы
     *
     * @param dataRows
     * @param totalRow
     * @param columns
     */
    @SuppressWarnings("unused")
    public static void calcTotalSum(List<DataRow<Cell>> dataRows, DataRow<Cell> totalRow, List<String> columns) {
        for (String alias : columns) {
            BigDecimal sum = BigDecimal.valueOf(0);
            for (DataRow<Cell> row : dataRows) {
                if (row.getAlias() == null) {
                    BigDecimal val = (BigDecimal) row.getCell(alias).getValue();
                    if (val != null) {
                        sum = sum.add(val);
                    }
                }
            }
            totalRow.getCell(alias).setValue(sum, null);
        }
    }

    /**
     * Проверка расчета сумм итогов
     *
     * @param dataRows
     * @param columns
     * @param logger
     * @param required
     */
    @SuppressWarnings("unused")
    public static void checkTotalSum(List<DataRow<Cell>> dataRows, List<String> columns, Logger logger,
                                     boolean required) {
        DataRow<Cell> totalRow = null;
        Map<String, BigDecimal> totalSums = new HashMap<String, BigDecimal>();
        for (DataRow<Cell> row : dataRows) {
            if (row.getAlias() != null) {
                totalRow = row;
                continue;
            }
            for (String alias : columns) {
                if (!totalSums.containsKey(alias)) {
                    totalSums.put(alias, BigDecimal.ZERO);
                }
                BigDecimal val = (BigDecimal) row.getCell(alias).getValue();
                if (val != null) {
                    totalSums.put(alias, totalSums.get(alias).add(val));
                }
            }
        }
        if (totalRow != null) {
            for (String alias : columns) {
                if (!totalSums.containsKey(alias)) {
                    totalSums.put(alias, BigDecimal.valueOf(0));
                }
                BigDecimal value = (BigDecimal) totalRow.getCell(alias).getValue();
                BigDecimal totalValue = (value != null ? value : BigDecimal.ZERO);
                if (totalSums.get(alias).compareTo(totalValue) != 0) {
                    String msg = String.format(WRONG_TOTAL, getColumnName(totalRow, alias));
                    if (required) {
                        logger.error(msg);
                    } else {
                        logger.warn(msg);
                    }
                }
            }
        }
    }

    /**
     * Проверка расчета сумм подытогов
     *
     * @param dataRows
     * @param columns
     * @param logger
     * @param required
     */
    @SuppressWarnings("unused")
    public static void checkSubTotalSum(List<DataRow<Cell>> dataRows, List<String> columns, Logger logger,
                                        boolean required) {
        Map<String, DataRow<Cell>> totalRows = new HashMap<String, DataRow<Cell>>();
        List<String> subAliases = new ArrayList<String>();
        Map<String, Map<String, BigDecimal>> totalSums = new HashMap<String, Map<String, BigDecimal>>();

        for (DataRow<Cell> row : dataRows) {
            if (row.getAlias() != null && row.getAlias().length() > 5) {
                String subAlias = row.getAlias().replace("total", "");
                totalRows.put(subAlias, row);
                subAliases.add(subAlias);

                Map<String, BigDecimal> sums = new HashMap<String, BigDecimal>();
                for (String alias : columns) {
                    sums.put(alias, BigDecimal.ZERO);
                }
                totalSums.put(subAlias, sums);
            }
        }

        if (subAliases.size() == 0)
            return;

        String subAlias;
        int index = 0;
        for (DataRow<Cell> row : dataRows) {
            subAlias = subAliases.get(index);
            if (row.getAlias() != null) {
                if (subAliases.size() <= ++index)
                    break;
                continue;
            }
            for (String alias : columns) {
                BigDecimal val = (BigDecimal) row.getCell(alias).getValue();
                if (val != null) {
                    totalSums.get(subAlias).put(alias, totalSums.get(subAlias).get(alias).add(val));
                }
            }
        }

        for (String sub : subAliases) {
            for (String alias : columns) {
                BigDecimal value = (BigDecimal) totalRows.get(sub).getCell(alias).getValue();
                BigDecimal totalValue = (value != null ? value : BigDecimal.ZERO);
                if (totalSums.get(sub).get(alias).compareTo(totalValue) != 0) {
                    String msg = String.format(WRONG_SUBTOTAL, sub, getColumnName(dataRows.get(0), alias));
                    if (required) {
                        logger.error(msg);
                    } else {
                        logger.warn(msg);
                    }
                }
            }
        }
    }

    /**
     * Приводит текст к удобному для сравнения виду (обрезает, убирает лишние пробелы, если остается только крестик, то заменяет на пустую строку)
     *
     * @param value
     * @return
     */
    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        value = value.replaceAll("\n", " ");
        value = com.aplana.sbrf.taxaccounting.model.util.StringUtils.cleanString(value);
        if (value.equals("x") || value.equals("х")) {//если x или икс
            return "";
        }
        return value;
    }

    /**
     * Выдать сообщение что импорт не предусмотрен.
     */
    @SuppressWarnings("unused")
    public static void noImport(Logger logger) {
        logger.error(IMPORT_IS_NOT_PROVIDED);
    }

    /**
     * Получение xml с общими проверками
     * Используется при импорте из собственного формата системы
     */
    @SuppressWarnings("unused")
    public static GPathResult getXML(BufferedInputStream inputStream, ImportService importService, String fileName, String startStr, String endStr) {
        return getXML(inputStream, importService, fileName, startStr, endStr, null, null);
    }

    /**
     * Получение xml с общими проверками (указана шапка)
     * Используется при импорте из собственного формата системы
     */
    public static GPathResult getXML(BufferedInputStream inputStream, ImportService importService, String fileName, String startStr, String endStr, Integer columnsCount, Integer headerRowCount) {
        checkBeforeGetXml(inputStream, fileName);

        if (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx") && !fileName.endsWith(".xlsm")) {
            throw new ServiceException(WRONG_XLS_FORMAT);
        }

        String xmlString;
        try {
            xmlString = importService.getData(inputStream, fileName, "windows-1251", startStr, endStr, columnsCount, headerRowCount);
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }

        return getXML(xmlString);
    }

    /**
     * Получение xml с общими проверками
     * Используется при импорте из транспортного файла
     */
    @SuppressWarnings("unused")
    public static GPathResult getTransportXML(BufferedInputStream inputStream, ImportService importService, String fileName, int columnCount, int totalCount) {
        checkBeforeGetXml(inputStream, fileName);

        if (!fileName.endsWith(".rnu")) {
            throw new ServiceException(WRONG_RNU_FORMAT);
        }

        String xmlString;
        try {
            xmlString = importService.getData(inputStream, fileName, "cp866");
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }

        // в файле rnu по умолчанию пропускаются первые две строки
        int rowIndex = 2;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource();
            // парсим xml в Document
            inputSource.setCharacterStream(new StringReader(xmlString));
            Document document = documentBuilder.parse(inputSource);
            // получаем узлы соответствующие строкам
            NodeList rows = document.getElementsByTagName("row");
            // проходим по строкам
            for (int i = 0; i < rows.getLength(); i++) {
                rowIndex++;
                Element element = (Element) rows.item(i);
                // получаем атрибут кол-ва столбцов в строке
                Integer count = Integer.valueOf(element.getAttribute("count"));
                // добавляем два служебных поля к числу граф и сверяем с их числом в файле
                if (count != columnCount + 2) {
                    throw new ServiceException(ROW_FILE_WRONG, rowIndex);
                }
            }

            // после строк с данными через одну пустую должны быть итоги
            rowIndex += 2;

            // проверка на итоги идет только если они ожидаются
            if (totalCount != 0) {
                // получаем узлы соответствующие итогам
                NodeList totals = document.getElementsByTagName("rowTotal");
                // сверяем кол-во строк итогов
                if (totals.getLength() != totalCount) {
                    throw new ServiceException(ROW_FILE_WRONG, rowIndex);
                } else {
                    // проходим по итогам
                    for (int i = 0; i < totals.getLength(); i++) {
                        rowIndex++;
                        Element element = (Element) totals.item(i);
                        // получаем атрибут кол-ва столбцов в строке
                        Integer count = Integer.valueOf(element.getAttribute("count"));
                        // добавляем два служебных поля к числу граф и сверяем
                        if (count != columnCount + 2) {
                            throw new ServiceException(ROW_FILE_WRONG, rowIndex);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }

        return getXML(xmlString);
    }

    private static void checkBeforeGetXml(BufferedInputStream inputStream, String fileName) {
        fileName = fileName != null ? fileName.toLowerCase() : null;
        if (fileName == null || fileName.equals("")) {
            throw new ServiceException(EMPTY_FILE_NAME);
        }
        if (inputStream == null) {
            throw new ServiceException(EMPTY_INPUT_STREAM);
        }
    }

    private static GPathResult getXML(String xmlString) {
        if (xmlString == null) {
            throw new ServiceException(WRONG_DATA_PARSE);
        }
        GPathResult xml;
        try {
            xml = new XmlSlurper().parseText(xmlString);
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        } catch (SAXException e) {
            throw new ServiceException(e.getMessage());
        } catch (ParserConfigurationException e) {
            throw new ServiceException(e.getMessage());
        }
        if (xml == null) {
            throw new ServiceException(WRONG_DATA_PARSE);
        }
        return xml;
    }

    /**
     * Проверить фиксированное значение из файла на соответствие фиксированой строке из макета.
     *
     * @param row           строка
     * @param value         значение для проверки
     * @param valueExpected ожидаемое значение
     * @param indexRow      номер строки
     * @param alias         алиас столбца проверяемой графы
     * @param logger        для вывода лога
     * @param required      фатальность
     */
    @SuppressWarnings("unused")
    public static void checkFixedValue(DataRow<Cell> row, String value, String valueExpected, int indexRow, String alias, Logger logger, boolean required) {
        if (value != null && !value.equals(valueExpected) || value == null && valueExpected != null) {
            String msg;
            if (valueExpected != null && !valueExpected.trim().isEmpty() && value != null && !value.trim().isEmpty()) {
                msg = String.format(WRONG_FIXED_VALUE, indexRow, getColumnName(row, alias), value, valueExpected);
            } else if (valueExpected == null || valueExpected.trim().isEmpty()) {
                msg = String.format(EMPTY_EXPECTED_VALUE, indexRow, getColumnName(row, alias), value);
            } else {
                msg = String.format(EMPTY_VALUE, indexRow, getColumnName(row, alias), valueExpected);
            }
            if (required) {
                rowError(logger, row, msg);
            } else {
                rowWarning(logger, row, msg);
            }
        }
    }

    /**
     * Вывод предупреждения с учетом возможного присутствия информации о исходной позиции строки
     */
    public static void rowWarning(Logger logger, DataRow<Cell> row, String msg) {
        rowLog(logger, row, msg, LogLevel.WARNING);
    }

    /**
     * Вывод ошибки с учетом возможного присутствия информации о исходной позиции строки
     */
    public static void rowError(Logger logger, DataRow<Cell> row, String msg) {
        rowLog(logger, row, msg, LogLevel.ERROR);
    }

    /**
     * Вывод исключения с учетом возможного присутствия информации о исходной позиции строки
     */
    @SuppressWarnings("unused")
    public static void rowServiceException(DataRow<Cell> row, String msg) {
        if (row.getImportIndex() != null) {
            msg = String.format(IMPORT_ROW_PREFIX, row.getImportIndex(), msg);
        }
        throw new ServiceException(msg);
    }

    private static void rowLog(Logger logger, DataRow<Cell> row, String msg, LogLevel logLevel) {
        if (row != null && row.getImportIndex() != null) {
            msg = String.format(IMPORT_ROW_PREFIX, row.getImportIndex(), msg);
        }
        switch (logLevel) {
            case ERROR:
                logger.error("%s", msg);
                break;
            case WARNING:
                logger.warn("%s", msg);
                break;
        }
    }

    /**
     * Замена "ёлочек" («») на двойные кавычки (")
     */
    @SuppressWarnings("unused")
    public static String replaceQuotes(String value) {
        if (value != null) {
            value = value.replaceAll("«", "\"").replaceAll("»", "\"");
        }
        return value;
    }

    /**
     * Проверка количества найденных в скрипте по составному ключу записей справочника во время импорта.
     */
    @SuppressWarnings("unused")
    public static boolean checkImportRecordsCount(PagingResult<Map<String, RefBookValue>> records, RefBook refBook,
                                                  String alias, String value, Date date, int rowIndex, int colIndex,
                                                  Logger logger, boolean required) {
        if (records.size() == 1) {
            return true;
        } else {
            String dateFormat = (new SimpleDateFormat("dd.MM.yyyy")).format(date);
            String msg = String.format(
                    (records.size() > 1 ? REF_BOOK_TOO_MANY_FOUND_IMPORT_ERROR : REF_BOOK_NOT_FOUND_IMPORT_ERROR),
                    rowIndex, getXLSColumnName(colIndex), refBook.getName(), refBook.getAttribute(alias).getName(), value, dateFormat);
            if (required) {
                logger.error("%s", msg);
            } else {
                logger.warn("%s", msg);
            }
        }
        return false;
    }

    /**
     * Получить название столбца excel'я по номеру.
     */
    public static String getXLSColumnName(int index) {
        return index + " (" + getXLSColumnNumber(index) + ")";
    }

    /**
     * Получить буквенное название столбца excel'я по номеру.
     */
    public static String getXLSColumnNumber(int index) {
        if (index < 1) {
            throw new IllegalArgumentException(WRONG_XLS_COLUMN_INDEX);
        }
        return CellReference.convertNumToColString(index - 1);
    }

    /**
     * Получить ключ для кешированых записей по идентикатору справочника и записи.
     *
     * @param refBookId идентикатор справочника
     * @param recordId  идентикатор записи
     */
    public static String getRefBookCacheKey(Long refBookId, Long recordId) {
        return refBookId + SEPARATOR + recordId;
    }

    /**
     * Проверка формата введённых данных по регулярному выражению
     */
    public static boolean checkFormat(String enteredValue, String pat) {
        if (enteredValue == null || pat == null) {
            return false;
        }
        Pattern p = Pattern.compile(pat);
        Matcher m = p.matcher(enteredValue);
        return m.matches();
    }

    public static void checkOverflow(BigDecimal value, DataRow<Cell> row, String alias, int index, int size, String algorithm) {
        if (value == null) {
            return;
        }
        BigDecimal overpower = new BigDecimal("1E" + size);

        if (value.abs().compareTo(overpower) != -1) {
            String columnName = getColumnName(row, alias);
            throw new ServiceException(CHECK_OVERFLOW_MESSAGE, index, columnName, size, columnName, algorithm);
        }
    }
}


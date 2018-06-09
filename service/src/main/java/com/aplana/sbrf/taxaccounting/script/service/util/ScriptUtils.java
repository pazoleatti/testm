package com.aplana.sbrf.taxaccounting.script.service.util;

import au.com.bytecode.opencsv.CSVReader;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.consolidation.ConsolidationIncome;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.TAInterruptedException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.script.service.ImportService;
import com.aplana.sbrf.taxaccounting.script.service.RefBookService;
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.joda.time.LocalDateTime;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.aplana.sbrf.taxaccounting.model.util.StringUtils.cleanString;

/**
 * Библиотека скриптовых функций
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 22.01.13 16:34
 */

public final class ScriptUtils {
    private static final Log LOG = LogFactory.getLog(ScriptUtils.class);

    // Ссылочный, независимая графа: Не найдена версия справочника, соответствующая значению в файле
    public static final String REF_BOOK_NOT_FOUND_IMPORT_ERROR = "Проверка файла: Строка %d, столбец %s: В справочнике «%s» в атрибуте «%s» не найдено значение «%s», актуальное на дату %s!";
    // Ссылочный, зависимая графа: Значение в файле отличается от того, которое должно быть в зависимой графе
    public static final String REF_BOOK_REFERENCE_NOT_FOUND_IMPORT_ERROR = "Проверка файла: Строка %d, столбец %s содержит значение «%s», отсутствующее в справочнике «%s»!";
    public static final String REF_BOOK_REFERENCE_NOT_FOUND_IMPORT_ERROR_2 = "Проверка файла: Строка %d, столбец %s содержит значение «%s», которое не соответствует справочному значению «%s» графы «%s», найденному для «%s»!";
    // Ссылочный: Найдено несколько записей справочника, соответствующих значению в файле
    public static final String REF_BOOK_TOO_MANY_FOUND_IMPORT_ERROR = "Проверка файла: Строка %d, столбец %s: В справочнике «%s» в атрибуте «%s» найдено более одного значения «%s», актуального на дату %s!";
    public static final String CHECK_OVERFLOW_MESSAGE = "Строка %d: Значение графы «%s» превышает допустимую разрядность (%d знаков). Графа «%s» рассчитывается как «%s»!";
    // для проверки итогов при загрузе экселя (посчитанные и ожижаемые значения как %s потому что %f теряет точность)
    public static final String COMPARE_TOTAL_VALUES = "Строка %d файла: Итоговое значение по графе «%s» (значение «%s») указано некорректно. Системой рассчитано значение «%s».";
    public static final String COMPARE_TOTAL_VALUES_NULL = "Строка %d файла: Итоговое значение по графе «%s» не указано. Системой рассчитано значение «%s».";
    public static final String INN_JUR_PATTERN = RefBookUtils.INN_JUR_PATTERN;
    public static final String INN_JUR_MEANING = RefBookUtils.INN_JUR_MEANING;
    public static final String INN_IND_PATTERN = RefBookUtils.INN_IND_PATTERN;
    public static final String INN_IND_MEANING = RefBookUtils.INN_IND_MEANING;
    public static final String KPP_PATTERN = RefBookUtils.KPP_PATTERN;
    public static final String KPP_MEANING = RefBookUtils.KPP_MEANING;
    public static final String TAX_ORGAN_PATTERN = RefBookUtils.TAX_ORGAN_PATTERN;
    public static final String TAX_ORGAN_MEANING = RefBookUtils.TAX_ORGAN_MEANING;
    public static final String COLLATOR_RULES_RUSSIAN = "< 0 < 1 < 2 < 3 < 4 < 5 < 6 < 7 < 8 < 9 < a,A < b,B < c,C < d,D < e,E < f,F < g,G < h,H < i,I < j,J < k,K < l,L < m,M < n,N < o,O < p,P < q,Q < r,R < s,S < t,T < u,U < v,V < w,W < x,X < y,Y < z,Z < " +
            "а,А < б,Б < в,В < г,Г < д,Д < е,Е < ё,Ё < ж,Ж < з,З < и,И < й,Й < к,К < л,Л < м,М < н,Н < о,О < п,П < р,Р < с,С < т,Т < у,У < ф,Ф < х,Х < ц,Ц < ч,Ч < ш,Ш < щ,Щ < ъ,Ъ < ы,Ы < ь,Ь < э,Э < ю,Ю < я,Я";
    public static final String WRONG_COLUMN_TYPE = "В указанном диапазоне граф «%s» - «%s» должны " +
            "быть только графы численного типа. Графа «%s» имеет неверный тип.";
    public static final String NOT_SAME_RANGES = "Диапазоны имеют разную размерность";
    public static final String CELL_NOT_FOUND = "Ячейка («%s», «%s») не найдена";
    public static final String WRONG_TYPE = "Проверка файла: Строка %d, столбец %s: Тип ячейки не соответствует типу, указанному в ЧТЗ.";
    public static final String WRONG_HEADER_EQUALS = "Заголовок таблицы не соответствует требуемой структуре. " +
            "Ожидается «%s» вместо «%s»!";
    public static final String WRONG_HEADER_COL_SIZE = "Заголовок таблицы не соответствует требуемой структуре. " +
            "Количество граф менее ожидаемого!";
    public static final String WRONG_HEADER_ROW_SIZE = "Заголовок таблицы не соответствует требуемой структуре. " +
            "Количество строк в заголовке менее ожидаемого!";
    public static final String WRONG_HEADER_START = "Не удалось распознать заголовок таблицы. " +
            "Проверьте, что название первой графы в файле совпадает с названием первой графы в форме";
    public static final String GROUP_WRONG_ITOG = "Группа «%s» не имеет строки итога!";
    public static final String GROUP_WRONG_ITOG_ROW = "Строка %d: Строка итога не относится к какой-либо группе!";
    public static final String GROUP_WRONG_ITOG_SUM = "Строка %d: Неверное итоговое значение по группе «%s» в графе «%s»";
    public static final String WRONG_NON_EMPTY = "Строка %d: Графа «%s» не заполнена!";
    public static final String WRONG_CALC = "Строка %d: Неверное значение граф: %s!";
    public static final String WRONG_TOTAL = "Итоговые значения рассчитаны неверно в графе «%s»!";
    public static final String WRONG_SUBTOTAL = "Неверное итоговое значение по коду '%s' графы «%s»!";
    public static final String IMPORT_IS_NOT_PROVIDED = "Импорт данных не предусмотрен!";
    public static final String WRONG_DATA_PARSE = "Отсутствие значения после обработки потока данных!";
    public static final String EMPTY_FILE_NAME = "Имя файла не должно быть пустым!";
    public static final String EMPTY_INPUT_STREAM = "Поток данных пуст!";
    public static final String WRONG_FILE_FORMAT = "Неверная структура загружаемого файла!";
    public static final String WRONG_FILE_EXTENSION = "Выбранный файл не соответствует формату %s!";
    public static final String WRONG_FIXED_VALUE = "Строка %d: Графа «%s» содержит значение «%s», не соответствующее значению «%s» данной графы в макете налоговой формы!";
    public static final String EMPTY_VALUE = "Строка %d: Графа «%s» содержит пустое значение, не соответствующее значению «%s» данной графы в макете налоговой формы!";
    public static final String EMPTY_EXPECTED_VALUE = "Строка %d: Графа «%s» содержит значение «%s», не соответствующее пустому значению данной графы в макете налоговой формы!";
    public static final String IMPORT_ROW_PREFIX = "Строка файла %d: %s";
    public static final String CHECK_DATE_PERIOD = "Строка %d: Дата по графе «%s» должна принимать значение из диапазона: %s - %s!";
    public static final String CHECK_DATE_PERIOD_EXT = "Строка %d: Дата по графе «%s» должна принимать значение из диапазона %s - %s и быть больше либо равна дате по графе «%s»!";
    @SuppressWarnings("unused")
    public static final String TRANSPORT_FILE_SUM_ERROR = "Итоговая сумма в графе %s строки %s в транспортном файле некорректна.";
    public static final String TRANSPORT_FILE_SUM_ERROR_1 = "Строка %d файла: Итоговое значение по графе «%s» (значение «%s») указано некорректно. Системой рассчитано значение «%s»";
    public static final String TRANSPORT_FILE_SUM_ERROR_2 = "Строка %d файла: Итоговое значение по графе «%s» не указано. Системой рассчитано значение «%s»";
    public static final String ROW_FILE_WRONG = "Строка файла %s содержит некорректное значение.";
    public static final String WRONG_XLS_COLUMN_INDEX = "Номер столбца должен быть больше ноля!";
    // разделитель между идентификаторами в ключе для кеширования записей справочника
    public static final String SEPARATOR = "_";
    public static final String SNILS_REGEXP = "\\d{3}-\\d{3}-\\d{3}\\s\\d{2}";
    public static final String DUL_REGEXP = "[^№]+\\s[^N№]+";
    public static String DATE_FORMAT = "dd.MM.yyyy";

    /**
     * Запрещаем создавать экземляры класса
     */
    private ScriptUtils() {
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
        tmp = tmp.replaceAll(",", ".").replace(" ", "").replaceAll("\\u00A0", "");
        if (tmp.matches("-?\\d+(\\.\\d+)?")) {
            return new BigDecimal(tmp);
        } else {
            if (logger != null) {
                String msg = String.format(WRONG_TYPE, indexRow, getXLSColumnName(indexColumn));
                if (required) {
                    logger.error("%s", msg);
                } else {
                    logger.warn("%s", msg);
                }
            }
            return null;
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
        if (tmp.matches("\\d*") && tmp.length() == 4 && format.contains(".")) {
            BigDecimal tmpNum = parseNumber(tmp, indexRow, indexColumn, logger, required);
            if (tmpNum != null && tmpNum.compareTo(BigDecimal.ZERO) != 0) {
                return new GregorianCalendar(tmpNum.intValue(), Calendar.JANUARY, 1).getTime();
            }
        }
        if (tmp.contains(".") && !format.contains(".")) {
            BigDecimal tmpNum = parseNumber(tmp.substring(tmp.lastIndexOf('.') + 1), indexRow, indexColumn, logger, required);
            if (tmpNum != null && tmpNum.compareTo(BigDecimal.ZERO) != 0) {
                return new GregorianCalendar(tmpNum.intValue(), Calendar.JANUARY, 1).getTime();
            }
        }

        Date retVal = null;
        try {
            retVal = parseDate(format, tmp);
        } catch (ParseException ex) {
            // костыль для обхода http://jira.aplana.com/browse/SBRFACCTAX-11560
            try {
                if ("dd.mm.yyyy".equalsIgnoreCase(format) && tmp.matches("\\d{1,2}/\\d{1,2}/\\d{1,2}")) {
                    retVal = parseDate("M/d/yy", tmp);
                }
            } catch (ParseException ex2) {
            }
        }
        if (retVal == null) {
            if (logger != null) {
                String msg = String.format(WRONG_TYPE, indexRow, getXLSColumnName(indexColumn));
                if (required) {
                    logger.error("%s", msg);
                } else {
                    logger.warn("%s", msg);
                }
            }
            return null;
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
    public static boolean isDiffRow(DataRow<Cell> row, DataRow<Cell> nextRow, List<String> groupColumns) {
        for (String alias : groupColumns) {
            boolean isRefBook = Arrays.asList(ColumnType.REFBOOK, ColumnType.REFERENCE).contains(row.getCell(alias).getColumn().getColumnType());
            Object v1 = isRefBook ? row.getCell(alias).getRefBookDereference() : row.getCell(alias).getValue();
            Object v2 = isRefBook ? nextRow.getCell(alias).getRefBookDereference() : nextRow.getCell(alias).getValue();
            if (v1 == null && v2 == null) {
                v1 = row.getCell(alias).getValue();
                v2 = nextRow.getCell(alias).getValue();
            }
            if (v1 == null && v2 == null) {
                continue;
            }
            if (v1 == null || (v1 instanceof String && !((String) v1).equalsIgnoreCase((String) v2)) || (!(v1 instanceof String) && !v1.equals(v2))) {
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

                    if ("".equals(value1)) {
                        value1 = null;
                    }
                    if ("".equals(value2)) {
                        value2 = null;
                    }
                    if (value1 != null || value2 != null) {
                        if (value1 == null ^ value2 == null) {
                            // Если одна из них null, то null всегда в конец
                            return value1 == null ? 1 : -1;
                        }

                        int compareResult;
                        if (value1 instanceof String) {
                            compareResult = ((String) value1).compareToIgnoreCase((String) value2);
                        } else {
                            compareResult = value1.compareTo(value2);
                        }
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
                    boolean isRefBook = Arrays.asList(ColumnType.REFBOOK, ColumnType.REFERENCE).contains(o1.getCell(alias).getColumn().getColumnType());
                    Object v1 = isRefBook ? o1.getCell(alias).getRefBookDereference() : o1.getCell(alias).getValue();
                    Object v2 = isRefBook ? o2.getCell(alias).getRefBookDereference() : o2.getCell(alias).getValue();
                    if (v1 == null && v2 == null) {
                        continue;
                    }
                    if (v1 == null && v2 != null) {
                        return 1;
                    }
                    if (v1 != null && v2 == null) {
                        return -1;
                    }
                    if (v1 instanceof String) {
                        int result = ((String) v1).compareToIgnoreCase((String) v2);
                        if (result != 0) {
                            return result;
                        } else {
                            continue;
                        }
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
     * Сортировка строк по алфавиту
     */
    @SuppressWarnings("unused")
    public static void sortRowsRussian(List<DataRow<Cell>> dataRows, final List<String> groupColumns) throws ParseException {
        final RuleBasedCollator russianCollator = new RuleBasedCollator(COLLATOR_RULES_RUSSIAN);
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
                    boolean isRefBook = Arrays.asList(ColumnType.REFBOOK, ColumnType.REFERENCE).contains(o1.getCell(alias).getColumn().getColumnType());
                    Object v1 = isRefBook ? o1.getCell(alias).getRefBookDereference() : o1.getCell(alias).getValue();
                    Object v2 = isRefBook ? o2.getCell(alias).getRefBookDereference() : o2.getCell(alias).getValue();
                    if (v1 == null && v2 == null) {
                        continue;
                    }
                    if (v1 == null && v2 != null) {
                        return 1;
                    }
                    if (v1 != null && v2 == null) {
                        return -1;
                    }
                    if (v1 instanceof String) {
                        int result = russianCollator.compare((String) v1, (String) v2);
                        if (result != 0) {
                            return result;
                        } else {
                            continue;
                        }
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
    @Deprecated
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
            if (!dataRows.isEmpty()) {
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
                    if (i < 1 || (dataRows.get(i - 1).getAlias() != null && !"total".equals(dataRows.get(i).getAlias()))) {
                        logger.error(GROUP_WRONG_ITOG_ROW, dataRows.get(i).getIndex());
                    }
                }
            }
        } else {
            for (int i = 0; i < testItogRows.size(); i++) {
                DataRow<Cell> testItogRow = testItogRows.get(i);
                DataRow<Cell> realItogRow = itogRows.get(i);
                String str = testItogRow.getAlias().split("#")[0];
                int itg = Integer.valueOf(testItogRow.getAlias().replaceAll(str + '#', ""));
                if (dataRows.get(itg).getAlias() != null) {
                    logger.error(GROUP_WRONG_ITOG_ROW, dataRows.get(i).getIndex());
                } else {
                    String groupCols = groupString.getString(dataRows.get(itg));
                    if (groupCols != null) {
                        String checkStr = checkGroupSum.check(testItogRow, realItogRow);
                        if (checkStr != null) {
                            logger.error(GROUP_WRONG_ITOG_SUM, realItogRow.getIndex(), groupCols, checkStr);
                        }
                    }
                }
            }
        }
    }

    /**
     * Проверка итоговых строк
     */
    @SuppressWarnings("unused")
    public static void checkItogRows(List<DataRow<Cell>> dataRows, List<DataRow<Cell>> testItogRows, List<DataRow<Cell>> itogRows,
                                     List<String> groupColums, Logger logger, boolean fatal, GroupString groupString,
                                     CheckGroupSum checkGroupSum, CheckDiffGroup checkDiffGroup) {
        LogLevel logLevel = fatal ? LogLevel.ERROR : LogLevel.WARNING;
        // считает количество реальных групп данных
        int groupCount = 0;
        // Итоговые строки были удалены
        // Неитоговые строки были удалены
        for (int i = 0; i < dataRows.size(); i++) {
            DataRow<Cell> row = dataRows.get(i);
            // строка или итог другой группы после строки без подитога между ними
            if (i > 0) {
                DataRow<Cell> prevRow = dataRows.get(i - 1);
                if (prevRow.getAlias() == null) {
                    // может вернуть true (строки в разных группах), false (строки в одной группе) и null (первая строка не в группе так как групп. графы не заполнены)
                    Boolean isDiffGroup = checkDiffGroup.check(prevRow, row, groupColums);
                    if (Boolean.TRUE.equals(isDiffGroup)) {
                        itogRows.add(groupCount, null);
                        groupCount++;
                        String groupCols = groupString.getString(prevRow);
                        if (groupCols != null) {
                            if (fatal) {
                                logger.error(GROUP_WRONG_ITOG, groupCols);
                            } else {
                                logger.warn(GROUP_WRONG_ITOG, groupCols);
                            }
                        }
                    }
                }
            }
            if (row.getAlias() != null) {
                // итог после итога (или после строки из другой группы)
                if (i < 1 || dataRows.get(i - 1).getAlias() != null || !(Boolean.FALSE.equals(checkDiffGroup.check(dataRows.get(i - 1), row, groupColums)))) {
                    rowLog(logger, row, String.format(GROUP_WRONG_ITOG_ROW, row.getIndex()), logLevel);
                    // удаляем из проверяемых итогов строку без подчиненных строк
                    itogRows.remove(row);
                } else {
                    groupCount++;
                }
            } else {
                // нефиксированная строка и отсутствует последний итог
                if (i == dataRows.size() - 1 && (checkDiffGroup.check(row, row, groupColums) != null)) {
                    itogRows.add(groupCount, null);
                    groupCount++;
                    String groupCols = groupString.getString(row);
                    if (groupCols != null) {
                        if (fatal) {
                            logger.error(GROUP_WRONG_ITOG, groupCols);
                        } else {
                            logger.warn(GROUP_WRONG_ITOG, groupCols);
                        }
                    }
                }
            }
        }
        if (testItogRows.size() == itogRows.size()) {
            for (int i = 0; i < testItogRows.size(); i++) {
                DataRow<Cell> testItogRow = testItogRows.get(i);
                DataRow<Cell> realItogRow = itogRows.get(i);
                if (realItogRow == null) {
                    continue;
                }
                int rowIndex = dataRows.indexOf(realItogRow) - 1;
                DataRow<Cell> row = dataRows.get(rowIndex);
                String groupCols = groupString.getString(row);
                if (groupCols != null) {
                    String checkStr = checkGroupSum.check(testItogRow, realItogRow);
                    if (checkStr != null) {
                        rowLog(logger, row, String.format(GROUP_WRONG_ITOG_SUM, realItogRow.getIndex(), groupCols, checkStr), logLevel);
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
        String name = cell.getColumn().getShortName();
        if (name == null || name.isEmpty()) {
            name = cell.getColumn().getName();
        }
        return name;
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
     * Проверка заголовка импортируемого файла на его (заголовка) существование и соответствие размерности
     *
     * @param headerRows       - список значений в шапке
     * @param referenceColSize - количество ожидаемых столбцов
     * @param referenceRowSize - количество ожидаемых строк
     */
    @SuppressWarnings("unused")
    public static void checkHeaderSize(List<List<String>> headerRows, int referenceColSize, int referenceRowSize) {
        if (headerRows == null || headerRows.isEmpty()) {
            throw new ServiceException(WRONG_HEADER_START);
        }
        int currentColSize = 0;
        for (List<String> row : headerRows) {
            if (row != null) {
                currentColSize = Math.max(row.size(), currentColSize);
            }
        }
        checkHeaderSize(currentColSize, headerRows.size(), referenceColSize, referenceRowSize);
    }

    /**
     * Сравнение строки с эталонной
     */
    public static void checkHeaderEquals(Map<Object, String> headerMapping) {
        checkHeaderEquals(headerMapping, null);
    }

    /**
     * Сравнение строки с эталонной (SBRFACCTAX-11930)
     */
    public static void checkHeaderEquals(ArrayList<Map<Object, String>> headerMapping, Logger logger) {
        for (Map<Object, String> currentString : headerMapping) {
            checkHeaderEquals(currentString, logger);
        }
    }

    /**
     * Сравнение строки с эталонной для всего набора строк (если передан Logger)
     */
    public static void checkHeaderEquals(Map<Object, String> headerMapping, Logger logger) {
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

            if (logger == null) {
                throw new ServiceException(WRONG_HEADER_EQUALS, s2, s1);
            }
            logger.error(WRONG_HEADER_EQUALS, s2, s1);
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
            throw new IllegalArgumentException("Row alias cannot be null");
        }
        for (DataRow<Cell> row : dataRows) {
            if (rowAlias.equals(row.getAlias())) {
                return row;
            }
        }
        throw new IllegalArgumentException("Wrong row alias requested: " + rowAlias);
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
        if (dataRows == null || dataRows.isEmpty()) {
            return; // нечего вычислять
        }
        Integer totalRowIndex = totalRow.getIndex(); // totalRowIndex необходим для вывода сообщений в Cell.setValue
        if (totalRowIndex == null) {
            DataRow<Cell> lastRow = dataRows.get(dataRows.size() - 1);
            if (lastRow != null) {
                totalRowIndex = lastRow.getIndex() == null ? 1 : lastRow.getIndex() + 1;
            }
        }

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
            totalRow.getCell(alias).setValue(sum, totalRowIndex);
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
                        logger.error("%s", msg);
                    } else {
                        logger.warn("%s", msg);
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

        if (subAliases.isEmpty())
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
                        logger.error("%s", msg);
                    } else {
                        logger.warn("%s", msg);
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
            throw new ServiceException(WRONG_FILE_EXTENSION, "xls/xlsx/xlsm");
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
    public static GPathResult getTransportXML(BufferedInputStream inputStream, ImportService importService, String fileName, final int columnCount, final int totalCount) {
        checkBeforeGetXml(inputStream, fileName);

        if (!fileName.endsWith(".rnu")) {
            throw new ServiceException(WRONG_FILE_EXTENSION, "rnu");
        }

        String xmlString;
        try {
            xmlString = importService.getData(inputStream, fileName, "cp866");
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }
/**
 * TODO SAX почему-то менее производителен чем DOM - лучше перепроверить
 DefaultHandler handler = new DefaultHandler() {
 // в файле rnu по умолчанию пропускаются первые две строки
 int rowIndex = 2;
 boolean isBeginTotalRows = false;
 int rowTotalCount = 0;

 @Override public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
 if (qName.equalsIgnoreCase("row")) {
 rowIndex++;
 if (attributes.getIndex("count") != -1) {
 Integer count = Integer.valueOf(attributes.getValue("count"));
 if (count != columnCount + 2) {
 throw new ServiceException(ROW_FILE_WRONG, rowIndex);
 }
 }
 return;
 } else if (!isBeginTotalRows) {
 isBeginTotalRows = true;
 // после строк с данными через одну пустую должны быть итоги
 rowIndex += 2;
 }
 // проверка на итоги идет только если они ожидаются
 if (totalCount != 0) {
 if (qName.equalsIgnoreCase("rowTotal")) {
 rowTotalCount++;
 rowIndex++;
 if (attributes.getIndex("count") != -1) {
 Integer count = Integer.valueOf(attributes.getValue("count"));
 if (count != columnCount + 2) {
 throw new ServiceException(ROW_FILE_WRONG, rowIndex);
 }
 }
 }
 }
 }

 @Override public void endDocument() throws SAXException {
 // сверяем кол-во строк итогов
 if (totalCount != 0 && rowTotalCount != totalCount) {
 throw new ServiceException(ROW_FILE_WRONG, rowIndex);
 }
 }
 };

 try {
 SAXParserFactory factory = SAXParserFactory.newInstance();
 SAXParser saxParser = factory.newSAXParser();
 InputSource inputSource = new InputSource();
 inputSource.setCharacterStream(new StringReader(xmlString));
 // парсим xml
 saxParser.parse(inputSource, handler);
 } catch (Exception e) {
 throw new ServiceException(e.getMessage());
 }
 */
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
            rowIndex += 1;

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

    /**
     * Проверка загружаемого ТФ:
     * 1. имя файла задано;
     * 2. поток данных не пуст;
     * 3. имя файла заканчивается на ".rnu"
     */
    public static void checkTF(BufferedInputStream inputStream, String fileName) {
        checkBeforeGetXml(inputStream, fileName);
        if (fileName != null && !fileName.toLowerCase().endsWith(".rnu")) {
            throw new ServiceException(WRONG_FILE_EXTENSION, "rnu");
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
        if (value != null && valueExpected != null && !value.equalsIgnoreCase(valueExpected) ||
                valueExpected != null && !"".equals(valueExpected) && !valueExpected.equalsIgnoreCase(value) ||
                value != null && !"".equals(value) && valueExpected == null) {
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
     * Вывод в логгер сообщений об ошибках, хранящихся в ячейках строк
     *
     * @param rows   интересующие строки с сообщениями в ячейках
     * @param logger логгер для вывода сообщений
     */
    public static void showMessages(List<DataRow<Cell>> rows, Logger logger) {
        for (DataRow<Cell> row : rows) {
            for (String cellAlias : row.keySet()) {
                String msg = row.getCell(cellAlias).getMessage();
                if (msg != null && !msg.isEmpty()) {
                    rowError(logger, row, msg);
                }
            }
        }
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
     * Получить индекс формата, для форматирования даты в Excel2007 в формат ДД.ММ.ГГГГ
     * Пример применения результата:
     * cellStyle.setDataFormat(createXlsDateFormat(workbook));
     * cell.setCellValue(new Date());
     * cell.setCellStyle(cellStyle);
     *
     * @param workbook объект книги xlsx
     * @return код формата по спецификации OpenXML
     */
    @SuppressWarnings("unused")
    public static short createXlsDateFormat(Workbook workbook) {
        String excelFormatPattern = DateFormatConverter.getJavaDatePattern(DateFormat.DEFAULT, new Locale("ru"));
        DataFormat poiFormat = workbook.createDataFormat();
        return poiFormat.getFormat(excelFormatPattern);
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

    /**
     * Проверка превышения допустимой разрядности
     */
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

    /**
     * Для обработки xlsx файла: проверка потока данных, имени файла, расширения, чтение файла, сбор данных.
     *
     * @param inputStream     потом данных
     * @param uploadFileName  имя файла
     * @param allValues       список для хранения списков значении каждой строки данных
     * @param headerValues    список для хранения списков значении каждой строки шапки таблицы
     * @param tableStartValue начальное значение, с которого начинается сбор данных
     * @param tableEndValue   конечное значение, с которого прекращается сбор данных
     * @param headerRowCount  количество строк в шапке таблицы
     * @param paramsMap       мапа с параметрами (rowOffset отступ сверху, colOffset отступ слева)
     */
    public static void checkAndReadFile(InputStream inputStream, String uploadFileName,
                                        List<List<String>> allValues, List<List<String>> headerValues,
                                        String tableStartValue, String tableEndValue, int headerRowCount,
                                        Map<String, Object> paramsMap) throws IOException, OpenXML4JException, SAXException {
        // проверка потока данных, имени файла, расширения
        if (inputStream == null) {
            throw new ServiceException(EMPTY_INPUT_STREAM);
        }
        String fileName = (uploadFileName != null ? uploadFileName.toLowerCase() : null);
        if (fileName == null || fileName.equals("")) {
            throw new ServiceException(EMPTY_FILE_NAME);
        }
        if (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xlsm")) {
            throw new ServiceException(WRONG_FILE_EXTENSION, "xlsx/xlsm");
        }
        OPCPackage pkg = null;
        XSSFReader r = null;
        StylesTable styles = null;
        SharedStringsTable sst = null;
        XMLReader parser = null;
        ContentHandler handler = null;
        InputStream sheet1 = null;
        InputSource sheetSource = null;

        try {
            // получение строк из файла (из первого листа)
            pkg = OPCPackage.open(inputStream);
            r = new XSSFReader(pkg);
            styles = r.getStylesTable();
            sst = r.getSharedStringsTable();
            parser = XMLReaderFactory.createXMLReader();
            // обработчик
            handler = new SheetHandler(sst, styles, allValues, headerValues, tableStartValue, tableEndValue, headerRowCount, paramsMap);
            parser.setContentHandler(handler);
            // обработать первый лист в книге
            sheet1 = r.getSheetsData().next();
            sheetSource = new InputSource(sheet1);
            parser.parse(sheetSource);
        } catch (InvalidFormatException e) {
            throw new ServiceException(WRONG_FILE_FORMAT);
        } finally {
            // освобождение ресурсов для экономии памяти
            if (sheet1 != null) {
                sheet1.close();
            }
            sheet1 = null;
            sheetSource = null;
            parser = null;
            handler = null;
            sst = null;
            styles = null;
            r = null;
            if (pkg != null) {
                pkg.close();
            }
            pkg = null;
        }
    }

    /**
     * Работает аналогично {@link ScriptUtils#checkAndReadFile(InputStream, String, List, List, String, String, int, Map)}
     * Однако читает данные не с первого листа, а с диапазона листов.
     *
     * @param file             файл xlsx
     * @param allValues        список для хранения списков значении каждой строки данных
     * @param headerValues     список для хранения списков значении каждой строки шапки таблицы
     * @param headerStartValue значение с которого начинается заголовок
     * @param headerRowCount   количество строк в шапке таблицы
     * @param paramsMap        мапа с параметрами (rowOffset отступ сверху, colOffset отступ слева)
     * @param startSheetIndex  индекс начального листа, не должен быть null
     * @param endSheetIndex    индекс последнего листа включительно. Может быть равен null, тогда будут обрабатываться
     *                         все листы с индексом больше начального
     * @throws IOException
     * @throws OpenXML4JException
     * @throws SAXException
     */
    public static void readSheetsRange(File file, List<List<String>> allValues, List<List<String>> headerValues,
                                       String headerStartValue, int headerRowCount,
                                       Map<String, Object> paramsMap, Integer startSheetIndex, Integer endSheetIndex) throws IOException, OpenXML4JException, SAXException {
        if (startSheetIndex == null) {
            throw new IllegalArgumentException("Начальный индекс не должен быть равен null");
        }

        OPCPackage pkg = OPCPackage.open(file.getAbsolutePath(), PackageAccess.READ);
        XSSFReader r = new XSSFReader(pkg);
        SharedStringsTable sst = r.getSharedStringsTable();
        XMLReader parser = XMLReaderFactory.createXMLReader();
        StylesTable styles = r.getStylesTable();
        ContentHandler handler = new MultiHeaderSheetWorkbookHandler(sst, styles, allValues, headerValues, headerStartValue, headerRowCount, paramsMap);
        parser.setContentHandler(handler);
        Iterator<InputStream> sheets = r.getSheetsData();
        int i = 0;
        while (sheets.hasNext()) {
            if (i >= startSheetIndex) {
                if (endSheetIndex == null || endSheetIndex <= i) {
                    InputStream sheet = sheets.next();
                    InputSource sheetSource = new InputSource(sheet);
                    parser.parse(sheetSource);
                }
            } else {
                sheets.next();
            }
            i++;
        }
    }

    /**
     * Проставление индексов у набора строк
     *
     * @param dataRows
     */
    @SuppressWarnings("unused")
    public static void updateIndexes(List<DataRow<Cell>> dataRows) {
        int index = 1;
        for (DataRow<Cell> row : dataRows) {
            row.setIndex(index++);
        }
    }

    /**
     * Проверка контрольной суммы ИНН
     *
     * @param logger логер для записи сообщения
     * @param row    строка НФ
     * @param alias  псевдоним столбца
     * @param value  значение ИНН
     * @param fatal  фатально ли сообщение
     * @return
     */
    public static boolean checkControlSumInn(Logger logger, DataRow<Cell> row, String alias, String value, boolean fatal) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        if (!RefBookUtils.checkControlSumInn(value)) {
            rowLog(logger, row, (row != null ? ("Строка " + row.getIndex() + ": ") : "") + String.format("Вычисленное контрольное число по полю \"%s\" некорректно (%s).", getColumnName(row, alias), value), fatal ? LogLevel.ERROR : LogLevel.WARNING);
            return false;
        }
        return true;
    }

    /**
     * Проверка контрольной суммы ИНН
     *
     * @param value значение ИНН
     * @return
     */
    public static boolean checkControlSumInn(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        if (!RefBookUtils.checkControlSumInn(value)) {
            return false;
        }
        return true;
    }

    /**
     * Проверка диапазона даты (от 1991 до 2099)
     *
     * @param logger логер для записи сообщения
     * @param row    строка НФ
     * @param alias  псевдоним столбца
     * @param value  проверяемое значение (строка или дата)
     * @param fatal
     */
    public static boolean checkDateValid(Logger logger, DataRow<Cell> row, String alias, Object value, boolean fatal) {
        if (value == null) {
            return false;
        }
        Integer year = null;
        Date date = null;
        if (value instanceof Date) {
            date = (Date) value;
        }
        try {
            if (value instanceof String) {
                date = parseDate("dd.MM.yyyy", (String) value);
            }
            String yearString = formatDate(date, "yyyy");
            if (yearString != null) {
                year = Integer.valueOf(yearString);
            }
        } catch (ParseException e) {
            rowLog(logger, row, (row != null ? ("Строка " + row.getIndex() + ": ") : "") + String.format("Ошибка при разборе значения даты атрибута «%s» (%s)", getColumnName(row, alias), value), fatal ? LogLevel.ERROR : LogLevel.WARNING);
        }
        if (year == null || year < 1991 || year > 2099) {
            rowLog(logger, row, (row != null ? ("Строка " + row.getIndex() + ": ") : "") + String.format("Значение даты атрибута «%s» должно принимать значение из следующего диапазона: 01.01.1991 - 31.12.2099", getColumnName(row, alias)), fatal ? LogLevel.ERROR : LogLevel.WARNING);
            return false;
        }
        return true;
    }

    public static boolean checkPattern(Logger logger, DataRow<Cell> row, String alias, String value, String pattern, String meaning, boolean fatal) {
        return checkPattern(logger, row, alias, value, Arrays.asList(pattern), Arrays.asList(meaning), fatal);
    }

    /**
     * Проверка текста на паттерны
     *
     * @param logger   логер для записи сообщения
     * @param row      строка НФ
     * @param alias    псевдоним столбца
     * @param value    проверяемое значение (строка или дата)
     * @param patterns regExp для проверки
     * @param fatal
     */
    public static boolean checkPattern(Logger logger, DataRow<Cell> row, String alias, String value, List<String> patterns, List<String> meanings, boolean fatal) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        boolean result = false;
        for (String pattern : patterns) {
            if (!result) {
                result = checkFormat(value, pattern);
            }
            if (patterns.indexOf(pattern) != 0) {
                sb.append("\" / \"");
            }
            sb.append(pattern);
        }
        if (!result) {
            rowLog(logger, row, (row != null ? ("Строка " + row.getIndex() + ": ") : "") + String.format("Атрибут \"%s\" заполнен неверно (%s)! Ожидаемый паттерн: \"%s\"", getColumnName(row, alias), value, sb.toString()), fatal ? LogLevel.ERROR : LogLevel.WARNING);
            if (meanings != null) {
                for (String meaning : meanings) {
                    if (meaning != null && !meaning.isEmpty()) {
                        int index = meanings.indexOf(meaning);
                        if (patterns.size() > index) {
                            rowLog(logger, row, (row != null ? ("Строка " + row.getIndex() + ": ") : "") + String.format("Расшифровка паттерна \"%s\": %s", patterns.get(index), meaning), fatal ? LogLevel.ERROR : LogLevel.WARNING);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static void compareTotalValues(DataRow<Cell> total, DataRow<Cell> totalTmp, List<String> columns, Logger logger, boolean required) {
        compareTotalValues(total, totalTmp, columns, logger, 2, required);
    }

    /**
     * Сравнить значения итоговых строк.
     *
     * @param totalRow    итоговая строка нф
     * @param totalRowTmp итоговая строка с посчитанными значениям
     * @param columns     список алиасов итоговых графов
     * @param logger      для вывода сообщении
     * @param precision   точность значении (для BigDecimal есть различия в точности после запятой, например 1.0 не равно 1.00)
     * @param required    фатальность
     */
    public static void compareTotalValues(DataRow<Cell> totalRow, DataRow<Cell> totalRowTmp, List<String> columns,
                                          Logger logger, int precision, boolean required) {
        if (totalRow == null || totalRowTmp == null || columns == null || columns.isEmpty()) {
            return;
        }
        for (String alias : columns) {
            BigDecimal value1 = totalRow.getCell(alias).getNumericValue();
            BigDecimal value2 = totalRowTmp.getCell(alias).getNumericValue();
            if (value2 == null) {
                value2 = BigDecimal.ZERO;
            }
            if (value1 != null) {
                value1 = round(value1, precision);
            }
            value2 = round(value2, precision);
            if (!value2.equals(value1)) {
                String msg;
                if (value1 == null) {
                    msg = String.format(COMPARE_TOTAL_VALUES_NULL, totalRow.getImportIndex(), getColumnName(totalRow, alias), value2);
                } else {
                    msg = String.format(COMPARE_TOTAL_VALUES, totalRow.getImportIndex(), getColumnName(totalRow, alias), value1, value2);
                }
                if (required) {
                    logger.error("%s", msg);
                } else {
                    logger.warn("%s", msg);
                }
            }
        }
    }

    public static BigDecimal round(BigDecimal value) {
        return round(value, 0);
    }

    public static BigDecimal round(BigDecimal value, int precision) {
        if (value == null) {
            return null;
        }
        return (new BigDecimal(value.toString())).setScale(precision, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * @param logger                логер для записи сообщения
     * @param row                   строка НФ
     * @param alias                 псевдоним столбца
     * @param reportPeriodStartDate дата начала периода текущей формы
     * @param reportPeriodEndDate   дата окончания периода текущей формы
     * @param fatal                 фатально ли сообщение
     */
    public static void checkDealDoneDate(Logger logger, DataRow<Cell> row, String alias, Date reportPeriodStartDate,
                                         Date reportPeriodEndDate, boolean fatal) {
        Date dealDoneDate = row.getCell(alias).getDateValue();
        if (dealDoneDate != null && (dealDoneDate.before(reportPeriodStartDate) || dealDoneDate.after(reportPeriodEndDate))) {
            rowLog(logger, row, (row != null ? ("Строка " + row.getIndex() + ": ") : "") +
                    String.format("Дата, указанная в графе «%s» (%s), должна относиться к отчетному периоду текущей формы (%s - %s)!",
                            getColumnName(row, alias), formatDate(dealDoneDate, "dd.MM.yyyy"),
                            formatDate(reportPeriodStartDate, "dd.MM.yyyy"),
                            formatDate(reportPeriodEndDate, "dd.MM.yyyy")
                    ), fatal ? LogLevel.ERROR : LogLevel.WARNING);
        }
    }

    /**
     * Проверка нахождения даты в диапазоне (вариант 1, с датами)
     *
     * @param logger    логер для записи сообщения
     * @param row       строка НФ
     * @param alias     псевдоним столбца
     * @param startDate дата окончания периода текущей формы
     * @param endDate   дата окончания периода текущей формы
     * @param fatal     фатально ли сообщение
     */
    public static void checkDatePeriod(Logger logger, DataRow<Cell> row, String alias, Date startDate, Date endDate, boolean fatal) {
        Date docDate = row.getCell(alias).getDateValue();
        if (docDate != null && (docDate.before(startDate) || docDate.after(endDate))) {
            rowLog(logger, row, String.format(CHECK_DATE_PERIOD,
                    row.getIndex(),
                    getColumnName(row, alias),
                    formatDate(startDate, "dd.MM.yyyy"),
                    formatDate(endDate, "dd.MM.yyyy")
            ), fatal ? LogLevel.ERROR : LogLevel.WARNING);
        }
    }

    /**
     * Проверка нахождения даты в диапазоне (вариант 2, с названиями граф)
     *
     * @param logger     логер для записи сообщения
     * @param row        строка НФ
     * @param alias      псевдоним столбца
     * @param startAlias псевдоним графы даты начала периода
     * @param endDate    дата окончания периода текущей формы
     * @param fatal      фатально ли сообщение
     */
    public static void checkDatePeriod(Logger logger, DataRow<Cell> row, String alias, String startAlias, Date endDate, boolean fatal) {
        Date docDate = row.getCell(alias).getDateValue();
        Date startDate = row.getCell(startAlias).getDateValue();
        if (docDate != null && startDate != null && (docDate.before(startDate) || docDate.after(endDate))) {
            rowLog(logger, row, String.format("Строка %d: Значение графы «%s» должно быть не меньше значения графы «%s» и не больше %s!",
                    row.getIndex(),
                    getColumnName(row, alias),
                    getColumnName(row, startAlias),
                    formatDate(endDate, "dd.MM.yyyy")
            ), fatal ? LogLevel.ERROR : LogLevel.WARNING);
        }
    }

    /**
     * Проверка нахождения даты в диапазоне и сравнение с другой графой
     *
     * @param logger     логер для записи сообщения
     * @param row        строка НФ
     * @param alias      псевдоним столбца
     * @param startAlias псевдоним графы даты начала периода
     * @param endDate    дата окончания периода текущей формы
     * @param fatal      фатально ли сообщение
     */
    public static void checkDatePeriodExt(Logger logger, DataRow<Cell> row, String alias, String startAlias, Date yearStartDate, Date endDate, boolean fatal) {
        // дата проверяемой графы
        Date docDate = row.getCell(alias).getDateValue();
        // дата другой графы
        Date startDate = row.getCell(startAlias).getDateValue();

        if (docDate != null && startDate != null && (docDate.before(yearStartDate) || docDate.after(endDate) || docDate.before(startDate))) {
            rowLog(logger, row, String.format(CHECK_DATE_PERIOD_EXT,
                    row.getIndex(),
                    getColumnName(row, alias),
                    formatDate(yearStartDate, "dd.MM.yyyy"),
                    formatDate(endDate, "dd.MM.yyyy"),
                    getColumnName(row, startAlias)
            ), fatal ? LogLevel.ERROR : LogLevel.WARNING);
        }
    }

    /**
     * Сравнить суммы из транспортного файла с посчитанными суммами и задать значения из строки тф в строку нф.
     *
     * @param totalRow   строка с посчитанными суммами
     * @param totalRowTF строка с суммами из транспортного файла
     * @param columns    список алиасов столбцов
     * @param rowIndex   номер строки файла
     * @param logger     логгер
     * @param isFatal    фатальность - ошибка / предупреждение
     */
    public static void checkAndSetTFSum(DataRow<Cell> totalRow, DataRow<Cell> totalRowTF, List<String> columns, Integer rowIndex, Logger logger, boolean isFatal) {
        if (!logger.containsLevel(LogLevel.ERROR) && totalRowTF != null) {
            // сравнение контрольных сумм
            checkTFSum(totalRow, totalRowTF, columns, rowIndex, logger, isFatal);

            // задать итоговой строке нф значения из итоговой строки тф
            for (String alias : columns) {
                BigDecimal value = totalRowTF.getCell(alias).getNumericValue();
                totalRow.getCell(alias).setValue(value, null);
            }
        } else {
            logger.warn("В транспортном файле не найдена итоговая строка");
            // очистить итоги
            for (String alias : columns) {
                totalRow.getCell(alias).setValue(null, null);
            }
        }
    }

    /**
     * Сравнить суммы из транспортного файла с посчитанными суммами.
     *
     * @param totalRow   строка с посчитанными суммами
     * @param totalRowTF строка с суммами из транспортного файла
     * @param columns    список алиасов столбцов
     * @param rowIndex   номер строки файла
     * @param logger     логгер
     * @param isFatal    фатальность - ошибка / предупреждение
     */
    public static void checkTFSum(DataRow<Cell> totalRow, DataRow<Cell> totalRowTF, List<String> columns, Integer rowIndex, Logger logger, boolean isFatal) {
        for (String alias : columns) {
            BigDecimal v1 = totalRowTF.getCell(alias).getNumericValue();
            BigDecimal v2 = totalRow.getCell(alias).getNumericValue();
            if (v1 == null && v2 == null) {
                continue;
            }
            String msg = null;
            if (v1 == null) {
                // нет значения в тф
                msg = String.format(TRANSPORT_FILE_SUM_ERROR_2, rowIndex, getColumnName(totalRow, alias), v2);
            } else if (v1.compareTo(v2) != 0) {
                // значения расходятся
                msg = String.format(TRANSPORT_FILE_SUM_ERROR_1, rowIndex, getColumnName(totalRow, alias), v1, v2);
            }
            if (msg != null) {
                if (isFatal) {
                    logger.error("%s", msg);
                } else {
                    logger.warn("%s", msg);
                }
            }
        }
    }

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
     * Интерфейс для сравнения принадлежности двух строк к одной группе
     */
    public interface CheckDiffGroup {
        /**
         * Нужно сравнить 1) две нефиксированные и 2) фиксированную и нефиксированную
         */
        Boolean check(DataRow<Cell> row1, DataRow<Cell> row2, List<String> groupColumns);
    }

    static class SheetHandler extends DefaultHandler {
        enum XssfDataType {
            BOOL, ERROR, FORMULA, INLINESTR, SSTINDEX, NUMBER
        }

        XssfDataType dataType;
        SharedStringsTable sst;     // таблица со строковыми значениями (Shared Strings Table)
        StylesTable stylesTable;    // таблица со стилями ячеек
        List<List<String>> allValues;     // список для хранения списков значении каждой строки данных
        List<List<String>> headerValues;  // список для хранения списков значении каждой строки шапки таблицы
        String tableStartValue;     // начальное значение, с которого начинается сбор данных
        String tableEndValue;       // конечное значение, с которого прекращается сбор данных
        int headerRowCount;         // количество строк в шапке таблицы
        Map<String, Object> paramsMap;  // мапа с параметрами
        StringBuffer lastValue;     // последнее считаное значение
        boolean nextIsString;       // признак того что следующее считаное значение хранится в виде строки в sst (Shared Strings Table)
        List<String> rowValues;     // список значении строки из файла
        boolean isData = false;     // признак того что считанные значения относятся к данным
        boolean isHeader = false;   // признак того что считанные значения относятся к шапке таблицы
        boolean endRead = false;    // признак того что встретилось значение окончания таблицы
        String position;            // позиция ячейки (A1, B2, C1 ... AB12)
        int maxColumnCount;         // максимальное количество значении в строке файла (определяется по шапке таблицы - строка с нумерацией столбцов)
        Integer rowOffset;          // отступ сверху (до данных)
        Integer colOffset;          // отступ слева
        int prevRowIndex = 0;       // номер предыдущей строки
        Map<String, XSSFCellStyle> styleMap = new HashMap<String, XSSFCellStyle>();// мапа со стилями
        Map<String, String> lastValuesMap = new HashMap<String, String>();  // мапа со считанными строковыми значениями из sst
        short formatIndex;          // идентификатор формата даты (дата хранится в виде числа)
        String formatString;        // формат даты
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy"); // свой формат дат, что б исключить использование фомратов по умолчанию: н-р d/m/yyyy
        boolean inlineString;

        /**
         * Для обработки листа экселя.
         *
         * @param sst             таблица со строковыми значениями (Shared Strings Table)
         * @param stylesTable     таблица со стилями ячеек
         * @param allValues       список для хранения списков значении каждой строки данных
         * @param headerValues    список для хранения списков значении каждой строки шапки таблицы
         * @param tableStartValue начальное значение, с которого начинается сбор данных
         * @param tableEndValue   конечное значение, с которого прекращается сбор данных
         * @param headerRowCount  количество строк в шапке таблицы
         * @param paramsMap       мапа с параметрами (rowOffset отступ сверху, colOffset отступ слева)
         */
        private SheetHandler(SharedStringsTable sst, StylesTable stylesTable,
                             List<List<String>> allValues, List<List<String>> headerValues,
                             String tableStartValue, String tableEndValue, int headerRowCount, Map<String, Object> paramsMap) {
            this.sst = sst;
            this.stylesTable = stylesTable;
            this.allValues = allValues;
            this.headerValues = headerValues;
            this.tableStartValue = tableStartValue;
            this.tableEndValue = tableEndValue;
            this.headerRowCount = headerRowCount;
            this.paramsMap = paramsMap;

            this.rowOffset = Integer.parseInt(String.valueOf(paramsMap.get("rowOffset")));
            this.colOffset = Integer.parseInt(String.valueOf(paramsMap.get("colOffset")));

            this.lastValue = new StringBuffer();
        }

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) {
            // attributes.getValue("r") - позиция ячейки: A1, B1, ... AC15
            // attributes.getValue("t") - тип ячейки: "s" строка, "str" формула, "n" число
            // attributes.getValue("s") - стиль ячейки
            // name.equals("c")         - ячейка
            // name.equals("row")       - строка
            // name.equals("v")         - значение

            if (endRead) {
                return;
            }
            if (name.equals("c")) { // ячейка
                this.formatIndex = -1;
                this.formatString = null;

                position = attributes.getValue("r");

                dataType = XssfDataType.NUMBER;
                formatIndex = -1;
                formatString = null;

                final String cellType = attributes.getValue("t");
                if ("b".equals(cellType)) {
                    dataType = XssfDataType.BOOL;
                } else if ("e".equals(cellType)) {
                    dataType = XssfDataType.ERROR;
                } else if ("inlineStr".equals(cellType)) {
                    dataType = XssfDataType.INLINESTR;
                } else if ("s".equals(cellType)) {
                    dataType = XssfDataType.SSTINDEX;
                } else if ("str".equals(cellType)) {
                    dataType = XssfDataType.FORMULA;
                }

                String cellStyleStr = attributes.getValue("s");
                if (cellStyleStr != null) {
                    XSSFCellStyle style = getStyle(cellStyleStr);

                    if (dataType == XssfDataType.NUMBER) {
                        this.formatIndex = style.getDataFormat();
                        this.formatString = style.getDataFormatString();
                        if (this.formatString == null) {
                            this.formatString = BuiltinFormats.getBuiltinFormat(this.formatIndex);
                        }
                    }
                }
            } else if (name.equals("row")) { // новая строка
                rowValues = new ArrayList<String>();
            } else if (name.equals("is")) { // inlineString
                inlineString = true;
            }
            lastValue.setLength(0);
        }

        @Override
        public void endElement(String uri, String localName, String name) {
            if (endRead) {
                return;
            }
            if (nextIsString) {
                String v = getLastValue(lastValue.toString());
                lastValue.setLength(0);
                lastValue.append(v);
                nextIsString = false;
            }

            if (name.equals("v") || inlineString && name.equals("t")) { // конец значения
                // добавить отступ: если первое значение таблицы нашлось не в первом столбце, то делается отступ - пропуск лишних столбцов слева
                int columnIndex = getColumnIndex(position);
                if (columnIndex < colOffset) {
                    endRead = (tableEndValue != null && tableEndValue.equals(getValue()));
                    return;
                }
                // добавить отсутствующие/пропущенные ячейки
                if (rowValues.size() < columnIndex - colOffset) {
                    int n = (columnIndex - rowValues.size() - colOffset);
                    for (int i = 1; i <= n; i++) {
                        rowValues.add("");
                    }
                }
                // строка
                rowValues.add(getValue());
            } else if (name.equals("row")) { // конец строки
                if (isData) {
                    endRead = (rowValues != null && rowValues.contains(tableEndValue));
                    if (!endRead) {
                        // еще не конец таблицы - дополнить список значений недостоющеми значениями и добавить ко всем строкам
                        performRowData();
                    }
                } else {
                    if (headerValues.isEmpty() && rowValues != null && (tableStartValue == null || rowValues.contains(tableStartValue))) {
                        // найдено начало таблицы
                        int from = tableStartValue != null ? rowValues.indexOf(tableStartValue) : 0;
                        colOffset = from; // отступ слева
                        if (from > 0) {
                            int to = rowValues.size();
                            rowValues = rowValues.subList(from, to);
                        }
                        isHeader = true;
                    }
                    if (isHeader) {
                        headerValues.add(rowValues);
                        if (headerValues.size() == headerRowCount) {
                            // дальше только данные
                            performRowHeader();
                        }
                    }
                }
            } else if (name.equals("sheetData")) {
                // конец данных - обновить значения переданных параметов для использования в дальнейшем
                paramsMap.put("rowOffset", rowOffset);
                paramsMap.put("colOffset", colOffset + 1);
            } else if (name.equals("is")) {
                inlineString = false;
            }
        }

        void performRowData() {
            if (rowValues != null && rowValues.size() < maxColumnCount) {
                int n = maxColumnCount - rowValues.size();
                for (int i = 1; i <= n; i++) {
                    rowValues.add(null);
                }
            }
            int rowIndex = getRowIndex(position);
            if (rowIndex > prevRowIndex + 1) {
                int n = rowIndex - prevRowIndex - 1;
                for (int i = 1; i <= n; i++) {
                    allValues.add(new ArrayList<String>());
                }
            }
            allValues.add(rowValues);
            prevRowIndex = rowIndex;
        }

        void performRowHeader() {
            isData = true;
            isHeader = false;
            maxColumnCount = (rowValues != null ? rowValues.size() : 0); // максимальное количество значении в строке
            rowOffset = getRowIndex(position); // отступ сверху
            prevRowIndex = getRowIndex(position);
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            lastValue.append(ch, start, length);
        }

        /**
         * Получить номер столбца по значению позиции (A1, B1 ... AB12).
         */
        int getColumnIndex(String position) {
            String onlyColumnName = position.replaceAll("[\\d]+", "");
            return CellReference.convertColStringToIndex(onlyColumnName);
        }

        /**
         * Получить номер строки по значению позиции (A1, B1 ... AB12).
         */
        int getRowIndex(String position) {
            return Integer.parseInt(position.replaceAll("[^\\d]+", ""));
        }

        private XSSFCellStyle getStyle(String cellStyleStr) {
            if (styleMap.get(cellStyleStr) == null) {
                int styleIndex = Integer.parseInt(cellStyleStr);
                styleMap.put(cellStyleStr, stylesTable.getStyleAt(styleIndex));
            }
            return styleMap.get(cellStyleStr);
        }

        String getLastValue(String value) {
            if (lastValuesMap.get(value) == null) {
                int idx = Integer.parseInt(value);
                lastValuesMap.put(value, new XSSFRichTextString(sst.getEntryAt(idx)).toString());
            }
            return lastValuesMap.get(value);
        }

        /**
         * Получить значение в виде строки.
         */
        String getValue() {
            String value;

            switch (dataType) {
                case BOOL:
                    char first = lastValue.charAt(0);
                    value = (first == '0') ? "false" : "true";
                    break;
                case ERROR:
                    System.out.println("Error-cell occurred: " + lastValue);
                    value = lastValue.toString();
                    break;
                case FORMULA:
                    value = lastValue.toString();
                    break;
                case INLINESTR:
                    XSSFRichTextString rtsi = new XSSFRichTextString(lastValue.toString());
                    value = rtsi.toString();
                    break;
                case SSTINDEX:
                    String sstIndex = lastValue.toString();
                    int idx = Integer.parseInt(sstIndex);
                    XSSFRichTextString rtss = new XSSFRichTextString(sst.getEntryAt(idx));
                    value = rtss.toString();
                    break;
                case NUMBER:
                    final String numberString = lastValue.toString();
                    if (formatString != null) {
                        if (DateUtil.isADateFormat(formatIndex, formatString)) {
                            Date date = DateUtil.getJavaDate(Double.parseDouble(numberString));
                            value = simpleDateFormat.format(date);
                        } else {
                            value = (new BigDecimal(NumberToTextConverter.toText(Double.parseDouble(numberString)))).toPlainString();
                        }
                    } else {
                        if (numberString.endsWith(".0")) {
                            // xlsx only stores doubles, so integers get ".0" appended
                            // to them
                            value = numberString.substring(0, numberString.length() - 2);
                        } else {
                            value = (new BigDecimal(NumberToTextConverter.toText(Double.parseDouble(numberString)))).toPlainString();
                        }
                    }
                    break;
                default:
                    System.out.println("Unsupported data type: " + dataType);
                    value = "";
            }

            return cleanString(value);
        }
    }

    /**
     * Обработчик книги в которой таблица находится на нескольких листах и продублирована шапка
     */
    static class MultiHeaderSheetWorkbookHandler extends SheetHandler {

        final String headerStartValue; // значение с которого начинается заголовок

        private MultiHeaderSheetWorkbookHandler(SharedStringsTable sst, StylesTable stylesTable, List<List<String>> allValues, List<List<String>> headerValues, String headerStartValue, int headerRowCount, Map<String, Object> paramsMap) {
            super(sst, stylesTable, allValues, headerValues, null, null, headerRowCount, paramsMap);
            this.headerStartValue = headerStartValue;
        }

        @Override
        public void endElement(String uri, String localName, String name) {
            if (endRead) {
                return;
            }
            if (nextIsString) {
                String v = getLastValue(lastValue.toString());
                lastValue.setLength(0);
                lastValue.append(v);
                nextIsString = false;
            }

            if (name.equals("v") || inlineString && name.equals("t")) { // конец значения
                // добавить отступ: если первое значение таблицы нашлось не в первом столбце, то делается отступ - пропуск лишних столбцов слева
                int columnIndex = getColumnIndex(position);
                if (columnIndex < colOffset) {
                    endRead = (tableEndValue != null && tableEndValue.equals(getValue()));
                    return;
                }
                // добавить отсутствующие/пропущенные ячейки
                if (rowValues.size() < columnIndex - colOffset) {
                    int n = (columnIndex - rowValues.size() - colOffset);
                    for (int i = 1; i <= n; i++) {
                        rowValues.add("");
                    }
                }
                // строка
                rowValues.add(getValue());
            } else if (name.equals("row")) { // конец строки
                if (isData && rowValues != null && !rowValues.isEmpty() && !rowValues.get(0).startsWith(headerStartValue)) {
                    endRead = (rowValues.contains(tableEndValue));
                    if (!endRead) {
                        // еще не конец таблицы - дополнить список значений недостоющеми значениями и добавить ко всем строкам
                        performRowData();
                    }
                } else {
                    isData = false;
                    if (rowValues != null && !rowValues.isEmpty() && rowValues.get(0).startsWith(headerStartValue)) {
                        colOffset = 0;
                        isHeader = true;
                    }
                    if (isHeader) {
                        headerValues.add(rowValues);
                        if (headerValues.size() % headerRowCount == 0) {
                            // дальше только данные
                            performRowHeader();
                        }
                    }
                }
            } else if (name.equals("sheetData")) {
                // конец данных - обновить значения переданных параметов для использования в дальнейшем
                paramsMap.put("rowOffset", rowOffset);
                paramsMap.put("colOffset", colOffset + 1);
            } else if (name.equals("is")) {
                inlineString = false;
            }
        }
    }

    /**
     * Проверка заголовка и второй пустой строки тф.
     */
    public static void checkFirstRowsTF(CSVReader reader, Logger logger) throws IOException {
        // заголовок
        String[] rowCells = reader.readNext();
        if (rowCells != null && isEmptyCells(rowCells)) {
            logger.error("Первой строкой должен идти заголовок, а не пустая строка");
        }
        // пустая строка
        rowCells = reader.readNext();
        if (rowCells != null && !isEmptyCells(rowCells)) {
            logger.error("Вторая строка должна быть пустой");
        }
    }

    public static boolean isEmptyCells(String[] rowCells) {
        return rowCells.length == 1 && "".equals(rowCells[0]);
    }

    /**
     * Проверка корректности СНИЛС
     */
    public static boolean checkSnils(String snils) {
        if (snils == null) {
            return false;
        }

        String number = snils.replaceAll("\\D", "");

        if (number.length() != 11) {
            return false;
        }

        Integer firstValue;
        Integer secondValue;

        try {
            firstValue = Integer.valueOf(number.substring(0, 9));
            secondValue = Integer.valueOf(number.substring(9, 11));
        } catch (NumberFormatException numberFormatException) {
            return false;
        }

        if (firstValue <= 1001998) {
            return true;
        }

        Integer controlSumm =
                9 * Integer.valueOf(number.substring(0, 1))
                        + 8 * Integer.valueOf(number.substring(1, 2))
                        + 7 * Integer.valueOf(number.substring(2, 3))
                        + 6 * Integer.valueOf(number.substring(3, 4))
                        + 5 * Integer.valueOf(number.substring(4, 5))
                        + 4 * Integer.valueOf(number.substring(5, 6))
                        + 3 * Integer.valueOf(number.substring(6, 7))
                        + 2 * Integer.valueOf(number.substring(7, 8))
                        + Integer.valueOf(number.substring(8, 9));

        Integer controlModSum = controlSumm % 101;

        if (controlModSum.equals(100)) {
            return secondValue.equals(0);
        }

        return secondValue.equals(controlSumm % 101);
    }

    /**
     * Проверка корректности ДУЛ
     */
    public static boolean checkDul(String dul) {
        if (dul == null) {
            return false;
        }

        if (!checkFormat(dul, DUL_REGEXP)) {
            return false;
        }

        return true;
    }

    /**
     * Сравнение двух значений справочника, производится преобразование числовых значений a к типу сравниваемого значения b (Integer или Long) , так как в RefBookValue все числа хранятся как BigDecimal
     * Ограничение: в текущей реализации нереализовано сравнение чисел с плавающей точкой
     *
     * @return
     */
    public static boolean equalsNullSafe(Object a, Object b) {
        boolean result = false;
        if (a == null && b == null) {
            result = true;
        } else if (a != null && b != null) {
            if (a instanceof Number) {
                Number anum = (Number) a;
                if (b instanceof Integer) {
                    return isEquals(anum.intValue(), b);
                } else if (b instanceof Long) {
                    return isEquals(anum.longValue(), b);
                } else {
                    throw new UnsupportedOperationException("The method 'equalsNullSafe' is not supported for arguments type " + a.getClass() + " and " + b.getClass());
                }
            } else {
                result = a.equals(b);
            }
        } else {
            result = false;
        }
        return result;
    }

    private static boolean isEquals(Object a, Object b) {
        return a.equals(b);
    }


    /**
     * Проверка заполнения графы, значение считаться незаполненным в том числе, если в ней указан "0"
     *
     * @param value
     * @return
     */
    public static boolean isEmpty(Object value) {

        if (value == null) {
            return true;
        }

        if (value instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal) value;
            return bigDecimal.compareTo(BigDecimal.ZERO) == 0;
        } else if (value instanceof Integer) {
            Number number = (Number) value;
            return number.intValue() == 0;
        } else if (value instanceof Long) {
            Number number = (Number) value;
            return number.intValue() == 0;
        }
        if (value instanceof String) {
            String string = (String) value;
            return string.isEmpty();
        } else {
            throw new UnsupportedOperationException("The method 'isEmpty' is not supported for arguments type " + value.getClass());
        }
    }

    public static void checkInterrupted() {
        if (Thread.interrupted()) {
            LOG.info("Thread " + Thread.currentThread().getName() + " was interrupted");
            throw new TAInterruptedException();
        }
    }

    /**
     * Проверка ИНН физического лица
     *
     * @param innValue
     * @return
     * @see <a href="https://conf.aplana.com/pages/viewpage.action?pageId=27182953>Проверка ИНН физического лица</a>
     */
    public static String checkInn(String innValue) {
        if (innValue != null) {
            if (innValue.length() != 12) {
                return "Значение гр. \"ИНН в РФ\" (\"" + innValue + "\") должно содержать 12 символов";
            }
            if (!checkFormat(innValue, "[0-9]{12}")) {
                return "Значение гр. \"ИНН в РФ\" (\"" + innValue + "\") должно содержать только цифры";
            }
            if (Arrays.asList("00", "90", "93", "94", "95", "96", "98").contains(innValue.substring(0, 2))) {
                return "Значение гр. \"ИНН в РФ\" (\"" + innValue + "\") некорректно. Первые два разряда ИНН не могут быть равны одному из значений: \"00\",\"90\",\"93\",\"94\",\"95\",\"96\",\"98\", может быть отказано в приеме";
            }
            if (!RefBookUtils.checkControlSumInn(innValue)) {
                return "Некорректное контрольное число в значении гр. \"ИНН в РФ\" (\"" + innValue + "\")";
            }
        }
        return null;
    }

    public static String checkName(String value, String attrName) {
        if (value != null && !value.isEmpty()) {
            if (Arrays.asList(" ", "ь", "ъ", "-", ".", "'").contains(value.substring(0, 1).toLowerCase())) {
                return "Значение гр. \"" + attrName + "\" (\"" + value + "\") не должно начинаться с символов \"Ъ\", \"Ь\", дефис, точка, апостроф и пробел. Может быть отказано в приеме";
            }
            if (!checkFormat(value, "^[а-яА-ЯёЁ -]+")) {
                return "Значение гр. \"" + attrName + "\" (\"" + value + "\") содержит недопустимые символы. Значение может содержать только буквы русского алфавита (кириллица), пробелы и дефисы";
            }
        }
        return null;
    }

    public static String checkDul(String code, String value, String attrName) {
        String format = null;
        String formatStr = null;
        String zeroFormat = null;
        if (code.equals("01")) {
            format = "[^\\wА-яа-яЁё]*([IVXLC][^\\wА-яа-яЁё]*)([А-ЯЁ][^\\wА-яа-яЁё]*){2}([0-9][^\\wА-яа-яЁё]*){6}";
            formatStr = "\"R-ББ 999999\", где R - римское число, заданное символами \"I\", \"V\", \"X\", \"L\", \"C\", набранными на верхнем регистре латинской клавиатуры; Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная). Представление римских чисел только через латинскую клавиатуру";
        } else if (code.equals("02")) {
            format = "[^\\wА-яа-яЁё]*([0-9][^\\wА-яа-яЁё]*){8,9}";
            formatStr = "\"99 0999999\", где 9 - любая десятичная цифра (обязательная), 0 - любая десятичная цифра (необязательная, может отсутствовать)";
        } else if (code.equals("04")) {
            format = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}([0-9][^\\wА-яа-яЁё]*){6,7}";
            formatStr = "\"ББ 0999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная), 0 - любая десятичная цифра (необязательная, может отсутствовать)";
        } else if (code.equals("06")) {
            format = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}([0-9][^\\wА-яа-яЁё]*){6}";
            formatStr = "\"ББ 999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная)";
        } else if (code.equals("09")) {
            format = "[^\\wА-яа-яЁё]*([0-9][^\\wА-яа-яЁё]*){9}";
            formatStr = "\"99 9999999\", где 9 - любая десятичная цифра (обязательная)";
        } else if (code.equals("19")) {
            format = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}([0-9][^\\wА-яа-яЁё]*){10}";
            formatStr = "\"ББ-999 9999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная)";
        } else if (code.equals("21")) {
            format = "[^\\wА-яа-яЁё]*([0-9][^\\wА-яа-яЁё]*){10}";
            formatStr = "\"99 99 999999\", где 9 - любая десятичная цифра (обязательная)";
            zeroFormat = "[^\\wА-яа-яЁё]*(0[^\\wА-яа-яЁё]*){10}";
        } else if (code.equals("22")) {
            format = "[^\\wА-яа-яЁё]*([0-9][^\\wА-яа-яЁё]*){9}";
            formatStr = "\"99 9999999\", где 9 - любая десятичная цифра (обязательная)";
        } else if (code.equals("07")) {
            format = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}([0-9][^\\wА-яа-яЁё]*){6,7}";
            formatStr = "\"ББ 0999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная), 0 - любая десятичная цифра (необязательная, может отсутствовать)";
            zeroFormat = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}(0[^\\wА-яа-яЁё]*){6,7}";
        } else if (code.equals("18")) {
            format = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}([0-9][^\\wА-яа-яЁё]*){10}";
            formatStr = "\"ББ-999 9999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная)";
            zeroFormat = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}(0[^\\wА-яа-яЁё]*){10}";
        } else if (code.equals("24")) {
            format = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}([0-9][^\\wА-яа-яЁё]*){7}";
            formatStr = "\"ББ 9999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная)";
            zeroFormat = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}(0[^\\wА-яа-яЁё]*){7}";
        } else if (code.equals("26")) {
            format = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}([0-9][^\\wА-яа-яЁё]*){6,7}";
            formatStr = "\"ББ 0999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная), 0 - любая десятичная цифра (необязательная, может отсутствовать)";
        } else if (code.equals("27")) {
            format = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}([0-9][^\\wА-яа-яЁё]*){6,7}";
            formatStr = "\"ББ 0999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная), 0 - любая десятичная цифра (необязательная, может отсутствовать)";
        }
        if (format != null && !checkFormat(value, format)) {
            return "Значение гр. \"" + attrName + "\" (\"" + value + "\") не соответствует формату " + formatStr;
        }
        if (zeroFormat != null && checkFormat(value, zeroFormat)) {
            return "Значение гр. \"" + attrName + "\" (\"" + value + "\") не должно быть нулевым";
        }
        return null;
    }

    public static String formatDocNumber(String code, String value) {
        StringBuilder formattedValue = new StringBuilder(value);
        switch (code) {
            case "21": {
                return formattedValue.insert(2, " ")
                        .insert(5, " ").toString();
            }
            case "07": {
                return formattedValue.insert(2, " ").toString();
            }
            case "18": {
                return formattedValue.insert(2, "-")
                        .insert(6, " ").toString();
            }
            case "24": {
                return formattedValue.insert(2, " ").toString();
            }
        }
        return value;
    }

    ;

    /**
     * Проверяет соответствие ДУЛ формату с удаленными разделителями.
     *
     * @param code
     * @param value
     * @return
     */
    public static boolean checkDulSymbols(String code, String value) {
        boolean pass = true;
        String format = null;
        if (code.equals("21")) {
            format = "[0-9]{10}";
        } else if (code.equals("07")) {
            format = "[А-ЯЁ]{2}[0-9]?[0-9]{6}";
        } else if (code.equals("18")) {
            format = "[А-ЯЁ]{2}[0-9]{10}";
        } else if (code.equals("24")) {
            format = "[А-ЯЁ]{2}[0-9]{7}";
        }
        if (!checkFormat(value, format)) {
            pass = false;
        }
        return pass;
    }

    public static String calcTimeMillis(long time) {
        long currTime = System.currentTimeMillis();
        return (currTime - time) + " мс)";
    }

    public static String formatDate(Object date) {
        if (date instanceof LocalDateTime) {
            return ((LocalDateTime) date).toString(DATE_FORMAT);
        } else {
            return ScriptUtils.formatDate((Date) date, DATE_FORMAT);
        }
    }

    /**
     * Получить форму слова первого склонения в зависимости от стоящего рядом со словом числительным.
     *
     * @param word    Слово первого склонения в именительном падеже
     * @param numeric Числительное
     * @return Слово в полученное в аргументе в нужном падеже в зависимости от числительного. Работает не со всеми
     * словами данной категории. А только заканчивающимися в иминительном падеже множественного числа на ы.
     */
    public static String getFirstDeclensionByNumeric(String word, int numeric) {
        int res = numeric % 10;
        String numberAsString = String.valueOf(numeric);
        for (String teenNumber : Arrays.asList("11", "12", "13", "14")) {
            if (numberAsString.endsWith(teenNumber)) {
                return word.substring(0, word.length() - 1);
            }
        }
        if (Arrays.asList(5, 6, 7, 8, 9, 0).contains(res)) {
            return word.substring(0, word.length() - 1);
        }
        if (Arrays.asList(2, 3, 4).contains(res)) {
            return word.substring(0, word.length() - 1) + "ы";
        }
        return word;
    }

    /**
     * Рассчитывает UUID для объекта дохода на основе состояния полей объекта операции дохода. Данное значение UUID будет являться
     * отпечатком объекта. Архитектура UUID основана на стандарте RFC 4122. По этому стандарту UUID моет быть использована в т.ч для хеширования:
     * Мы используем версию 3 UUID - это значит что UUID генерируется на основе значения с использованием алгоритма MD5.
     * В этом методе из набора полей объекта дохода строится массив байтов. И из этого массива байтов вычисляется отпечаток,
     * который будет эквивалентен для одинаковых массивов байтов и различаться для разных массивов байтов.
     * @param income объект операции дохода
     * @return строковое представление UUID объекта дохода
     */
    public static String getConsolidationIncomeUUID(ConsolidationIncome income) {
        int size = 0;
        byte[] inp = stringToByteArray(income.getInp());
        size += inp.length;
        byte[] incomeCode = stringToByteArray(income.getIncomeCode());
        size += incomeCode.length;
        byte[] incomeType = stringToByteArray(income.getIncomeType());
        size += incomeType.length;
        byte[] incomeAccruedDate = dateToByteArray(income.getIncomeAccruedDate());
        size += incomeAccruedDate.length;
        byte[] incomePayoutDate = dateToByteArray(income.getIncomePayoutDate());
        size += incomePayoutDate.length;
        byte[] kpp = stringToByteArray(income.getKpp());
        size += kpp.length;
        byte[] oktmo = stringToByteArray(income.getOktmo());
        size += oktmo.length;
        byte[] incomeAccruedSumm = bigDecimalToByteArray(income.getIncomeAccruedSumm());
        size += incomeAccruedSumm.length;
        byte[] incomePayoutSumm = bigDecimalToByteArray(income.getIncomePayoutSumm());
        size += incomePayoutSumm.length;
        byte[] totalDeductionsSumm = bigDecimalToByteArray(income.getTotalDeductionsSumm());
        size += totalDeductionsSumm.length;
        byte[] taxBase = bigDecimalToByteArray(income.getTaxBase());
        size += taxBase.length;
        byte[] taxRate = intToByteArray(income.getTaxRate());
        size += taxRate.length;
        byte[] taxDate = dateToByteArray(income.getTaxDate());
        size += taxDate.length;
        byte[] calculatedTax = bigDecimalToByteArray(income.getCalculatedTax());
        size += calculatedTax.length;
        byte[] withHoldingTax = bigDecimalToByteArray(income.getWithholdingTax());
        size += withHoldingTax.length;
        byte[] notHoldingTax = bigDecimalToByteArray(income.getNotHoldingTax());
        size += notHoldingTax.length;
        byte[] overholdingTax = bigDecimalToByteArray(income.getOverholdingTax());
        size += overholdingTax.length;
        byte[] refoundTax = longToByteArray(income.getRefoundTax());
        size += refoundTax.length;
        byte[] taxTransferDate = dateToByteArray(income.getTaxTransferDate());
        size += taxTransferDate.length;
        byte[] paymentDate = dateToByteArray(income.getPaymentDate());
        size += paymentDate.length;
        byte[] paymentNumber = stringToByteArray(income.getPaymentNumber());
        size += paymentNumber.length;
        byte[] taxSumm = longToByteArray(income.getTaxSumm());
        size += taxSumm.length;

        int offset = 0;
        byte[] resultArray = new byte[size];
        mergeByteArrays(inp, resultArray, offset);
        offset += inp.length;
        mergeByteArrays(incomeCode, resultArray, offset);
        offset += incomeCode.length;
        mergeByteArrays(incomeType, resultArray, offset);
        offset += incomeType.length;
        mergeByteArrays(incomeAccruedDate, resultArray, offset);
        offset += incomeAccruedDate.length;
        mergeByteArrays(incomePayoutDate, resultArray, offset);
        offset += incomePayoutDate.length;
        mergeByteArrays(kpp, resultArray, offset);
        offset += kpp.length;
        mergeByteArrays(oktmo, resultArray, offset);
        offset += oktmo.length;
        mergeByteArrays(incomeAccruedSumm, resultArray, offset);
        offset += incomeAccruedSumm.length;
        mergeByteArrays(incomePayoutSumm, resultArray, offset);
        offset += incomePayoutSumm.length;
        mergeByteArrays(totalDeductionsSumm, resultArray, offset);
        offset += totalDeductionsSumm.length;
        mergeByteArrays(taxBase, resultArray, offset);
        offset += taxBase.length;
        mergeByteArrays(taxRate, resultArray, offset);
        offset += taxRate.length;
        mergeByteArrays(taxDate, resultArray, offset);
        offset += taxDate.length;
        mergeByteArrays(calculatedTax, resultArray, offset);
        offset += calculatedTax.length;
        mergeByteArrays(withHoldingTax, resultArray, offset);
        offset += withHoldingTax.length;
        mergeByteArrays(notHoldingTax, resultArray, offset);
        offset += notHoldingTax.length;
        mergeByteArrays(overholdingTax, resultArray, offset);
        offset += overholdingTax.length;
        mergeByteArrays(refoundTax, resultArray, offset);
        offset += refoundTax.length;
        mergeByteArrays(taxTransferDate, resultArray, offset);
        offset += taxTransferDate.length;
        mergeByteArrays(paymentDate, resultArray, offset);
        offset += paymentDate.length;
        mergeByteArrays(paymentNumber, resultArray, offset);
        offset += paymentNumber.length;
        mergeByteArrays(taxSumm, resultArray, offset);

        return UUID.nameUUIDFromBytes(resultArray).toString();
    }

    /**
     * Преобразует дату в массив байтов на основе её long значения
     *
     * @param d дата
     * @return массив байтов
     */
    private static byte[] dateToByteArray(Date d) {
        if (d != null) {
            return longToByteArray(d.getTime());
        }
        return new byte[]{(byte) 0xFF};
    }

    /**
     * Преобразует число {@code BigDecimal} в массив байтов на основе его String представления
     *
     * @param d число
     * @return массив байтов
     */
    private static byte[] bigDecimalToByteArray(BigDecimal d) {
        if (d != null) {
            return stringToByteArray(d.toString());
        }
        return new byte[]{(byte) 0xFF};
    }

    /**
     * Преобразует строку в массив байтов
     *
     * @param s строка
     * @return массив байтов
     */
    private static byte[] stringToByteArray(String s) {
        if (s != null) {
            return s.toLowerCase().getBytes(Charset.forName("UTF-8"));
        }
        return new byte[]{(byte) 0xFF};
    }

    /**
     * Преобразует число long в массив байтов, для каждый из 8 байтов числа помещается в массив
     *
     * @param l число
     * @return массив байтов
     */
    private static byte[] longToByteArray(Long l) {
        if (l != null) {
            byte[] toReturn = new byte[8];
            long primitiveL = l.longValue();
            toReturn[7] = (byte) primitiveL;
            primitiveL >>>= 8;
            toReturn[6] = (byte) primitiveL;
            primitiveL >>>= 8;
            toReturn[5] = (byte) primitiveL;
            primitiveL >>>= 8;
            toReturn[4] = (byte) primitiveL;
            primitiveL >>>= 8;
            toReturn[3] = (byte) primitiveL;
            primitiveL >>>= 8;
            toReturn[2] = (byte) primitiveL;
            primitiveL >>>= 8;
            toReturn[1] = (byte) primitiveL;
            primitiveL >>>= 8;
            toReturn[0] = (byte) primitiveL;
            return toReturn;
        }
        return new byte[]{(byte) 0xFF};
    }

    /**
     * Преобразует число int в массив байтов, для каждый из 4 байтов числа помещается в массив
     *
     * @return массив байтов
     */
    private static byte[] intToByteArray(Integer i) {
        if (i != null) {
            byte[] toReturn = new byte[4];
            int primitiveI = i.intValue();
            toReturn[3] = (byte) primitiveI;
            primitiveI >>>= 8;
            toReturn[2] = (byte) primitiveI;
            primitiveI >>>= 8;
            toReturn[1] = (byte) primitiveI;
            primitiveI >>>= 8;
            toReturn[0] = (byte) primitiveI;
            return toReturn;
        }
        return new byte[]{(byte) 0xFF};
    }

    /**
     * Сливает массивы байтов, таким образом, чтобы slave прицеплялся к хвосту master
     *
     * @param slave  массив источник
     * @param master массив к которому прицепляют источник
     * @param offset позиция массива master к которой прицепляют источник
     */
    private static void mergeByteArrays(byte[] slave, byte[] master, int offset) {
        System.arraycopy(slave, 0, master, offset, slave.length);
    }
}
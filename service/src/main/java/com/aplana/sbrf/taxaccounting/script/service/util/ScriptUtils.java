package com.aplana.sbrf.taxaccounting.script.service.util;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookUtils;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.consolidation.ConsolidationIncome;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.TAInterruptedException;
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
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
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

    public static final String EMPTY_FILE_NAME = "Имя файла не должно быть пустым!";
    public static final String EMPTY_INPUT_STREAM = "Поток данных пуст!";
    public static final String WRONG_FILE_FORMAT = "Неверная структура загружаемого файла!";
    public static final String WRONG_FILE_EXTENSION = "Выбранный файл не соответствует формату %s!";
    public static final String DUL_REGEXP = "[^№]+\\s[^N№]+";
    // код страны - Россия
    public static final String COUNTRY_CODE_RUSSIA = "643";
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
        XSSFReader r;
        StylesTable styles;
        SharedStringsTable sst;
        XMLReader parser;
        ContentHandler handler;
        InputStream sheet1 = null;
        InputSource sheetSource;

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
            if (pkg != null) {
                pkg.close();
            }
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

        try (OPCPackage pkg = OPCPackage.open(file.getAbsolutePath(), PackageAccess.READ)) {
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
            if (paramsMap != null) {
                this.paramsMap = paramsMap;
            } else {
                this.paramsMap = new HashMap<>();
                this.paramsMap.put("rowOffset", 0);
                this.paramsMap.put("colOffset", 0);
            }

            this.rowOffset = Integer.parseInt(String.valueOf(this.paramsMap.get("rowOffset")));
            this.colOffset = Integer.parseInt(String.valueOf(this.paramsMap.get("colOffset")));

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
                rowValues.add(getValue());
            } else if (name.equals("row")) { // конец строки
                if (isData && rowValues != null && !rowValues.isEmpty() && rowValues.get(0) != null && !rowValues.get(0).startsWith(headerStartValue)) {
                    endRead = (rowValues != null && rowValues.contains(tableEndValue));
                    if (!endRead) {
                        // еще не конец таблицы - дополнить список значений недостоющеми значениями и добавить ко всем строкам
                        performRowData();
                    }
                } else {
                    isData = false;
                    if (rowValues != null && !rowValues.isEmpty() && rowValues.get(0) != null && rowValues.get(0).startsWith(headerStartValue)) {
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
        boolean result;
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
            if (checkFormat(innValue, "0+")) {
                return "Значение гр. \"ИНН в РФ\" (\"" + innValue + "\") не должно содержать нули во всех разрядах";
            }
            if (Arrays.asList("00", "90", "93", "94", "95", "96", "98").contains(innValue.substring(0, 2))) {
                return "Значение гр. \"ИНН в РФ\" (\"" + innValue + "\") некорректно. Первые два разряда ИНН не могут быть равны одному из значений: \"00\",\"90\",\"93\",\"94\",\"95\",\"96\",\"98\"";
            }
            if (!RefBookUtils.checkControlSumInn(innValue)) {
                return "Некорректное контрольное число в значении гр. \"ИНН в РФ\" (\"" + innValue + "\")";
            }
        }
        return null;
    }

    public static List<String> checkFirstName(String firstName, String citizenship) {
        return checkName(firstName, citizenship, "Имя");
    }

    public static List<String> checkLastName(String lastName, String citizenship) {
        return checkName(lastName, citizenship, "Фамилия");
    }

    /**
     * Проверка допустимости имен и фамилий ФЛ.
     *
     * @param value       проверяемое имя или фамилия
     * @param citizenship код страны гражданства
     * @param attrName    что проверяем, для формирования сообщений об ошибке
     * @return если проверка не пройдена - сообщение об ошибке, иначе null
     */
    private static List<String> checkName(String value, String citizenship, String attrName) {

        List<String> errorMessages = new ArrayList<>();

        if (value != null && !value.isEmpty()) {
            if (citizenship != null && citizenship.equals(COUNTRY_CODE_RUSSIA)) {
                // для российских проверяем на кириллицу
                if (!checkFormat(value, "^[а-яА-ЯёЁ -]+")) {
                    errorMessages.add("Значение параметра \"" + attrName + "\" (\"" + value +
                            "\") содержит недопустимые символы. Значение может содержать только буквы русского алфавита (кириллица), пробелы и дефисы");
                }
            } else {
                // для иностранцев может быть латиница
                if (!checkFormat(value, "^[a-zA-Zа-яА-ЯёЁ '-]+")) {
                    errorMessages.add("Значение параметра \"" + attrName + "\" (\"" + value +
                            "\") содержит недопустимые символы. Значение может содержать только буквы русского (кириллица) или латинского алфавитов, пробелы, апострофы и дефисы");
                }
            }
            // проверяем первый символ
            String firstSymbol = value.substring(0, 1).toLowerCase();
            if (Arrays.asList(" ", "ь", "ъ", "-").contains(firstSymbol)) {
                errorMessages.add("Значение параметра \"" + attrName + "\" (\"" + value +
                        "\") некорректно. Первый символ не может быть равен одному из значений: \"Ъ\", \"Ь\", дефис или пробел");
            }
        }
        return errorMessages;
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
            zeroFormat = "[^\\wА-яа-яЁё]*(0[^\\wА-яа-яЁё]*){9}";
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
            zeroFormat = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}(0[^\\wА-яа-яЁё]*){6,7}";
        } else if (code.equals("91") && isUSSRIdDoc(value)) {
            return "Значение гр. 11 ДУЛ Номер (\"" + value + "\") содержит реквизиты паспорта гражданина СССР. Паспорт гражданина СССР не является актуальным документом, удостоверяющим личность";
        } else {
            format = ".{1,25}";
            formatStr = "";
        }
        if (format != null && !checkFormat(value, format)) {
            return "Значение гр. \"" + attrName + "\" (\"" + value + "\") не соответствует формату " + formatStr;
        }
        if (zeroFormat != null && checkFormat(value, zeroFormat)) {
            return "Значение гр. \"" + attrName + "\" (\"" + value + "\") не должно быть нулевым";
        }
        return null;
    }

    public static boolean isUSSRIdDoc(String number) {
        return checkFormat(number, "[IiVvXxLlCcУуХхЛлСс1]*-[А-ЯЁ]{2} [0-9]{6}");
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
        } else if (code.equals("22")) {
            format = "[0-9]{9}";
        } else if (code.equals("07")) {
            format = "[А-ЯЁ]{2}[0-9]?[0-9]{6}";
        } else if (code.equals("18")) {
            format = "[А-ЯЁ]{2}[0-9]{10}";
        } else if (code.equals("24")) {
            format = "[А-ЯЁ]{2}[0-9]{7}";
        } else if (code.equals("27")) {
            format = "[А-ЯЁ]{2}[0-9]?[0-9]{6}";
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
     * Рассчитывает UUID для объекта дохода на основе состояния полей объекта операции дохода. Данное значение UUID будет являться
     * отпечатком объекта. Архитектура UUID основана на стандарте RFC 4122. По этому стандарту UUID моет быть использована в т.ч для хеширования:
     * Мы используем версию 3 UUID - это значит что UUID генерируется на основе значения с использованием алгоритма MD5.
     * В этом методе из набора полей объекта дохода строится массив байтов. И из этого массива байтов вычисляется отпечаток,
     * который будет эквивалентен для одинаковых массивов байтов и различаться для разных массивов байтов.
     *
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
        byte[] taxSumm = bigDecimalToByteArray(income.getTaxSumm());
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

    /**
     * Создает группу Cell
     *
     * @param columns
     * @param styles
     * @return
     */
    public static List<Cell> createCells(List<Column> columns, List<FormStyle> styles) {
        List<Cell> cells = new ArrayList<Cell>();
        for (Column column : columns) {
            cells.add(new Cell(column, styles));
        }
        return cells;
    }
}
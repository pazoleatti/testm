package com.aplana.sbrf.taxaccounting.service.util;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.model.util.StringUtils.cleanString;

/**
 * Утилита для чтения excel-файлов
 */
public class ExcelImportUtils {
    private static final String EMPTY_FILE_NAME = "Имя файла не должно быть пустым!";
    private static final String EMPTY_INPUT_STREAM = "Поток данных пуст!";
    private static final String WRONG_FILE_FORMAT = "Неверная структура загружаемого файла!";
    private static final String WRONG_FILE_EXTENSION = "Выбранный файл не соответствует формату %s!";

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
     * Работает аналогично {@link #checkAndReadFile(InputStream, String, List, List, String, String, int, Map)}
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
}

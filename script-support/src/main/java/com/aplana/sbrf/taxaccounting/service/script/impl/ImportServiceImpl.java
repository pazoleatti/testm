package com.aplana.sbrf.taxaccounting.service.script.impl;

import au.com.bytecode.opencsv.CSVReader;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.script.Point;
import com.aplana.sbrf.taxaccounting.service.script.ImportService;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service("importService")
public class ImportServiceImpl implements ImportService {

    private static final String XLS = "xls";
    private static final String XLSX = "xlsx";
    private static final String XLSM = "xlsm";
    private static final String RNU = "rnu";
    private static final String XML = "xml";
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final String ENTER = "\r\n";
    private static final String TAB = "\t";
    private static final char SEPARATOR = '|';
    /**
     * Используется при обработке файла формата *.rnu (csv),
     * чтобы избежать ошибку при разборе строки файла: в случаях когда есть открывающий (нечетный) двойной апостраф,
     * но нет закрывающего (четного).
     * Изменил на пустой символ в связи с http://jira.aplana.com/browse/SBRFACCTAX-10422
     */
    private static final char QUOTE = '\0';

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Override
    public String getData(InputStream inputStream, String fileName, String charset) throws IOException {
        if (inputStream == null) {
            throw new ServiceException("Поток данных не должен быть пустым");
        }
        if (fileName == null || "".equals(fileName.trim())) {
            throw new ServiceException("Имя файла не может быть пустым");
        }
        if (charset == null || "".equals(charset.trim())) {
            charset = DEFAULT_CHARSET;
        }

        // Получение расширения файла
        String format = getFileExtension(fileName.trim());

        if (XLS.equals(format) || XLSX.equals(format) || XLSM.equals(format)) {
            return getXMLStringFromXLS(inputStream, null, null, null, null, !XLS.equals(format));
        } else if (XML.equals(format)) {
            return getXMLStringFromXML(inputStream, charset);
        } else if (RNU.equals(format) || (format != null && format.matches("r[\\d]{2}"))) {
            return getXMLStringFromCSV(inputStream, charset);
        }
        throw new ServiceException("Формат файла не может быть " + format + ".");
    }

    @Override
    public String getData(InputStream inputStream, String fileName, String charset, String startStr, String endStr) throws IOException {
        return getData(inputStream, fileName, charset, startStr, endStr, null);
    }

    @Override
    public String getData(InputStream inputStream, String fileName) throws IOException {
        return getData(inputStream, fileName, DEFAULT_CHARSET);
    }

    @Override
    public String getData(InputStream inputStream, String fileName, String charset, String startStr,
                          String endStr, Integer columnsCount) throws IOException {
        return getData(inputStream, fileName, charset, startStr, endStr, columnsCount, null);
    }

    @Override
    public String getData(InputStream inputStream, String fileName, String charset, String startStr,
                          String endStr, Integer columnsCount, Integer headerRowCount) throws IOException {
        if (inputStream == null) {
            throw new ServiceException("Поток данных не должен быть пустым");
        }
        if (fileName == null || "".equals(fileName.trim())) {
            throw new ServiceException("Имя файла не может быть пустым");
        }
        if (charset == null || "".equals(charset.trim())) {
            charset = DEFAULT_CHARSET;
        }
        if ((startStr == null || "".equals(startStr.trim()))
                && (endStr == null || "".equals(endStr.trim()))) {
            getData(inputStream, fileName, charset);
        }

        // Получение расширения файла
        String format = getFileExtension(fileName.trim());

        if (XLS.equals(format) || XLSX.equals(format) || XLSM.equals(format)) {
            return getXMLStringFromXLS(inputStream, startStr, endStr, columnsCount, headerRowCount, !XLS.equals(format));
        } else if (XML.equals(format)) {
            return getXMLStringFromXML(inputStream, charset);
        } else if (RNU.equals(format) || (format != null && format.matches("r[\\d]{2}"))) {
            return getXMLStringFromCSV(inputStream, charset);
        }
        throw new IllegalArgumentException("Формат файла не может быть " + format + ".");
    }

    /**
     * Получить текстовый xml из CSV файла.
     *
     * @param inputStream данные из файла
     * @param charset     кодировка
     */
    private String getXMLStringFromCSV(InputStream inputStream, String charset) throws IOException {
        InputStreamReader isr = new InputStreamReader(inputStream, charset);
        CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE);

        StringBuilder sb = new StringBuilder();
        sb.append("<data>").append(ENTER);
        String[] rowCells;
        // количество пустых строк
        int countEmptyRow = 0;
        while ((rowCells = reader.readNext()) != null) {
            // если еще не было пустых строк, то это первая строка - заголовок
            if (rowCells.length == 1 && rowCells[0].length() < 1) { // если встетилась вторая пустая строка, то дальше только строки итогов и ЦП
                if (countEmptyRow > 0) {
                    addRow(sb, reader.readNext(), "rowTotal");
                    break;
                }
                countEmptyRow++;
                continue;
            }
            if (countEmptyRow == 0) {
                addRow(sb, rowCells, "rowHead");
            } else {
                addRow(sb, rowCells, "row");
            }
        }
        sb.append("</data>");
        return sb.toString();
    }

    /**
     * Добавить строку в xml (при обработке CSV).
     *
     * @param sb       текст xml
     * @param rowCells список значении из CSV
     * @param rowName  наименование добавляемой строки (обычная строка с данными - <b>row</b>, итоговая строка - <b>rowTotal</b>)
     */
    void addRow(StringBuilder sb, String[] rowCells, String rowName) {
        if (rowCells == null) {
            return;
        }
        sb.append(TAB).append("<").append(rowName).append(" count=\"").append(rowCells.length).append("\"").append(">").append(ENTER);
        for (String cell : rowCells) {
            sb.append(TAB).append(TAB).append("<cell>");
            sb.append(StringEscapeUtils.escapeXml(StringUtils.cleanString(cell)));
            sb.append("</cell>").append(ENTER);
        }
        sb.append(TAB).append("</").append(rowName).append(">").append(ENTER);
    }

    /**
     * Получить текстовый xml из XLS файла.
     *
     * @param inputStream  данные из файла
     * @param startStr     начало таблицы (шапка первой колонки) (может быть null)
     * @param endStr       конец табцицы (надпись "итого" или значения после таблицы) (может быть null)
     * @param columnsCount количество колонок в таблице (может быть null)
     * @param headerRowCount количество строк в шапке (может быть null)
     * @param isCompressed true для xlsx и xlsm, false для xls
     */
    private String getXMLStringFromXLS(InputStream inputStream, String startStr, String endStr, Integer columnsCount,
                                       Integer headerRowCount,
                                       boolean isCompressed) throws IOException {
        Workbook workbook = isCompressed ? new XSSFWorkbook(inputStream) : new HSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheetAt(0);

        // позиция начала таблицы
        Point firstP = findCellCoordinateByValue(sheet, startStr);
        if (firstP == null) {
            firstP = new Point(0, 0);
        }
        // позиция конца таблицы
        Point endP = findCellCoordinateByValue(sheet, endStr);

        // обработка эксельки
        StringBuilder sb = new StringBuilder();
        sb.append("<data>").append(ENTER);

        // запись смещения для excel-файла
        sb.append("<infoXLS>").append(ENTER);
        String[] tmp = {Integer.toString(firstP.getY() + 1)};
        addRow(sb, tmp, "rowOffset");
        tmp[0] = Integer.toString(firstP.getX() + 1);
        addRow(sb, tmp, "colOffset");
        sb.append("</infoXLS>").append(ENTER);

        Set<Integer> skipSet = getSkipCol(sheet, firstP, headerRowCount);

        String rowStr;
        int indexRow = -1;

        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i ++)
        {
            indexRow++;
            Row row = sheet.getRow(i);

            // брать строки с позиции начала таблицы
            if (indexRow < firstP.getY()) {
                continue;
            }
            // брать строки до позиции конца таблицы
            if (endP != null && indexRow >= endP.getY()) {
                break;
            }

            // получить значения ячеек строки
            rowStr = getRowString(row, firstP.getX(), columnsCount, skipSet);
            sb.append(rowStr);
        }
        sb.append("</data>");
        return sb.toString();
    }

    /**
     * Получить номера столбцов которые нужно пропустить.
     *
     * @param sheet  лист XLS
     * @param firstP начало таблицы
     * @param headerRowCount количество строк в шапке
     */
    Set<Integer> getSkipCol(Sheet sheet, Point firstP, Integer headerRowCount) {
        Set<Integer> value = new HashSet<Integer>();
        // Список объединенных столбцов
        Set<Integer> mergeRegion = new HashSet<Integer>();
        if (headerRowCount == null) {
            headerRowCount = 2;
        }
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress adr = sheet.getMergedRegion(i);
            if (adr.getFirstRow() != firstP.getY() + headerRowCount - 1) {
                continue;
            }
            for (int j = adr.getFirstRow(); j <= adr.getLastRow(); j++) {
                for (int k = adr.getFirstColumn(); k <= adr.getLastColumn(); k++) {
                    mergeRegion.add(k);
                }
            }
        }
        if (mergeRegion.isEmpty()) {
            return value;
        }

        int indexRow = -1;

        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
            indexRow += 1;
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            // брать строки с позиции начала таблицы
            if (indexRow < (firstP.getY() + headerRowCount - 1)) {
                continue;
            } else if (indexRow > (firstP.getY() + headerRowCount - 1)) {
                break;
            }

            int indexCol = -1;
            for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
                indexCol++;
                // получить значение ячейки
                Cell cell = row.getCell(j);
                if (cell == null) {
                    continue;
                }
                // Пропускаем ячейки только в объединенных столбцах
                if (mergeRegion.contains(cell.getColumnIndex())) {
                    String cellValue = getCellValue(cell);
                    if (cellValue == null || "".equals(cellValue)) {
                        value.add(indexCol);
                    }
                }
            }
        }
        return value;
    }

    /**
     * Получить текстовый xml из XML файла.
     *
     * @param inputStream данные из файла
     * @param charset     кодировка
     */
    private String getXMLStringFromXML(InputStream inputStream, String charset) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader isr = new InputStreamReader(inputStream, charset);
        BufferedReader br = new BufferedReader(isr);
        String endXML = "</form>";
        String st;
        while (null != (st = br.readLine())) {
            // не брать цифровую подпись (после строки "</form>")
            if (st.contains(endXML)) {
                int lastIndex = st.indexOf(endXML) + endXML.length();
                sb.append(st.substring(0, lastIndex));
                break;
            }
            sb.append(StringUtils.cleanString(st));
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Получить в строковом виде строку из таблицы экселя
     *
     * @param row          строка из таблицы
     * @param colP         номер ячейки с которой брать данные
     * @param columnsCount количество столбцов в таблице
     */
    private String getRowString(Row row, Integer colP, Integer columnsCount, Set<Integer> skipSet) {
        if (row == null) {
            return TAB + "<row/>" + ENTER;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(TAB).append("<row>").append(ENTER);

        int indexCol = -1;
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            indexCol++;
            String cellValue = getCellValue(row.getCell(i));
            if (skipSet.contains(indexCol)) {
                continue;
            }
            if (colP != null && indexCol + row.getFirstCellNum() < colP.shortValue()) {
                continue;
            }
            if (columnsCount != null && indexCol > columnsCount) {
                break;
            }
            sb.append(TAB).append(TAB).append("<cell>");
            sb.append(cellValue != null ? StringEscapeUtils.escapeXml(cellValue) : "");
            sb.append("</cell>").append(ENTER);
        }
        sb.append(TAB).append("</row>").append(ENTER);
        return sb.toString();
    }

    /**
     * Получить значение ячейки.
     *
     * @param cell ячейка
     * @return значение в виде строки
     */
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        int type = cell.getCellType();
        return getCellAsString(cell, type, true);
    }

    /**
     * Получить значение ячейки.
     *
     * @param cell ячейка
     * @param type тип ячейки
     * @param useFormulaType проверять тип на формулу (true - для обычной ячейки, false - что бы получить значние формульной ячейки)
     */
    private String getCellAsString(Cell cell, int type, boolean useFormulaType) {
        String value = null;
        if (type == Cell.CELL_TYPE_STRING) {
            // строка
            value = StringUtils.cleanString(cell.getRichStringCellValue().toString());
        } else if (type == Cell.CELL_TYPE_NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                // дата
                Date date = cell.getDateCellValue();
                if (date != null) {
                    value = sdf.get().format(date);
                }
            } else {
                // число
                value = getNumberAsString(cell.getNumericCellValue());
            }
        } else if (useFormulaType && type == Cell.CELL_TYPE_FORMULA) {
            // формула - результат формулы может быть числовым, строковым..
            value = getCellAsString(cell, cell.getCachedFormulaResultType(), false);
        }
        return value;
    }

    /** Получить число в строковом виде. */
    private String getNumberAsString(double value) {
        String result;
        // число
        BigDecimal tmp1 = BigDecimal.valueOf(value).setScale(0, BigDecimal.ROUND_DOWN);
        BigDecimal tmp2 = BigDecimal.valueOf(value).setScale(0, BigDecimal.ROUND_UP);
        // если число без дроби, то отбросить часть числа "*.0", иначе не надо отбрасывать дробные значения
        if (tmp1.equals(tmp2)) {
            result = tmp1.toPlainString();
        } else {
            result = BigDecimal.valueOf(value).toPlainString();
        }
        if (result != null) {
            // поменять запятую на точку и убрать пробелы
            result = result.replaceAll(",", ".").replaceAll("[^\\d.,-]+", "");
        }
        return result;
    }

    /**
     * Найти координаты ячейки по его строковому значению.
     *
     * @param sheet страница эксель файла
     * @param value значение для поиска
     */
    private Point findCellCoordinateByValue(Sheet sheet, String value) {
        if (value == null) {
            return null;
        }
        // найти координату startStr
        int firstRow = -1;
        int firstCol = 0;
        boolean isFind = false;

        for (int i = sheet.getFirstRowNum();  i <= sheet.getLastRowNum() && !isFind ; i++) {
            firstRow++;
            firstCol = -1;
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
                firstCol++;
                String cell = getCellValue(row.getCell(j));
                if (value.equals(cell)) {
                    firstCol = firstCol + row.getFirstCellNum();
                    isFind = true;
                    break;
                }
            }
        }
        if (isFind) {
            return new Point(firstCol, firstRow);
        } else {
            return null;
        }
    }

    /**
     * Получить расширение файла.
     *
     * @param fileName имя файла
     * @return расширение (null - если нет расширения)
     */
    private String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return (index != -1 ? fileName.substring(index + 1).trim().toLowerCase() : null);
    }
}

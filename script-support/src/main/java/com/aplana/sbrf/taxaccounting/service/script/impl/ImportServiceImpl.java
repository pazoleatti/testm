package com.aplana.sbrf.taxaccounting.service.script.impl;

import au.com.bytecode.opencsv.CSVReader;
import com.aplana.sbrf.taxaccounting.model.script.Point;
import com.aplana.sbrf.taxaccounting.service.script.ImportService;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

@Service("importService")
public class ImportServiceImpl implements ImportService {

    private final String XLS = "xls";
    private final String CSV = "csv";
    private final String DEFAULT_CHARSET = "UTF-8";
    private final String ENTER = "\r\n";
    private final String TAB = "\t";
    private static HSSFDataFormatter formatter = new HSSFDataFormatter();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    public String getData(InputStream inputStream, String fileName, String charset) {
        if (inputStream == null) {
            throw new NullPointerException("inputStream cannot be null");
        }
        if (fileName == null) {
            throw new NullPointerException("file name cannot be null");
        }
        if ("".equals(fileName.trim())) {
            throw new IllegalArgumentException("file name cannot be empty");
        }
        if (charset == null || "".equals(charset.trim())) {
            charset = DEFAULT_CHARSET;
        }

        // Получение расширения файла
        String format = getFileExtension(fileName.trim());

        if (XLS.equals(format)) {
            return getXMLStringFromXLS(inputStream);
        } else if (CSV.equals(format)) {
            return getXMLStringFromCSV(inputStream, charset);
        }
        throw new IllegalArgumentException("format cannot be " + format + ". Only xls or csv");
    }

    @Override
    public String getData(InputStream inputStream, String fileName) {
        return getData(inputStream, fileName, DEFAULT_CHARSET);
    }

    @Override
    public String getData(InputStream inputStream, String fileName, String charset, String startStr, String endStr) {
        if (inputStream == null) {
            throw new NullPointerException("inputStream cannot be null");
        }
        if (fileName == null) {
            throw new NullPointerException("format cannot be null");
        }
        if ("".equals(fileName.trim())) {
            throw new IllegalArgumentException("file name cannot be empty");
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

        if (XLS.equals(format)) {
            return getXMLStringFromXLS(inputStream, startStr, endStr);
        } else if (CSV.equals(format)) {
            // TODO (Ramil Timerbaev) обработка csv с позициями
            return getXMLStringFromCSV(inputStream, charset);
        }
        throw new IllegalArgumentException("format cannot be " + format + ". Only xls or csv");
    }

    /**
     * Получить текстовый xml из CSV файла.
     *
     * @param inputStream данные из файла
     * @param charset кодировка
     */
    private String getXMLStringFromCSV(InputStream inputStream, String charset) {
        try {
            InputStreamReader isr = new InputStreamReader(inputStream, charset);
            CSVReader reader = new CSVReader(isr, ';');

            StringBuilder sb = new StringBuilder();
            sb.append("<data>").append(ENTER);
            String [] row;
            while ((row = reader.readNext()) != null) {
                sb.append(TAB).append("<row>").append(ENTER);
                for (String cell : row) {
                    sb.append(TAB).append(TAB).append("<cell>");
                    sb.append(cell);
                    sb.append("</cell>").append(ENTER);
                }
                sb.append(TAB).append("</row>").append(ENTER);
            }
            sb.append("</data>");
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Получить текстовый xml из XLS файла.
     *
     * @param startStr начало таблицы (шапка первой колонки)
     * @param endStr конец табцицы (надпись "итого" или значения после таблицы)
     * @param inputStream данные из файла
     */
    private String getXMLStringFromXLS(InputStream inputStream, String startStr, String endStr) {
        HSSFWorkbook workbook;
        try {
            workbook = new HSSFWorkbook(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        HSSFSheet sheet = workbook.getSheetAt(0);

        // позиция начала таблицы
        Point firstP = findCellCoordinateByValue(sheet, startStr);
        if (firstP == null) {
            return "<data></data>";
        }
        // позиция конца таблицы
        Point endP = findCellCoordinateByValue(sheet, endStr);

        // обработка эксельки
        StringBuilder sb = new StringBuilder();
        sb.append("<data>").append(ENTER);

        String rowStr;
        int indexRow = -1;
        Iterator rows = sheet.rowIterator();

        while (rows.hasNext()) {
            indexRow += 1;
            HSSFRow row = (HSSFRow) rows.next();

            // брать строки с позиции начала таблицы
            if (indexRow < firstP.getY()) {
                continue;
            }
            // брать строки до позиции конца таблицы
            if (endP != null && indexRow >= endP.getY()) {
                break;
            }

            // получить значения ячеек строки
            rowStr = getRowString(row, firstP.getX());
            sb.append(rowStr);
        }
        sb.append("</data>");
        return sb.toString();
    }

    /**
     * Получить текстовый xml из XLS файла.
     *
     * @param inputStream данные из файла
     */
    private String getXMLStringFromXLS(InputStream inputStream) {
        HSSFWorkbook workbook;
        try {
            workbook = new HSSFWorkbook(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        HSSFSheet sheet = workbook.getSheetAt(0);
        Iterator rows = sheet.rowIterator();

        StringBuilder sb = new StringBuilder();
        sb.append("<data>").append(ENTER);

        String rowStr;
        while (rows.hasNext()) {
            HSSFRow row = (HSSFRow) rows.next();
            rowStr = getRowString(row);
            sb.append(rowStr);
        }
        sb.append("</data>");
        return sb.toString();
    }

    /**
     * Получить в строковом виде строку из таблицы экселя
     *
     * @param row строка из таблицы
     */
    private String getRowString(HSSFRow row) {
        if (row == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(TAB).append("<row>").append(ENTER);
        String cellValue;
        Iterator<Cell> iterator = row.cellIterator();
        while (iterator.hasNext()) {
            cellValue = getCellValue((HSSFCell)iterator.next());
            sb.append(TAB).append(TAB).append("<cell>");
            sb.append(cellValue != null ? cellValue : "");
            sb.append("</cell>").append(ENTER);
        }
        sb.append(TAB).append("</row>").append(ENTER);
        return sb.toString();
    }

    /**
     * Получить в строковом виде строку из таблицы экселя
     *
     * @param row строка из таблицы
     * @param colP номер ячейки с которой брать данные
     */
    private String getRowString(HSSFRow row, Integer colP) {
        if (row == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(TAB).append("<row>").append(ENTER);
        String cellValue;
        Iterator<Cell> iterator = row.cellIterator();
        int indexCol = -1;
        while (iterator.hasNext()) {
            indexCol++;
            // получить значение ячейки
            cellValue = getCellValue((HSSFCell)iterator.next());
            if (colP != null && indexCol + row.getFirstCellNum() < colP.shortValue()) {
                continue;
            }
            sb.append(TAB).append(TAB).append("<cell>");
            sb.append(cellValue != null ? cellValue : "");
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
    private String getCellValue(HSSFCell cell) {
        if (cell == null) {
            return null;
        }
        String value = null;
        int type = cell.getCellType();
        if (type == HSSFCell.CELL_TYPE_STRING) {
            value = cell.getRichStringCellValue().toString();
        } else if (type == HSSFCell.CELL_TYPE_NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                // дата
                Date date = cell.getDateCellValue();
                if (date != null) {
                    value = dateFormat.format(date);
                }
            } else {
                // число
                value = formatter.formatCellValue(cell);
                if (value != null) {
                    // поменять запятую на точку и убрать пробелы
                    value = value.replaceAll(",", ".").replaceAll("[^\\d.,-]+", "");
                }
            }
        } else if (type == HSSFCell.CELL_TYPE_BLANK) {
            value = null;
        }
        return value;
    }

    /**
     * Найти координаты ячейки по его строковому значению.
     *
     * @param sheet страница эксель файла
     * @param value значение для поиска
     */
    private Point findCellCoordinateByValue(HSSFSheet sheet, String value) {
        if (value == null) {
            return null;
        }
        // найти координату startStr
        Iterator rows = sheet.rowIterator();
        int firstRow = -1;
        int firstCol = 0;
        boolean isFind = false;
        while (rows.hasNext() && !isFind) {
            firstRow++;
            firstCol = -1;
            HSSFRow row = (HSSFRow) rows.next();
            if (row == null) {
                continue;
            }
            Iterator<Cell> cells = row.cellIterator();
            while (cells.hasNext()) {
                firstCol++;
                String cell = getCellValue((HSSFCell)cells.next());
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
        int index = fileName.lastIndexOf(".");
        return (index != -1 ? fileName.substring(index + 1).trim().toLowerCase() : null);
    }
}

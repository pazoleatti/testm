package com.aplana.sbrf.taxaccounting.service.impl.print.persons;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.HashMap;
import java.util.Map;

public class StyleBuilder {

    private Workbook workBook;
    private Map<String, CellStyle> cellStyleCache = new HashMap<>();

    public StyleBuilder(Workbook workBook) {
        this.workBook = workBook;
    }

    CellStyle getCellStyle(String colPropName, PersonsReportBuilder.CellType cellType) {
        String cacheKey = colPropName + "_" + cellType.name();
        CellStyle cellStyle = cellStyleCache.get(cacheKey);
        if (cellStyle == null) {
            cellStyle = workBook.createCellStyle();
            cellStyle.setBorderRight(CellStyle.BORDER_THIN);
            cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
            cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
            cellStyle.setBorderTop(CellStyle.BORDER_THIN);
            cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            DataFormat dataFormat = workBook.createDataFormat();
            switch (cellType) {
                case STRING:
                    cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
                    cellStyle.setWrapText(true);
                    cellStyle.setDataFormat(dataFormat.getFormat("@"));
                    break;
                case NUMBERIC:
                    cellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
                    cellStyle.setWrapText(true);
                    cellStyle.setDataFormat(dataFormat.getFormat("@"));
                    break;
                case DATE:
                    cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
                    cellStyle.setDataFormat(dataFormat.getFormat("dd.MM.yyyy"));
                    break;
            }
            cellStyleCache.put(cacheKey, cellStyle);
        }
        return cellStyle;
    }

    CellStyle getBoldSmallStyle() {
        CellStyle style = cellStyleCache.get("table_header");
        if (style == null) {
            style = workBook.createCellStyle();
            style.setVerticalAlignment(CellStyle.VERTICAL_BOTTOM);
            Font fontBoldSmall = workBook.createFont();
            fontBoldSmall.setBoldweight(Font.BOLDWEIGHT_BOLD);
            fontBoldSmall.setFontHeightInPoints((short) 11);
            style.setFont(fontBoldSmall);
            cellStyleCache.put("table_header", style);
        }
        return style;
    }

    CellStyle getTableHeaderStyle() {
        Font font = workBook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        CellStyle tableHeaderCellStyle = workBook.createCellStyle();
        tableHeaderCellStyle.setFont(font);
        tableHeaderCellStyle.setBorderRight(CellStyle.BORDER_THIN);
        tableHeaderCellStyle.setBorderLeft(CellStyle.BORDER_THIN);
        tableHeaderCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
        tableHeaderCellStyle.setBorderTop(CellStyle.BORDER_THIN);
        tableHeaderCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        tableHeaderCellStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.index);
        tableHeaderCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        tableHeaderCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        tableHeaderCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        tableHeaderCellStyle.setWrapText(true);
        return tableHeaderCellStyle;
    }
}

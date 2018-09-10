package com.aplana.sbrf.taxaccounting.service.impl.print.departmentConfigs;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class StyleBuilder {

    enum CellType {
        STRING,
        NUMERIC,
        DATE
    }

    private Workbook workBook;
    private Map<String, CellStyle> cellStyleCache = new HashMap<>();

    StyleBuilder(Workbook workBook) {
        this.workBook = workBook;
    }

    CellStyle getCellStyle(String colPropName, CellType cellType, short align) {
        String cacheKey = colPropName + "_" + cellType.name();
        CellStyle cellStyle = cellStyleCache.get(cacheKey);
        if (cellStyle == null) {
            cellStyle = workBook.createCellStyle();
            cellStyle.setBorderRight(CellStyle.BORDER_THIN);
            cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
            cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
            cellStyle.setBorderTop(CellStyle.BORDER_THIN);
            cellStyle.setAlignment(align);
            cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            DataFormat dataFormat = workBook.createDataFormat();
            switch (cellType) {
                case STRING:
                    cellStyle.setWrapText(true);
                    cellStyle.setDataFormat(dataFormat.getFormat("@"));
                    break;
                case NUMERIC:
                    cellStyle.setWrapText(true);
                    cellStyle.setDataFormat(dataFormat.getFormat("@"));
                    break;
                case DATE:
                    cellStyle.setDataFormat(dataFormat.getFormat("dd.MM.yyyy"));
                    break;
            }
            cellStyleCache.put(cacheKey, cellStyle);
        }
        return cellStyle;
    }

    CellStyle getTableHeaderStyle() {
        Font font = workBook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        XSSFCellStyle tableHeaderCellStyle = (XSSFCellStyle) workBook.createCellStyle();
        tableHeaderCellStyle.setFont(font);
        tableHeaderCellStyle.setBorderRight(CellStyle.BORDER_THIN);
        tableHeaderCellStyle.setBorderLeft(CellStyle.BORDER_THIN);
        tableHeaderCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
        tableHeaderCellStyle.setBorderTop(CellStyle.BORDER_THIN);
        tableHeaderCellStyle.setFillForegroundColor(new XSSFColor(new Color(217, 217, 217)));
        tableHeaderCellStyle.setFillBackgroundColor(new XSSFColor(new Color(217, 217, 217)));
        tableHeaderCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        tableHeaderCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        tableHeaderCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        tableHeaderCellStyle.setWrapText(true);
        return tableHeaderCellStyle;
    }
}

package com.aplana.sbrf.taxaccounting.service.impl.print.tausers;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserFull;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractXlsxReportBuilder;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

/**
 *
 */
public class TAUsersReportBuilder extends AbstractXlsxReportBuilder {

    static {
        fileName = "Список_пользователей_";
    }

    private List<TAUserFull> taUserList;

    private int rowNumber = 5;
    private int cellNumber = 0;

    private String FIRST_COLUMN = "Логин";
    private String SECOND_COLUMN = "Имя";
    private String THIRD_COLUMN = "Департамент";
    private String FOURTH_COLUMN = "Активность";
    private String FIFTH_COLUMN = "Почта";
    private String SIXTH_COLUMN = "Роль";


    public TAUsersReportBuilder(List<TAUserFull> taUserList) {
        super();
        this.taUserList = taUserList;
        this.workBook = new XSSFWorkbook();
        this.sheet = workBook.createSheet("Список пользователей");
    }

    @Override
    protected void createTableHeaders() {
        CellStyle cs = workBook.createCellStyle();
        cs.setAlignment(CellStyle.ALIGN_CENTER);
        cs.setBorderBottom(CellStyle.BORDER_THICK);
        cs.setBorderTop(CellStyle.BORDER_THICK);
        cs.setBorderRight(CellStyle.BORDER_THICK);
        cs.setBorderLeft(CellStyle.BORDER_THICK);
        cs.setFillForegroundColor(HSSFColor.BRIGHT_GREEN.index);
        cs.setFillBackgroundColor(HSSFColor.BRIGHT_GREEN.index);
        cs.setFillPattern(CellStyle.SOLID_FOREGROUND);

        Row row = sheet.createRow(rowNumber);
        Cell cell = row.createCell(cellNumber++);
        cell.setCellStyle(cs);
        cell.setCellValue(FIRST_COLUMN);

        cell = row.createCell(cellNumber++);
        cell.setCellStyle(cs);
        cell.setCellValue(SECOND_COLUMN);

        cell = row.createCell(cellNumber++);
        cell.setCellStyle(cs);
        cell.setCellValue(THIRD_COLUMN);

        cell = row.createCell(cellNumber++);
        cell.setCellStyle(cs);
        cell.setCellValue(FOURTH_COLUMN);

        cell = row.createCell(cellNumber++);
        cell.setCellStyle(cs);
        cell.setCellValue(FIFTH_COLUMN);

        cell = row.createCell(cellNumber++);
        cell.setCellStyle(cs);
        cell.setCellValue(SIXTH_COLUMN);

        cellNumber = 0;
        rowNumber = sheet.getLastRowNum();
    }

    @Override
    protected void fillHeader() {
        CellStyle cs = workBook.createCellStyle();
        Font font = workBook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        cs.setFont(font);

        Row row = sheet.createRow(rowNumber);
        Cell cell = row.createCell(cellNumber++);
        cell.setCellValue("Пользователи АС Учет налогов");
        cell.setCellStyle(cs);

        cellNumber = 0;
        rowNumber = sheet.getLastRowNum() + 2;
    }

    @Override
    protected void createDataForTable() {
        CellStyle cs = workBook.createCellStyle();
        cs.setAlignment(CellStyle.ALIGN_CENTER);
        cs.setWrapText(true);
        cs.setBorderBottom(CellStyle.BORDER_DOUBLE);
        cs.setBorderTop(CellStyle.BORDER_DOUBLE);
        cs.setBorderRight(CellStyle.BORDER_DOUBLE);
        cs.setBorderLeft(CellStyle.BORDER_DOUBLE);

        for (TAUserFull user : taUserList){
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            Cell cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(user.getUser().getLogin());
            fillWidth(cellNumber, cell.getStringCellValue().length());
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(user.getUser().getName());
            fillWidth(cellNumber, cell.getStringCellValue().length());
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(user.getDepartment().getName());
            fillWidth(cellNumber, cell.getStringCellValue().length());
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(user.getUser().isActive()?"активный":"отключеный");
            fillWidth(cellNumber, cell.getStringCellValue().length());
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(user.getUser().getEmail());
            fillWidth(cellNumber, cell.getStringCellValue().length());
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            for (int j = 0; j < user.getUser().getRoles().size(); j++){
                cell.setCellValue(user.getUser().getRoles().get(j).getName());
                fillWidth(cellNumber, cell.getStringCellValue().length());
                if(j < user.getUser().getRoles().size() - 1){
                    row = sheet.createRow(sheet.getLastRowNum() + 1);
                    cell = row.createCell(cellNumber);
                }

            }

            cellNumber = 0;
            rowNumber = sheet.getLastRowNum();
        }
    }

    @Override
    protected void fillFooter() {
        //
    }
}

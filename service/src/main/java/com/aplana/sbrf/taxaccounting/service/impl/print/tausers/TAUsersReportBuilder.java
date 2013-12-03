package com.aplana.sbrf.taxaccounting.service.impl.print.tausers;

import com.aplana.sbrf.taxaccounting.model.TAUserFull;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Date;
import java.util.List;

/**
 *
 */
public class TAUsersReportBuilder extends AbstractReportBuilder {

    private List<TAUserFull> taUserList;

    private int rowNumber = 3;
    private int cellNumber = 0;

    private static final String FIRST_COLUMN = "Полное имя пользователя";
    private static final String SECOND_COLUMN = "Логин";
    private static final String THIRD_COLUMN = "Электронная почта";
    private static final String FOURTH_COLUMN = "Признак активности";
    private static final String FIFTH_COLUMN = "Подразделение";
    private static final String SIXTH_COLUMN = "Роль";


    public TAUsersReportBuilder(List<TAUserFull> taUserList) {
        super("Список_пользователей_", ".xlsx");
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
	    cs.setAlignment(CellStyle.ALIGN_CENTER);

        Row header = sheet.createRow(0);
        Cell cell = header.createCell(3);
        cell.setCellValue("Список пользователей");
        cell.setCellStyle(cs);


	    DataFormat dateFormat = workBook.createDataFormat();
	    CellStyle ds = workBook.createCellStyle();
	    ds.setDataFormat(dateFormat.getFormat("dd.mm.yyyy"));
	    ds.setAlignment(CellStyle.ALIGN_CENTER);
	    Row date = sheet.createRow(1);
	    Cell dateCell = date.createCell(3);
	    dateCell.setCellValue(new Date());
	    dateCell.setCellStyle(ds);


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
	        cell.setCellValue(user.getUser().getName());
	        fillWidth(cellNumber, cell.getStringCellValue().length());
	        cellNumber++;

	        cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(user.getUser().getLogin());
            fillWidth(cellNumber, cell.getStringCellValue().length());
            cellNumber++;

	        cell = row.createCell(cellNumber);
	        cell.setCellStyle(cs);
	        cell.setCellValue(user.getUser().getEmail());
	        fillWidth(cellNumber, cell.getStringCellValue().length());
	        cellNumber++;

	        cell = row.createCell(cellNumber);
	        cell.setCellStyle(cs);
	        cell.setCellValue(user.getUser().isActive()?"активный":"отключеный");
	        fillWidth(cellNumber, cell.getStringCellValue().length());
	        cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(user.getDepartment().getName());
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
                    cell.setCellStyle(cs);
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

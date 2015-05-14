package com.aplana.sbrf.taxaccounting.service.impl.print.logentry;

import au.com.bytecode.opencsv.CSVWriter;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class LogEntryReportBuilder extends AbstractReportBuilder {

	private static final String FIRST_COLUMN = "№ п/п";
    private static final String SECOND_COLUMN = "Дата-время";
    private static final String THIRD_COLUMN = "Тип сообщения";
	private static final String FOURTH_COLUMN = "Текст сообщения";

    private static final SimpleDateFormat DATE_DATA_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private static String[] headers = new String[]{"№ п/п", "Дата-время", "Тип сообщения", "Текст сообщения"};
    private static final String ENCODING = "windows-1251";

    private List<LogEntry> list;

	private int rowNumber = 0;
	private int cellNumber = 0;


	public LogEntryReportBuilder(List<LogEntry> list){
        super("log", ".xlsx");
        this.list = list;
		this.workBook = new XSSFWorkbook();
		this.sheet = workBook.createSheet("Учет налогов");
        this.sheet.setColumnWidth(1, cellWidthMin * 40 * 4);
        this.sheet.setColumnWidth(3, cellWidthMin * 256 * 4);
	}

	@Override
    protected void createTableHeaders(){
		CellStyle cs = workBook.createCellStyle();
		cs.setAlignment(CellStyle.ALIGN_LEFT);
		cs.setBorderBottom(CellStyle.BORDER_THICK);
		cs.setBorderTop(CellStyle.BORDER_THICK);
		cs.setBorderRight(CellStyle.BORDER_THICK);
		cs.setBorderLeft(CellStyle.BORDER_THICK);
		cs.setFillForegroundColor(IndexedColors.GREEN.index);
		cs.setFillBackgroundColor(IndexedColors.GREEN.index);
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

		cellNumber = 0;
		rowNumber = sheet.getLastRowNum();

	}

    @Override
    protected void fillHeader() {
        //No need to implement
    }

    @Override
    protected void fillFooter() {
        //No need to implement
    }

    @Override
    protected void createDataForTable(){
        //No need to implement
	}

    @Override
    protected String flush() throws IOException {
        File file = File.createTempFile("messages", ".csv");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
            CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(fileOutputStream, ENCODING), ';');

            csvWriter.writeNext(headers);
            for (int i = 0; i < list.size(); i++) {
                csvWriter.writeNext(assemble(list.get(i), i));
            }
            csvWriter.close();

            return file.getAbsolutePath();
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(fileOutputStream);
        }
    }

    private String[] assemble(LogEntry item, int numberStr){
        List<String> entries = new ArrayList<String>();
        entries.add(String.valueOf(numberStr));
        entries.add(DATE_DATA_FORMAT.format(item.getDate()));
        switch (item.getLevel()) {
            case ERROR:
                entries.add("ошибка");
                break;
            case WARNING:
                entries.add("предупреждение");
                break;
            default:
                entries.add("");
                break;
        }
        entries.add(item.getMessage());
        return entries.toArray(new String[entries.size()]);
    }
}

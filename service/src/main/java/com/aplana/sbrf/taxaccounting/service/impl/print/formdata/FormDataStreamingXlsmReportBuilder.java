package com.aplana.sbrf.taxaccounting.service.impl.print.formdata;


import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormDataReport;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.streaming.SheetDataWriter;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.helpers.XSSFRowShifter;

import java.io.*;
import java.lang.reflect.Field;
import java.util.List;

/**
   Для формирования больших отчетов (с использованием SXSSFWorkbook)
 */
public class FormDataStreamingXlsmReportBuilder extends FormDataXlsmReportBuilder {

    private static final Log LOG = LogFactory.getLog(FormDataStreamingXlsmReportBuilder.class);

    private XSSFWorkbook template;

    public FormDataStreamingXlsmReportBuilder(FormDataReport data, boolean isShowChecked, List<DataRow<Cell>> dataRows, RefBookValue periodCode, boolean deleteHiddenColumns)
            throws IOException {
        super(data, isShowChecked, dataRows, periodCode, deleteHiddenColumns);
    }

    @Override
    protected void initWorkbook() throws IOException {
        InputStream templateInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE);
        try {
            template = new XSSFWorkbook(templateInputStream);
            workBook = new SXSSFWorkbook(template, 100);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new IOException("Wrong file format. Template must be in format of 2007 Excel!!!");
        }
        sheet = template.getSheetAt(0);
        workBook.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK);
    }

    @Override
    protected void fillHeader() {
        /** Первые строки изменяем в шаблоне, остальные удаляем, т.к. по середине листа вставлять SXSSFWorkbook не даёт **/
        sheet = template.getSheetAt(0);

        super.fillHeader();

        AreaReference ar = new AreaReference(workBook.getName(XlsxReportMetadata.RANGE_REPORT_PERIOD).getRefersToFormula());
        for (int i = ar.getFirstCell().getRow() + 1; i <= sheet.getLastRowNum(); i++) {
            sheet.removeRow(sheet.getRow(i));
        }

        sheet = workBook.getSheetAt(0);
    }

    /** SXSSFSheet не поддерживает shiftRows, поэтому перемещяем только именованные области (Named Ranges) **/
    @Override
    protected void shiftRows(Sheet sheet, int startRow, int endRow, int n) {
        XSSFSheet templateSheet = template.getSheetAt(0);
        XSSFRowShifter rowShifter = new XSSFRowShifter(templateSheet);
        int sheetIndex = 0;
        FormulaShifter shifter = FormulaShifter.createForRowShift(sheetIndex, startRow, endRow, n);
        rowShifter.updateNamedRanges(shifter);
    }

    @Override
    protected void flush(File file) throws IOException {
        OutputStream out = new FileOutputStream(file);
        try {
            workBook.write(out);
        } finally {
            IOUtils.closeQuietly(out);
            // SXSSFWorkbook создаёт временные файлы, надо удалять
            //((SXSSFWorkbook) workBook).dispose(); // c версии poi 3.9
            try {
                deleteSXSSFTempFiles((SXSSFWorkbook) workBook);
            } catch (Exception e) {
                LOG.warn("Временнный файл не был удален.");
            }
        }
    }

    private Object getPrivateAttribute(Object containingClass, String fieldToGet) throws NoSuchFieldException, IllegalAccessException {
        //get the field of the containingClass instance
        Field declaredField = containingClass.getClass().getDeclaredField(fieldToGet);
        //set it as accessible
        declaredField.setAccessible(true);
        //access it
        Object get = declaredField.get(containingClass);
        //return it!
        return get;
    }

    private void deleteSXSSFTempFiles(SXSSFWorkbook workbook) throws NoSuchFieldException, IllegalAccessException {

        int numberOfSheets = workbook.getNumberOfSheets();

        //iterate through all sheets (each sheet as a temp file)
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheetAt = workbook.getSheetAt(i);

            //delete only if the sheet is written by stream
            if (sheetAt instanceof SXSSFSheet) {
                SheetDataWriter sdw = (SheetDataWriter) getPrivateAttribute(sheetAt, "_writer");
                File f = (File) getPrivateAttribute(sdw, "_fd");

                if (!f.delete()) {
                    LOG.warn(String.format("Временнный файл %s не был удален.", f.getName()));
                }
            }
        }
    }
}

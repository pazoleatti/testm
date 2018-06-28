package com.aplana.sbrf.taxaccounting.service.impl.print.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.impl.refbook.BatchIterator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.streaming.SheetDataWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class StreamingRefBookExcelReportBuilder extends RefBookExcelReportBuilder {
    private static final Log LOG = LogFactory.getLog(StreamingRefBookExcelReportBuilder.class);

    public StreamingRefBookExcelReportBuilder(RefBook refBook, List<RefBookAttribute> attributes, Date version, String searchPattern,
                                              boolean exactSearch, RefBookAttribute sortAttribute, PagingResult<Map<String, RefBookValue>> records) {
        super(refBook, attributes, version, searchPattern, exactSearch, sortAttribute, records);
    }

    public StreamingRefBookExcelReportBuilder(RefBook refBook, List<RefBookAttribute> attributes, Date version, String searchPattern,
                                              boolean exactSearch, RefBookAttribute sortAttribute, BatchIterator dataIterator) {
        super(refBook, attributes, version, searchPattern, exactSearch, sortAttribute, dataIterator);
    }

    @Override
    protected void initWorkbook() {
        workBook = new SXSSFWorkbook();
        workBook.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK);
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
}

package com.aplana.sbrf.taxaccounting.service.impl.print;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * User: avanteev
 * Date: 20.05.13
 */
public abstract class AbstractXlsxReportBuilder {

    protected Workbook workBook;

    protected Sheet sheet;

    protected final int cellWidthMin = 20;
    protected static final int cellWidthMax = 50;

    /*
     * Нужно создать в классе наследнике блок static, для определения в нем имени файла
     */
    protected static String fileName;

    protected static final int cellWidth = 30;

    public final String createReport() throws IOException {
        fillHeader();
        createTableHeaders();
        createDataForTable();
        fillFooter();
        return flush();
    }

    protected abstract void createTableHeaders();

    protected abstract void fillHeader();

    protected abstract void createDataForTable();

    protected abstract void fillFooter();

    private String flush() throws IOException {
        File file = File.createTempFile(fileName, ".xlsx");
        OutputStream out = new FileOutputStream(file);
        workBook.write(out);

        return file.getAbsolutePath();
    }
}

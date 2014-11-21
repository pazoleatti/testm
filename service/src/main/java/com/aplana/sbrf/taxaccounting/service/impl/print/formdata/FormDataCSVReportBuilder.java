package com.aplana.sbrf.taxaccounting.service.impl.print.formdata;

import au.com.bytecode.opencsv.CSVWriter;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FormDataCSVReportBuilder extends AbstractReportBuilder {

    public static final String FILE_NAME = "Налоговый_отчет_";

    List<DataRow<Cell>> dataRows;
    FormData data;
    FormTemplate formTemplate;
    boolean isShowChecked;
    RefBookValue refBookValue;

    public FormDataCSVReportBuilder() {
        super("", "");
    }

    public FormDataCSVReportBuilder(FormDataReport data, boolean isShowChecked, List<DataRow<Cell>> dataRows, RefBookValue refBookValue) {
        this();
        this.data = data.getData();
        this.dataRows = dataRows;
        formTemplate = data.getFormTemplate();
        this.isShowChecked = isShowChecked;
        this.refBookValue = refBookValue;
    }

    @Override
    protected void createTableHeaders() {}

    @Override
    protected void fillHeader() {}

    @Override
    protected void createDataForTable() {}

    @Override
    protected void fillFooter() {}

    @Override
    protected String flush() throws IOException {
        File file = File.createTempFile(FILE_NAME, ".csv");
        CSVWriter csvWriter = new CSVWriter(new FileWriter(file), ';');

        List<String> headersNames = new ArrayList<String>();
        for (Column column : data.getFormColumns()) {
            headersNames.add(column.getAlias());
        }

        csvWriter.writeNext(headersNames.toArray(new String[headersNames.size()]));
        for (DataRow<Cell> row : dataRows) {
            List<String> oneRow = new ArrayList<String>();
            for (Column column : formTemplate.getColumns()) {
				String alias = column.getAlias();
                if (ColumnType.REFBOOK.equals(column.getColumnType()) || ColumnType.REFERENCE.equals(column.getColumnType())) {
                    oneRow.add(row.getCell(alias).getRefBookDereference());
                } else {
                    oneRow.add(row.getCell(alias).getValue() == null ? "" : row.getCell(alias).getValue().toString());
                }
            }
            csvWriter.writeNext(oneRow.toArray(new String[oneRow.size()]));
        }

        csvWriter.close();

        return file.getAbsolutePath();
    }

    @Override
    protected byte[] flushBlobData() throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File file = new File(tmpDir + File.separator + FILE_NAME + ".csv");
        FileWriter fileWriter = new FileWriter(file);
        FileReader fileReader = new FileReader(file);
        try {
            CSVWriter csvWriter = new CSVWriter(fileWriter, ';');

            List<String> headersNames = new ArrayList<String>();
            for (Column column : data.getFormColumns()) {
                headersNames.add(column.getAlias());
            }

            csvWriter.writeNext(headersNames.toArray(new String[headersNames.size()]));
            for (DataRow<Cell> row : dataRows) {
                List<String> oneRow = new ArrayList<String>();
                for (Column column : formTemplate.getColumns()) {
                    if (column instanceof RefBookColumn || column instanceof ReferenceColumn) {
                        oneRow.add(row.getCell(column.getAlias()).getRefBookDereference());
                    } else {
                        oneRow.add(row.getCell(column.getAlias()).getValue() == null ? "" : row.getCell(column.getAlias()).getValue().toString());
                    }
                }
                csvWriter.writeNext(oneRow.toArray(new String[oneRow.size()]));
            }

            csvWriter.close();

            byte[] byteArray = IOUtils.toByteArray(fileReader);
            return byteArray;
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(fileWriter);
            IOUtils.closeQuietly(fileReader);
            if (!file.delete())
                logger.warn(String.format("Временнный файл %s не был удален.", FILE_NAME));
        }
    }
}

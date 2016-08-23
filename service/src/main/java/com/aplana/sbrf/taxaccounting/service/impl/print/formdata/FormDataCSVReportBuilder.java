package com.aplana.sbrf.taxaccounting.service.impl.print.formdata;

import au.com.bytecode.opencsv.CSVWriter;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class FormDataCSVReportBuilder extends AbstractReportBuilder {

    public static final String FILE_NAME = "Налоговый_отчет_";

    private List<DataRow<Cell>> dataRows;
    private FormData data;
    private FormTemplate formTemplate;
    private boolean isShowChecked;
    private RefBookValue refBookValue;


    public FormDataCSVReportBuilder(FormDataReport data, boolean isShowChecked, List<DataRow<Cell>> dataRows, RefBookValue refBookValue) {
        super("acctax", ".csv");
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
    protected File createTempFile() throws IOException {
        return File.createTempFile(FILE_NAME, ".csv");
    }

    @Override
    protected void flush(File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        CSVWriter csvWriter = new CSVWriter(bufferedWriter, ';');

        try {
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
                    } else if (ColumnType.NUMBER.equals(column.getColumnType())) {
                        oneRow.add(row.getCell(alias).getValue() == null ? "" : row.getCell(alias).getNumericValue().toPlainString());
                    } else if (ColumnType.DATE.equals(column.getColumnType())) {
                        String valueStr = "";
                        if (row.getCell(alias).getValue() != null) {
                            Formats formats = Formats.getById(((DateColumn) column).getFormatId());
                            SimpleDateFormat sdf;
                            if (formats.getId() == 0) {
                                sdf = new SimpleDateFormat(Formats.DD_MM_YYYY.getFormat());
                            } else {
                                sdf = new SimpleDateFormat(formats.getFormat());
                            }
                            valueStr = sdf.format(row.getCell(alias).getDateValue());
                        }
                        oneRow.add(valueStr);
                    } else {
                        oneRow.add(row.getCell(alias).getValue() == null ? "" : row.getCell(alias).getValue().toString());
                    }
                }
                csvWriter.writeNext(oneRow.toArray(new String[oneRow.size()]));
            }
        } finally {
            csvWriter.close();
            IOUtils.closeQuietly(bufferedWriter);
            IOUtils.closeQuietly(fileWriter);
        }
    }
}

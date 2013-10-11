package com.aplana.sbrf.taxaccounting.service.impl.print.logsystem;

import au.com.bytecode.opencsv.CSVWriter;
import com.aplana.sbrf.taxaccounting.model.LogSystemSearchResultItem;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * User: avanteev
 */
public class LogSystemCsvBuilder extends AbstractReportBuilder {

    private static final SimpleDateFormat SDF_LOG_NAME = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy");
    private static final String PATTER_LOG_FILE_NAME = "log_<%s>-<%s>";
    private static final String ENCODING = "windows-1251";

    private static final String POSTFIX = ".zip";

    private static String[] headers = new String[]{"Дата-время", "Событие", "Текст события", "Период", "Подразделение",
            "Тип формы", "Тип налоговой формы", "Вид налоговой формы/декларации",
            "Пользователь", "Роль пользователя", "IP пользователя"};

    private List<LogSystemSearchResultItem> items;

    public LogSystemCsvBuilder(List<LogSystemSearchResultItem> items) throws IOException {
        super("", "");
        this.items = items;
    }

    @Override
    protected void createTableHeaders() {
        //No need to implement
    }

    @Override
    protected void fillHeader() {
        //No need to implement
    }

    @Override
    protected void createDataForTable() {
        //No need to implement
    }

    @Override
    protected void fillFooter() {
        //No need to implement
    }

    @Override
    protected String flush() throws IOException {
        String fileName = String.format(PATTER_LOG_FILE_NAME,
                SDF_LOG_NAME.format(items.get(0).getLogDate()),
                SDF_LOG_NAME.format(items.get(items.size() - 1).getLogDate()));
        File file = File.createTempFile(fileName, ".csv");
        CSVWriter csvWriter = new CSVWriter(new FileWriter(file), ';');

        csvWriter.writeNext(headers);
        for (LogSystemSearchResultItem resultItem : items) {
            csvWriter.writeNext(assemble(resultItem));
        }
        csvWriter.close();

        File zipFile = File.createTempFile(fileName, POSTFIX);
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile));
        ZipEntry zipEntry = new ZipEntry(file.getName());
        zout.putNextEntry(zipEntry);
        zout.write(IOUtils.toByteArray(new FileReader(file), ENCODING));
        zout.close();

        file.delete();
        return zipFile.getAbsolutePath();
    }

    private String[] assemble(LogSystemSearchResultItem item){
        List<String> entries = new ArrayList<String>();
        entries.add(SDF.format(item.getLogDate()));
        entries.add(item.getEvent().getTitle());
        entries.add(item.getNote());
        entries.add(item.getReportPeriod() != null ?
                item.getReportPeriod().getName() + " " + item.getReportPeriod().getYear() :
                "");
        entries.add(item.getDepartment().getName());
        entries.add(item.getFormType() != null?"Налоговые формы" :
                item.getDeclarationType() != null?"Декларации":"");
        entries.add(item.getFormKind() != null ? item.getFormKind().getName() : "");
        entries.add(item.getFormType() != null ? item.getFormType().getName() : "");
        entries.add(item.getUser().getName());
        entries.add(item.getRoles());
        entries.add(item.getIp());

        return entries.toArray(new String[entries.size()]);
    }
}

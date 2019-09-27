package com.aplana.sbrf.taxaccounting.service.impl.print.logentry;

import au.com.bytecode.opencsv.CSVWriter;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для формирования CSV отчета уведомлений
 */
public class LogEntryReportBuilder extends AbstractReportBuilder {

    private static final String FIRST_COLUMN = "№ п/п";
    private static final String SECOND_COLUMN = "Дата-время";
    private static final String THIRD_COLUMN = "Тип сообщения";
    private static final String FOURTH_COLUMN = "Текст сообщения";
    private static final String FIFTH_COLUMN = "Тип";
    private static final String SIXTH_COLUMN = "Объект";
    private static final String SEVEN_COLUMN = "Период";

    // Шапка таблицы
    private static final String[] headers = new String[]{FIRST_COLUMN, SECOND_COLUMN, THIRD_COLUMN, FOURTH_COLUMN, FIFTH_COLUMN, SIXTH_COLUMN, SEVEN_COLUMN};
    private static final String ENCODING = "windows-1251";

    // Список записей уведомления
    private final List<LogEntry> list;


    public LogEntryReportBuilder(List<LogEntry> list) {
        super("log", ".xlsx");
        this.list = list;
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
    protected void fillFooter() {
        //No need to implement
    }

    @Override
    protected void createDataForTable() {
        //No need to implement
    }

    @Override
    protected File createTempFile() throws IOException {
        return File.createTempFile("messages", ".csv");
    }

    @Override
    protected void flush(File file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
            CSVWriter csvWriter = new CSVWriter(new BufferedWriter(new OutputStreamWriter(fileOutputStream, ENCODING)), ';');

            csvWriter.writeNext(headers);
            for (int i = 0; i < list.size(); i++) {
                csvWriter.writeNext(createRow(list.get(i), i + 1));
            }
            csvWriter.close();
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(fileOutputStream);
        }
    }

    private String[] createRow(LogEntry item, int numberStr) {
        List<String> entries = new ArrayList<>(4);
        entries.add(String.valueOf(numberStr));
        entries.add(FastDateFormat.getInstance("dd.MM.yyyy HH:mm:ss").format(item.getDate()));
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
        entries.add(item.getType());
        entries.add(item.getObject());
        entries.add(item.getPeriod());
        return entries.toArray(new String[entries.size()]);
    }
}

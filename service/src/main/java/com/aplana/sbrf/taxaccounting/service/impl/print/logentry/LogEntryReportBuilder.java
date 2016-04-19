package com.aplana.sbrf.taxaccounting.service.impl.print.logentry;

import au.com.bytecode.opencsv.CSVWriter;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LogEntryReportBuilder extends AbstractReportBuilder {

	private static final String FIRST_COLUMN = "№ п/п";
    private static final String SECOND_COLUMN = "Дата-время";
    private static final String THIRD_COLUMN = "Тип сообщения";
	private static final String FOURTH_COLUMN = "Текст сообщения";

    private static final ThreadLocal<SimpleDateFormat> DATE_DATA_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        }
    };

    private static String[] headers = new String[]{FIRST_COLUMN, SECOND_COLUMN, THIRD_COLUMN, FOURTH_COLUMN};
    private static final String ENCODING = "windows-1251";

    private List<LogEntry> list;


	public LogEntryReportBuilder(List<LogEntry> list){
        super("log", ".xlsx");
        this.list = list;
	}

	@Override
    protected void createTableHeaders(){
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
    protected void createDataForTable(){
        //No need to implement
	}

    @Override
    protected String flush() throws IOException {
        File file = File.createTempFile("messages", ".csv");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
            CSVWriter csvWriter = new CSVWriter(new BufferedWriter(new OutputStreamWriter(fileOutputStream, ENCODING)), ';');

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
        List<String> entries = new ArrayList<String>(4);
        entries.add(String.valueOf(numberStr));
        entries.add(DATE_DATA_FORMAT.get().format(item.getDate()));
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

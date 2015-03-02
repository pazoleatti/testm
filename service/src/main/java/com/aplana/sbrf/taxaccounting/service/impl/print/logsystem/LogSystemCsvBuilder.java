package com.aplana.sbrf.taxaccounting.service.impl.print.logsystem;

import au.com.bytecode.opencsv.CSVWriter;
import com.aplana.sbrf.taxaccounting.model.LogSearchResultItem;
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

    private static final SimpleDateFormat SDF_LOG_NAME = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static final String PATTER_LOG_FILE_NAME = "log_%s-%s";
    private static final String ENCODING = "windows-1251";

    private static final String POSTFIX = ".zip";

    private static String[] headers = new String[]{"Дата-время", "Событие", "Текст события", "Период", "Подразделение",
            "Тип формы", "Тип налоговой формы", "Вид налоговой формы/декларации",
            "Пользователь", "Роль пользователя", "IP пользователя"};

    private List<LogSearchResultItem> items;

    public LogSystemCsvBuilder(List<LogSearchResultItem> items) throws IOException {
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
    protected File flushBlobDataFile() throws IOException {
        String fileName = String.format(PATTER_LOG_FILE_NAME,
                SDF_LOG_NAME.format(items.get(items.size() - 1).getLogDate()),
                SDF_LOG_NAME.format(items.get(0).getLogDate()));
        String tmpDir = System.getProperty("java.io.tmpdir");
        File file = new File(tmpDir + File.separator + fileName + ".csv");
        FileWriter fileWriter = new FileWriter(file);
        File zipFile = new File(tmpDir + File.separator + fileName + POSTFIX);
        try {
            CSVWriter csvWriter = new CSVWriter(fileWriter, ';');

            csvWriter.writeNext(headers);
            for (LogSearchResultItem resultItem : items) {
                csvWriter.writeNext(assemble(resultItem));
            }
            csvWriter.close();

            ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile));
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zout.putNextEntry(zipEntry);
            zout.write(IOUtils.toByteArray(new FileReader(file), ENCODING));
            zout.close();

            return zipFile;
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(fileWriter);
            //IOUtils.closeQuietly(fileReader);
            if (!file.delete())
                logger.warn(String.format("Временнный файл %s не был удален.", file.getName()));
            /*if (!zipFile.delete())
                logger.warn(String.format("Временнный файл %s не был удален.", zipFile.getName()));*/
        }
    }

    private String[] assemble(LogSearchResultItem item){
        List<String> entries = new ArrayList<String>();
        entries.add(SDF.format(item.getLogDate()));
        entries.add(item.getEvent().getTitle());
        entries.add(item.getNote());
        entries.add(item.getReportPeriodName() != null ? item.getReportPeriodName() : "");
        entries.add(item.getDepartmentName());
        entries.add(item.getFormTypeName() != null?"Налоговые формы" :
                item.getDeclarationTypeName() != null?"Декларации":"");
        entries.add(item.getFormKind() != null ? item.getFormKind().getName() : "");
        entries.add(item.getFormTypeName());
        entries.add(item.getUser());
        entries.add(item.getRoles());
        entries.add(item.getIp());

        return entries.toArray(new String[entries.size()]);
    }
}

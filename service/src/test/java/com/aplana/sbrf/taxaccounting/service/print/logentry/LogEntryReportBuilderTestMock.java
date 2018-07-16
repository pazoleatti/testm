package com.aplana.sbrf.taxaccounting.service.print.logentry;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.impl.print.logentry.LogEntryReportBuilder;
import com.aplana.sbrf.taxaccounting.service.print.AbstractReportBuilderTest;
import org.apache.commons.lang3.time.FastDateFormat;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LogEntryReportBuilderTestMock extends AbstractReportBuilderTest {

    private static FastDateFormat dateFormat = FastDateFormat.getInstance("dd.MM.yyyy HH:mm:ss");

    private List<LogEntry> logEntries = new ArrayList<LogEntry>();

    @Before
    public void init() {
        logEntries.add(new LogEntry(LogLevel.ERROR, "Ошибочка! Учи матчасть!!!"));
        logEntries.add(new LogEntry(LogLevel.INFO, "Информация!"));
        logEntries.add(new LogEntry(LogLevel.WARNING, "Предупреждение! Будь аккуратней."));
    }

    @Test
    public void testReport() throws Exception {
        LogEntryReportBuilder reportBuilder = new LogEntryReportBuilder(logEntries);

        String reportPath = null;
        try {
            reportPath = reportBuilder.createReport();

            String[][] rowsExpected = new String[][]{
                    {"№ п/п", "Дата-время", "Тип сообщения", "Текст сообщения", "Тип", "Объект"},
                    {"1", dateFormat.format(logEntries.get(0).getDate()), "ошибка", "Ошибочка! Учи матчасть!!!"},
                    {"2", dateFormat.format(logEntries.get(1).getDate()), "", "Информация!"},
                    {"3", dateFormat.format(logEntries.get(2).getDate()), "предупреждение", "Предупреждение! Будь аккуратней."}
            };
            assertEquals(toList(rowsExpected).toString(),
                    readCsvFile(reportPath, "windows-1251").toString());
        } finally {
            if (reportPath != null) {
                File file = new File(reportPath);
                file.delete();
            }
        }
    }

}

package com.aplana.sbrf.taxaccounting.service.print;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.impl.print.logentry.LogEntryReportBuilder;

public class LogEntryReportBuilderTestMock {
	
	private List<LogEntry> logEntries = new ArrayList<LogEntry>();
	
	@Before
	public void init(){
		logEntries.add(new LogEntry(LogLevel.ERROR,"Ошибочка! Учи матчасть!!!"));
		logEntries.add(new LogEntry(LogLevel.INFO,"Информация!"));
		logEntries.add(new LogEntry(LogLevel.WARNING,"Предупреждение! Будь аккуратней."));
	}
	
	@Test
	public void testReport() throws IOException{
		LogEntryReportBuilder builder = new LogEntryReportBuilder(logEntries);
		try {
			builder.createReport();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

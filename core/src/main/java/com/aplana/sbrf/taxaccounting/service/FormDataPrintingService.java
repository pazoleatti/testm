package com.aplana.sbrf.taxaccounting.service;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.TAUserFull;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;

public interface FormDataPrintingService  {
	
	String generateExcel(int userId, long formDataId, boolean isShowChecked);
	String generateExcelLogEntry(List<LogEntry> listLogEntries);
    String generateExcelUsers(List<TAUserFull> taUserFullList);

}

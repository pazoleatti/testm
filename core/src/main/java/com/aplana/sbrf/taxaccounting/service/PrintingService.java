package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.LogSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.TAUserFull;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;

import java.util.List;

public interface PrintingService {
	
	String generateExcel(TAUserInfo userInfo, long formDataId, boolean isShowChecked);
	String generateExcelLogEntry(List<LogEntry> listLogEntries);
    String generateExcelUsers(List<TAUserFull> taUserFullList);
    String generateExcelLogSystem(List<LogSearchResultItem> resultItems);
    String generateAuditCsv(List<LogSearchResultItem> resultItems);
}

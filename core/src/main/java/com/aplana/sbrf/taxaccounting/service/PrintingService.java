package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.LogSystemSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.TAUserFull;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;

import java.util.List;

public interface PrintingService {
	
	String generateExcel(TAUserInfo userInfo, long formDataId, boolean isShowChecked);
	String generateExcelLogEntry(List<LogEntry> listLogEntries);
    String generateExcelUsers(List<TAUserFull> taUserFullList);
    String generateExcelLogSystem(List<LogSystemSearchResultItem> resultItems);
    String generateAuditCsv(List<LogSystemSearchResultItem> resultItems);
}

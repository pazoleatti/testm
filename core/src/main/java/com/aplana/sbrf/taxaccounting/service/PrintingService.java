package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.LogSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.TAUserFullWithDepartmentPath;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;

import java.util.List;

public interface PrintingService {
	
	String generateExcel(TAUserInfo userInfo, long formDataId, boolean manual, boolean isShowChecked);
	String generateExcelLogEntry(List<LogEntry> listLogEntries);
    String generateExcelUsers(List<TAUserFullWithDepartmentPath> taUserFullList);
    String generateExcelLogSystem(List<LogSearchResultItem> resultItems);
    String generateAuditCsv(List<LogSearchResultItem> resultItems);
}

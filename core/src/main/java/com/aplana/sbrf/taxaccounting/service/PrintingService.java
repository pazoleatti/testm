package com.aplana.sbrf.taxaccounting.service;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.LogSystemSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.TAUserFull;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;

public interface PrintingService {
	
	String generateExcel(TAUserInfo userInfo, long formDataId, boolean isShowChecked);
	String generateExcelLogEntry(List<LogEntry> listLogEntries);
    String generateExcelUsers(List<TAUserFull> taUserFullList);
    String generateExcelLogSystem(List<LogSystemSearchResultItem> resultItems);

}

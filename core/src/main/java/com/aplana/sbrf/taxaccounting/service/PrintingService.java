package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.LogSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TAUserView;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;

import java.util.List;

public interface PrintingService {
    /**
     * Формирует Excel-отчет НФ, сохраняет его в таблицы BLOB_DATA и возвращает uuid
     * @param userInfo
     * @param formDataId
     * @param manual
     * @param isShowChecked
     * @param saved
     * @return uuid записи с данными из таблицы BLOB_DATA
     */
    String generateExcel(TAUserInfo userInfo, long formDataId, boolean manual, boolean isShowChecked, boolean saved, LockStateLogger stateLogger);
    /**
     * Формирует CSV-отчет НФ, сохраняет его в таблицы BLOB_DATA и возвращает uuid
     * @param userInfo
     * @param formDataId
     * @param manual
     * @param isShowChecked
     * @param saved
     * @return uuid записи с данными из таблицы BLOB_DATA
     */
    String generateCSV(TAUserInfo userInfo, long formDataId, boolean manual, boolean isShowChecked, boolean saved, LockStateLogger stateLogger);
	String generateExcelLogEntry(List<LogEntry> listLogEntries);
    String generateExcelUsers(List<TAUserView> taUserViewList);
    String generateExcelLogSystem(List<LogSearchResultItem> resultItems);

    /**
     * Формирует csv-отчет и архивирует его в zip.
     * @return идентификатор в таблице BLOB_DATA
     */
    String generateAuditZip(List<LogSearchResultItem> resultItems);
}

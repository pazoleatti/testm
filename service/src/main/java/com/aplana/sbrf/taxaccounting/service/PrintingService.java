package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TAUserView;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;

import java.util.Date;
import java.util.List;

public interface PrintingService {
    String generateExcelLogEntry(List<LogEntry> listLogEntries);

    String generateExcelUsers(List<TAUserView> taUserViewList);

    /**
     * Формирует специфический отчет по справочнику
     *
     * @param refBookId
     * @param version
     * @param filter
     * @param specificReportType
     * @param userInfo
     * @param stateLogger
     * @return
     */
    String generateRefBookSpecificReport(long refBookId, String specificReportType, Date version, String filter, String searchPattern, RefBookAttribute sortAttribute, boolean isSortAscending, TAUserInfo userInfo, LockStateLogger stateLogger);

    /**
     * Формирует csv-отчет по справочнику
     *  @param refBookId
     * @param version
     * @param filter
     * @param sortAttribute
     * @param isSortAscending
     * @param searchPattern
     * @param exactSearch
     * @param stateLogger  @return
     */
    String generateRefBookCSV(long refBookId, Date version, String filter, RefBookAttribute sortAttribute, boolean isSortAscending, String searchPattern, Boolean exactSearch, LockStateLogger stateLogger);

    /**
     * Формирует excel-отчет по справочнику
     *
     * @param refBookId
     * @param version
     * @param filter
     * @param sortAttribute
     * @param isSortAscending
     * @param stateLogger
     * @return
     */
    String generateRefBookExcel(long refBookId, Date version, String filter, String searchPattern,
                                RefBookAttribute sortAttribute, boolean isSortAscending, LockStateLogger stateLogger);
}

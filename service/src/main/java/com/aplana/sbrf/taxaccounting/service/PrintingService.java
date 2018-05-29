package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TAUserView;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;

import java.util.Date;
import java.util.List;

/**
 * Интерфейс для формирования отчета по справочнику
 */
public interface PrintingService {
    String generateExcelLogEntry(List<LogEntry> listLogEntries);

    String generateExcelUsers(List<TAUserView> taUserViewList);

    /**
     * Формирует специфический отчет по справочнику
     *
     * @param refBookId          идентификатор справочника
     * @param version            дата актуальности
     * @param filter             sql-фрагмент фильтрации
     * @param specificReportType тип отчета
     * @param userInfo           информация о пользователе
     * @param stateLogger        логгер статуса задачи
     * @return uuid идентификатор файла blobData
     */
    String generateRefBookSpecificReport(long refBookId, String specificReportType, Date version, String filter, String searchPattern, RefBookAttribute sortAttribute, boolean isSortAscending, TAUserInfo userInfo, LockStateLogger stateLogger);

    /**
     * Формирует csv-отчет по справочнику
     *
     * @param refBookId       идентификатор справочника
     * @param version         дата актуальности
     * @param filter          sql-фрагмент фильтрации
     * @param sortAttribute   атрибут сортировки данных в отчете
     * @param isSortAscending признак сортировки по возрастанию
     * @param searchPattern   параметр поиска
     * @param exactSearch     признак поиска по точному совпадению
     * @param stateLogger     логгер статуса задачи
     * @return uuid идентификатор файла blobData
     */
    String generateRefBookCSV(long refBookId, Date version, String filter, RefBookAttribute sortAttribute, boolean isSortAscending, String searchPattern, Boolean exactSearch, LockStateLogger stateLogger);

    /**
     * Формирует excel-отчет по справочнику
     *
     * @param refBookId       идентификатор справочника
     * @param version         дата актуальности
     * @param filter          sql-фрагмент фильтрации
     * @param sortAttribute   атрибут сортировки данных в отчете
     * @param isSortAscending признак сортировки по возрастанию
     * @param stateLogger     логгер статуса задачи
     * @return uuid идентификатор файла blobData
     */
    String generateRefBookExcel(long refBookId, Date version, String filter, String searchPattern,
                                RefBookAttribute sortAttribute, boolean isSortAscending, LockStateLogger stateLogger);
}

package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Интерфейс для формирования отчета по справочнику
 */
public interface PrintingService {
    String generateExcelLogEntry(List<LogEntry> listLogEntries);

    /**
     * Формирует xlsx-файл со списком пользователей.
     *
     * @param taUserViewList список пользователей
     * @return uuid идентификатор файла blobData
     */
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
     * @param refBookId     идентификатор справочника
     * @param version       дата актуальности
     * @param searchPattern строка с текстом для поиска по справочника
     * @param exactSearch   признак точного совпадения при поиске
     * @param extraParams   дополнительные параметры для фильтрации записей
     * @param sortAttribute атрибут сортировки данных в отчете
     * @param direction     направление сортировки - asc, desc
     * @param stateLogger   логгер статуса задачи
     * @return uuid идентификатор файла blobData
     */
    String generateRefBookCSV(long refBookId, Date version, String searchPattern, boolean exactSearch, Map<String, String> extraParams,
                              RefBookAttribute sortAttribute, String direction, LockStateLogger stateLogger);

    /**
     * Формирует excel-отчет по справочнику
     *
     * @param refBookId     идентификатор справочника
     * @param version       дата актуальности
     * @param searchPattern строка с текстом для поиска по справочника
     * @param exactSearch   признак точного совпадения при поиске
     * @param extraParams   дополнительные параметры для фильтрации записей
     * @param sortAttribute атрибут сортировки данных в отчете
     * @param direction     направление сортировки - asc, desc
     * @param stateLogger   логгер статуса задачи
     * @return uuid идентификатор файла blobData
     */
    String generateRefBookExcel(long refBookId, Date version, String searchPattern, boolean exactSearch, Map<String, String> extraParams,
                                RefBookAttribute sortAttribute, String direction, LockStateLogger stateLogger);

    /**
     * Формирует excel-отчет реестра ФЛ
     *
     * @param filter фильтр на ФЛ
     * @return uuid на блоб файла
     */
    String generateExcelPersons(RefBookPersonFilter filter, PagingParams pagingParams, TAUser user);

    /**
     * Формирует excel-отчет настроек подразделений
     *
     * @param departmentId ид подразделения
     * @return uuid на блоб файла
     */
    String generateExcelDepartmentConfigs(int departmentId);

    /**
     * Формирование csv-файла с уведомлениями.
     *
     * @param notifications объекты уведомлений.
     * @return uuid сформированного файла в базе.
     */
    String generateCsvNotifications(List<Notification> notifications);
}

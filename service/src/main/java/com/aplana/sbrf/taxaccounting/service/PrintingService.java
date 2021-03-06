package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Интерфейс для формирования отчета по справочнику
 */
public interface PrintingService {

    /**
     * Формирование csv-файла уведомлений.
     *
     * @param logEntries список уведомлений.
     * @return uuid csv-файла.
     */
    String generateCsvLogEntries(List<LogEntry> logEntries);

    /**
     * Формирование архива с csv-файлами уведомлений по оповещениям.
     *
     * @return uuid файла архива.
     */
    String generateCsvNotificationsLogsArchive(List<Notification> notifications);

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
     * Формирование csv-файла с оповещениями.
     *
     * @param notifications объекты оповещений.
     * @return uuid сформированного файла в базе.
     */
    String generateCsvNotifications(List<Notification> notifications);

    /**
     * Формирует excel-отчет списка источники-приемники
     *
     * @param declarationDataId  идентификатор декларации
     * @param sources            источники
     * @param destinations       приемники
     * @return uuid на блоб файла
     */
    String generateExcelUnloadList(long declarationDataId, boolean sources, boolean destinations, TAUser user);


    /**
     * Формирует Excel со списком собщений "Обмена с ФП АС Учет Налогов"
     *
     * @return streamOutput, который необходимо после закрыть в вызывающем коде с помошью Stream.close()
     * @param transportMessages Список транспортных сообщений для формирования отчета
     * @param headerDescription
     */
    InputStream generateExcelTransportMessages(List<TransportMessage> transportMessages, String headerDescription) throws IOException;
}

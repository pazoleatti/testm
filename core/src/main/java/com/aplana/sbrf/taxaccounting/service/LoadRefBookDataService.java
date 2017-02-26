package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.ImportCounter;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TransportFileInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;


import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Сервис загрузки ТФ справочников
 *
 * @author Dmitriy Levykin
 */
public interface LoadRefBookDataService {
    /**
     * Загрузка ТФ справочников из ЦАС НСИ (ACCOUNT_PLAN_UPLOAD_DIRECTORY, OKATO_UPLOAD_DIRECTORY, REGION_UPLOAD_DIRECTORY)
     */
    ImportCounter importRefBookNsi(TAUserInfo userInfo, Logger logger, String lockId, boolean isAsync);

    /**
     * Загрузка ТФ справочников из ЦАС НСИ (ACCOUNT_PLAN_UPLOAD_DIRECTORY, OKATO_UPLOAD_DIRECTORY, REGION_UPLOAD_DIRECTORY)
     * Указанный список файлов
     */
    ImportCounter importRefBookNsi(TAUserInfo userInfo, List<String> loadedFileNameList, Logger logger, String lockId, boolean isAsync);

    /**
     * Загрузка ТФ справочников из Diasoft (DIASOFT_UPLOAD_DIRECTORY)
     */
    ImportCounter importRefBookDiasoft(TAUserInfo userInfo, Logger logger, String lockId, boolean isAsync);

    /**
     * Загрузка ТФ справочников из Diasoft (DIASOFT_UPLOAD_DIRECTORY)
     * Указанный список файлов
     */
    ImportCounter importRefBookDiasoft(TAUserInfo userInfo, List<String> loadedFileNameList, Logger logger, String lockId, boolean isAsync);

    /**
     * Загрузка ТФ справочника ФИАС
     * @param userInfo
     * @param loadedFileNameList
     * @param logger
     * @param lockId
     * @param isAsync
     * @return
     */
    ImportCounter importRefBookFias(TAUserInfo userInfo, List<String> loadedFileNameList, Logger logger, String lockId, boolean isAsync);

    /**
     * Проверка доступа к каталогам архива и ошибок
     * @param userInfo
     * @param logger
     * @return
     */
    boolean checkPathArchiveError(TAUserInfo userInfo, Logger logger, String lockId);

    /**
     * Соответствие имени файла ТФ справочников, поставляемых ЦАС НСИ:
     * «Коды ОКАТО»
     * «Коды субъектов Российской Федерации»
     * «План счетов бухгалтерского учета»
     */
    boolean isNSIFile(String name);

    /**
     * Соответствие имени файла ТФ справочников, поставляемых АС Diasoft Custody:
     * «Ценные бумаги»
     * «Эмитенты»
     */
    boolean isDiasoftFile(String name);

    /**
     * TODO Перенести в отдельный сервис
     * Вызов события FormDataEvent.SAVE для скрипта справочника
     * http://jira.aplana.com/browse/SBRFACCTAX-6780
     *
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @param sourceUniqueRecordId версия записи-источника
     * @param saveRecords новые значения для проверки по БЛ
     * @param validDateFrom действует с
     * @param validDateTo действует по
     * @param isNewRecords признак новой записи
     */
    void saveRefBookRecords(long refBookId, Long uniqueRecordId, Long recordId, Long sourceUniqueRecordId, List<Map<String, RefBookValue>> saveRecords, Date validDateFrom,
                            Date validDateTo, boolean isNewRecords, TAUserInfo userInfo, Logger logger);

    void checkImportRefBookTransportData(TAUserInfo userInfo, Logger logger, String lock, Date lockDate, boolean isAsync);

    void checkImportRefBooks(TAUserInfo userInfo, Logger logger, String uuid, boolean isAsync);

    /**
     * Возвращает список всех доступных для обработки файлов из каталога загрузки
     * для справочников Diasoft и Средняя стоимость транспортных средств
     * @param userInfo
     * @param logger
     * @return
     */
    List<TransportFileInfo> getRefBookTransportDataFiles(TAUserInfo userInfo, Logger logger);

    /**
     * Метод для импорта данных из файлов(асинхронная задача)
     * @param logger
     * @param userInfo
     * @param refBookId
     * @param inputStream
     * @param fileName
     * @param lockStateLogger
     */
    void importRefBook(Logger logger, TAUserInfo userInfo, long refBookId, InputStream inputStream, String fileName, Date dateFrom, Date dateTo, LockStateLogger lockStateLogger);

    /**
     * Проверка перед импортом
     * @param refBookId
     * @param fileName
     * @param userInfo
     * @param logger
     */
    void preLoadCheck(long refBookId, String fileName, Date dateFrom, Date dateTo, TAUserInfo userInfo, Logger logger);
}

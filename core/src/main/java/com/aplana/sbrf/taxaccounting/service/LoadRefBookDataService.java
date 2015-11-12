package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.ImportCounter;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TransportFileInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;


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
     * Загрузка ТФ справочника Средняя стоимость транспортных средст (AVG_COST_UPLOAD_DIRECTORY)
     */
    ImportCounter importRefBookAvgCost(TAUserInfo userInfo, Logger logger, String lockId, boolean isAsync);

    /**
     * Загрузка ТФ справочника Средняя стоимость транспортных средст (AVG_COST_UPLOAD_DIRECTORY)
     */
    ImportCounter importRefBookAvgCost(TAUserInfo userInfo, List<String> loadedFileNameList, Logger logger, String lockId, boolean isAsync);

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
     * Соответствие имени файла ТФ справочника "Средняя стоимость транспортных средств"
     */
    boolean isAvgCostFile(String name);

    /**
     * TODO Перенести в отдельный сервис
     * Вызов события FormDataEvent.SAVE для скрипта справочника
     * http://jira.aplana.com/browse/SBRFACCTAX-6780
     *
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @param saveRecords новые значения для проверки по БЛ
     * @param validDateFrom действует с
     * @param validDateTo действует по
     * @param isNewRecords признак новой записи
     */
    void saveRefBookRecords(long refBookId, Long uniqueRecordId, Long recordId, List<Map<String, RefBookValue>> saveRecords, Date validDateFrom,
                            Date validDateTo, boolean isVersion, boolean isNewRecords, TAUserInfo userInfo, Logger logger);

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
}

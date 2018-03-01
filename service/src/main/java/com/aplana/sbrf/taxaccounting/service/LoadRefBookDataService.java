package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.ImportCounter;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;

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
     * Загрузка ТФ справочника ФИАС
     *
     * @param userInfo           пользователь, выполняющий загрузку
     * @param loadedFileNameList список загружаемых файлов?
     * @param logger             логгер со списком сообщений о ходе выполнения процесса
     * @param taskId             идентификатор асинхронной задачи
     * @return
     */
    ImportCounter importRefBookFias(TAUserInfo userInfo, List<String> loadedFileNameList, Logger logger, long taskId);

    /**
     * TODO Перенести в отдельный сервис
     * Вызов события FormDataEvent.SAVE для скрипта справочника
     * http://jira.aplana.com/browse/SBRFACCTAX-6780
     *
     * @param uniqueRecordId       уникальный идентификатор версии записи справочника
     * @param sourceUniqueRecordId версия записи-источника
     * @param saveRecords          новые значения для проверки по БЛ
     * @param validDateFrom        действует с
     * @param validDateTo          действует по
     * @param isNewRecords         признак новой записи
     */
    void saveRefBookRecords(long refBookId, Long uniqueRecordId, Long recordId, Long sourceUniqueRecordId, List<Map<String, RefBookValue>> saveRecords, Date validDateFrom,
                            Date validDateTo, boolean isNewRecords, TAUserInfo userInfo, Logger logger);

    /**
     * Запускает новую задачу на загрузку данных из xml-файла в справочник ФЛ
     *
     * @param userInfo    пользователь, запустивший задачу
     * @param fileName    имя файла
     * @param inputStream данные файла
     * @param logger      логгер
     * @return результат запуска задачи
     */
    ActionResult createTaskToImportXml(TAUserInfo userInfo, String fileName, InputStream inputStream, Logger logger);

    /**
     * Загрузка данных из xml в справочник
     *
     * @param refBookId идентификатор справочника
     * @param blobData  загружаемый файл
     * @param userInfo  пользователь
     * @param logger    логгер
     */
    void importXml(long refBookId, BlobData blobData, TAUserInfo userInfo, Logger logger);

}

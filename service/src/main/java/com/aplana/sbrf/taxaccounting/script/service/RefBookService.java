package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.result.RefBookConfListItem;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;
import com.aplana.sbrf.taxaccounting.service.TransactionLogic;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@ScriptExposed
public interface RefBookService {

    /**
     * Запись справочника по Id
     */
    Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId);

    /**
     * Строковое значение атрибута записи справочника
     */
    String getStringValue(Long refBookId, Long recordId, String alias);

    /**
     * Выполняет указанную логику в новой транзакции
     *
     * @param logic код выполняемый в транзакции
     */
    void executeInNewTransaction(TransactionLogic logic);

    /**
     * Получение значения справочника по Id через кэш
     */
    @SuppressWarnings("unused")
    Map<String, RefBookValue> getRefBookValue(long refBookId, Long recordId,
                                              Map<String, Map<String, RefBookValue>> refBookCache);

    /**
     * Возвращяет страницу данных в таблицу справочников из настройщика
     *
     * @param pagingParams параметры для пагинации
     * @param userInfo     пользователь запустивший операцию
     * @return страницу справочников для настройщика
     */
    PagingResult<RefBookConfListItem> fetchRefBookConfPage(PagingParams pagingParams, TAUserInfo userInfo);

    /**
     * Формирует архив со скриптами и др файлами, связанными со всеми справочниками
     *
     * @param userInfo пользователь запустивший операцию
     * @return данные архива скриптов и xsd
     */
    BlobData exportRefBookConfs(TAUserInfo userInfo);

    /**
     * Выполняет импорт скриптов и др файлов, связанных со справочниками
     *
     * @param inputStream данные
     * @param fileName    имя архива
     * @param userInfo    пользователь запустивший операцию
     * @return uuid ссылку на уведомления с результатом выполнения
     */
    String importRefBookConfs(InputStream inputStream, String fileName, TAUserInfo userInfo);

    /**
     * Получение всех значений справочника АСНУ
     *
     * @return Список значений справочника отсортированых по названию по возрастанию
     */
    List<RefBookAsnu> findAllAsnu();

    /**
     * Получить АСНУ
     *
     * @param asnuId уникальный идентификатор записи
     * @return объект АСНУ
     */
    RefBookAsnu getAsnu(Long asnuId);

    /**
     * Проверка существования записи в ref_book_country по полю code.
     *
     * @param code код страны
     * @return существует ли запись в справочнике
     */
    boolean existsCountryByCode(String code);

    /**
     * Проверка существования записи в ref_book_doc_type по полю code.
     *
     * @param code код документа
     * @return существует ли запись в справочнике
     */
    boolean existsDocTypeByCode(String code);

    /**
     * Проверка существования записи в ref_book_taxpayer_state по полю code.
     *
     * @param code код статуса налогоплательщика
     * @return существует ли запись в справочнике
     */
    boolean existsTaxpayerStateByCode(String code);
}

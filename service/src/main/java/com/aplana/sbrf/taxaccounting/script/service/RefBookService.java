package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.result.RefBookConfListItem;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;
import com.aplana.sbrf.taxaccounting.service.TransactionLogic;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
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
     * Числовое значение атрибута записи справочника
     */
    Number getNumberValue(Long refBookId, Long recordId, String alias);

    /**
     * Датированное значение атрибута записи справочника
     */
    Date getDateValue(Long refBookId, Long recordId, String alias);

    /**
     * Разыменование строк НФ
     */
    @SuppressWarnings("unused")
    void dataRowsDereference(Logger logger, Collection<DataRow<Cell>> dataRows, List<Column> columns);

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
     * Получить выборку пользователей для представления "Список пользователей"
     *
     * @param filter фильтер
     * @return возвращает страницу со списком пользователей
     */
    PagingResult<TAUserView> getUsersByFilter(MembersFilterData filter);

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
     * @param asnuId    уникальный идентификатор записи
     * @return  объект АСНУ
     */
    RefBookAsnu getAsnu(Long asnuId);

}
package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecordVersion;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Дао для справочника ОКТМО
 *
 * @author auldanov on 03.02.14.
 */
public interface RefBookOktmoDao {
    public static final Long REF_BOOK_ID = 96L;

    static final String TABLE_NAME = "REF_BOOK_OKTMO";

    /**
     * Получение записей справочника
     *
     * @param version
     * @param pagingParams
     * @param filter
     * @param sortAttribute
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending);

    /**
     * Получение записи справочника по recordId
     * @param recordId
     * @return
     */
    Map<String, RefBookValue> getRecordData(Long recordId);

    /**
     * Перечень версий записей за период
     * @return
     */
    List<Date> getVersions(Date startDate, Date endDate);

    /**
     * Создание новых записей в справочнике
     *
     * @param version
     * @param records
     */
    void insertRecords(Date version, List<Map<String, RefBookValue>> records);

    /**
     * Загружает данные иерархического справочника на определенную дату актуальности
     *
     *
     * @param parentRecordId код родительского элемента
     * @param version дата актуальности
     * @param pagingParams определяет параметры запрашиваемой страницы данных
     * @param filter условие фильтрации строк
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

    /**
     * Удаление всех записей справочника
     * @param version Дата удаления записей
     *
     * Вместо этого метода надо использовать {@link RefBookDataProvider#deleteAllRecords}
     */
    void deleteAllRecords(Date version);

    /**
     * Возвращает все версии указанной записи справочника
     * Получается запись по id, выбирается его record_id,
     * по record_id возвращаются все версии
     *
     * @param id уникальный идентификатор записи, все версии которой будут получены
     * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecordVersions(Long id, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

    /**
     * Возвращает информацию о версии записи справочника
     * @param id уникальный идентификатор версии записи справочника
     * @return
     */
    RefBookRecordVersion getRecordVersionInfo(Long id);

    /**
     * Возвращает количество существующих версий для элемента справочника
     * @param id уникальный идентификатор версии записи справочника
     * @return
     */
    int getRecordVersionsCount(Long id);
}

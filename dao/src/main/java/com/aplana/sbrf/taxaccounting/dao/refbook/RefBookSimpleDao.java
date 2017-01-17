package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecordVersion;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DAO редактируемых справочников с хранением версии, находящихся в отдельных таблицах
 */
public interface RefBookSimpleDao {
    /**
     * Загружает данные справочника из отдельной таблицы на определенную дату актуальности
     * @param tableName название таблицы
     * @param refBookId код справочника
     * @param version дата актуальности
     * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @param isSortAscending признак сортировки по возрастанию
     * @return список записей
     */
    PagingResult<Map<String, RefBookValue>> getRecords(String tableName, Long refBookId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending);

    /**
     * Загружает данные иерархического справочника на определенную дату актуальности
     * @param tableName название таблицы
     * @param parentRecordId код родительского элемента
     * @param version дата актуальности
     * @param pagingParams определяет параметры запрашиваемой страницы данных
     * @param filter условие фильтрации строк
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return список записей
     */
    PagingResult<Map<String, RefBookValue>> getChildrenRecords(String tableName, Long refBookId, Date version, Long parentRecordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

    /**
     * Получение row_num записи по заданным параметрам
     * @param refBookId код справочника
     * @param version дата актуальности
     * @param recordId идентификатор искомой записи
     * @param filter условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return номер записи
     */
    Long getRowNum(@NotNull Long refBookId, Date version, Long recordId,
                   String filter, RefBookAttribute sortAttribute, boolean isSortAscending);

    /**
     * Получает уникальные идентификаторы записей, удовлетворяющих условиям фильтра
     * @param version дата актуальности
     * @param filter условие фильтрации строк. Может быть не задано
     * @return список идентификаторов
     */
    List<Long> getUniqueRecordIds(Long refBookId, String tableName, Date version, String filter);

    /**
     * Получает количество уникальных записей, удовлетворяющих условиям фильтра
     * @param version дата актуальности
     * @param filter условие фильтрации строк. Может быть не задано
     * @return количество
     */
    int getRecordsCount(Long refBookId, String tableName, Date version, String filter);

    /**
     * Возвращает информацию по версии записи справочника
     *
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @return версия
     */
    RefBookRecordVersion getRecordVersionInfo(String tableName, Long uniqueRecordId);

    /**
     * Перечень версий записей за период
     * @param tableName название таблицы
     * @return список дат - версий
     */
    List<Date> getVersions(String tableName, Date startDate, Date endDate);

    /**
     * Возвращает количество существующих версий для элемента справочника
     * @param tableName название таблицы
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @return количество версий
     */
    int getRecordVersionsCount(String tableName, Long uniqueRecordId);
}

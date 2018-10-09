package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.result.RefBookConfListItem;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Дао для версионных справочников.
 * <br />
 * При получении данные справочника оформляются в виде списка строк. Каждая строка
 * представляет собой набор пар "псевдоним атрибута"-"значение справочника". В списке атрибутов есть предопределенный -
 * это "id" - уникальный код строки. В рамках одного справочника псевдонимы повторяться не могут.
 */
public interface RefBookDao {

    /**
     * Загружает метаданные справочника
     *
     * @param id код справочника
     */
    RefBook get(Long id);

    /**
     * Возвращает список всех видимых справочников
     */
    List<RefBook> findAllVisible();

    /**
     * Поиск видимых справочников по названию.
     *
     * @param name         поисковая строка
     * @param pagingParams параметры сортировки и пагинации
     * @return список искомых справочников
     */
    List<RefBookShortInfo> findAllVisibleShortInfo(String name, PagingParams pagingParams);

    /**
     * Возвращяет страницу данных в таблицу справочников из настройщика
     *
     * @param pagingParams параметры для пагинации
     * @return страницу справочников для настройщика
     */
    PagingResult<RefBookConfListItem> fetchRefBookConfPage(PagingParams pagingParams);

    /**
     * Ищет справочник по коду атрибута
     *
     * @param attributeId код атрибута, входящего в справочник
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если справочник не найден
     */
    RefBook getByAttribute(Long attributeId);

    /**
     * Проверяет действуют ли записи справочника
     *
     * @param recordIds уникальные идентификаторы записей справочника
     * @return список id записей при проверке которых были обнаружены ошибки + код ошибки
     */
    List<ReferenceCheckResult> getInactiveRecords(String tableName, @NotNull List<Long> recordIds);

    /**
     * По коду справочника возвращает набор его атрибутов
     *
     * @param refBookId код справочника
     * @return набор атрибутов
     */
    List<RefBookAttribute> getAttributes(Long refBookId);

    /**
     * Устанавливает SCRIPT_ID для справочника
     *
     * @param refBookId идентификатор справочника
     * @param scriptId  идентификатор скрипта
     */
    void updateScriptId(Long refBookId, String scriptId);

    /**
     * Изменяет ид xsd файла
     *
     * @param refBookId идентификатор справочника
     * @param xsdId     идентификатор xsd файла
     */
    void updateXsdId(Long refBookId, String xsdId);

    /**
     * Перегруженная функция с восходящей сортировкой по умолчанию
     */
    PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId,
                                                       String tableName,
                                                       PagingParams pagingParams,
                                                       String filter,
                                                       RefBookAttribute sortAttribute,
                                                       String whereClause);

    /**
     * Получает данные справочника по sql-запросу, сформированному ранее
     *
     * @param ps      объект с sql-запросом и его параметрами
     * @param refBook справочник
     */
    List<Map<String, RefBookValue>> getRecordsData(PreparedStatementData ps, RefBook refBook);

    /**
     * Возвращает row_num для элемента справочника
     */
    Long getRowNum(Long refBookId,
                   String tableName,
                   Long recordId,
                   String filter,
                   RefBookAttribute sortAttribute,
                   boolean isSortAscending,
                   String whereClause);

    /**
     * Возвращает row_num для элемента справочника
     */
    Long getRowNum(PreparedStatementData ps, Long recordId);

    /**
     * Получение данных справочника
     *
     * @param refBookId       идентификатор справочника
     * @param tableName       название таблицы справочника в БД
     * @param pagingParams    параметры пэйджинга
     * @param filter          фильтрация данных
     * @param sortAttribute   параметры сортировки
     * @param isSortAscending направление сортировки
     * @param whereClause     дополнительное условие фильтрации
     */
    PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId,
                                                       String tableName,
                                                       PagingParams pagingParams,
                                                       String filter,
                                                       RefBookAttribute sortAttribute,
                                                       boolean isSortAscending,
                                                       String whereClause);

    List<Long> getUniqueRecordIds(Long refBookId, String tableName, String filter);

    /**
     * Количество записей в выборке
     */
    Integer getRecordsCount(PreparedStatementData ps);

    /**
     * Изменение периода актуальности для указанной версии
     *
     * @param tableName      название таблицы
     * @param uniqueRecordId уникальный идентификатор версии записи
     * @param version        новая дата начала актуальности
     */
    void updateVersionRelevancePeriod(String tableName, Long uniqueRecordId, Date version);

    Map<String, RefBookValue> getRecordData(Long refBookId, String tableName, Long id);

    /**
     * Удаляет указанные версии записи из справочника
     *
     * @param uniqueRecordIds список идентификаторов версий записей, которые будут удалены
     * @param isDelete        если true, запись удаляется физически, иначе проставляется status = -1
     */
    void deleteRecordVersions(String tableName, @NotNull List<Long> uniqueRecordIds, boolean isDelete);

    /**
     * Разыменование набора ссылок для простых справочников: один справочник - одна таблица
     *
     * @param tableName   название таблицы с данными
     * @param attributeId идентификатор атрибута-ссылки
     * @param recordIds   перечень ссылок
     * @return ref_book_record.id - ref_book_value
     */
    Map<Long, RefBookValue> dereferenceValues(String tableName, Long attributeId, Collection<Long> recordIds);

    /**
     * Проверяет, существует ли указанный справочник
     *
     * @param refBookId идентификатор справочника
     * @return все записи существуют?
     */
    boolean isRefBookExist(long refBookId);

    PagingResult<Map<String, RefBookValue>> getRecordsWithVersionInfo(RefBook refBook,
                                                                      Date version,
                                                                      PagingParams pagingParams,
                                                                      String filter,
                                                                      RefBookAttribute sortAttribute,
                                                                      String direction);
}

package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.QueryBuilder;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookSimple;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.refbook.ReferenceCheckResult;
import com.aplana.sbrf.taxaccounting.model.result.RefBookConfListItem;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Дао для версионных справочников.
 * <br />
 * При получении данные справочника оформляются в виде списка строк. Каждая строка
 * представляет собой набор пар "псевдоним атрибута"-"значение справочника". В списке атрибутов есть предопределенный -
 * это "id" - уникальный код строки. В рамках одного справочника псевдонимы повторяться не могут.
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 04.07.13 12:25
 */
public interface RefBookDao {

    /**
     * Загружает метаданные справочника
     *
     * @param id код справочника
     * @return
     */
    RefBook get(@NotNull Long id);

    /**
     * Возвращает список всех справочников
     */
    List<RefBook> fetchAll();

    /**
     * Возвращает список всех видимых справочников
     */
    List<RefBook> fetchAllVisible();

    /**
     * Возвращает список всех невидимых справочников
     */
    List<RefBook> fetchAllInvisible();

    /**
     * Поиск видимых справочников по названию.
     *
     * @param name поисковая строка
     * @return список искомых справочников
     */
    List<RefBook> searchVisibleByName(String name);

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
     * @return
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если справочник не найден
     */
    RefBook getByAttribute(@NotNull Long attributeId);

    /**
     * Проверяет действуют ли записи справочника
     *
     * @param recordIds уникальные идентификаторы записей справочника
     * @return список id записей при проверке которых были обнаружены ошибки + код ошибки
     */
    List<ReferenceCheckResult> getInactiveRecords(String tableName, @NotNull List<Long> recordIds);

    /**
     * Возвращает атрибут по его коду
     *
     * @param attributeId
     * @return атрибут справочника
     */
    RefBookAttribute getAttribute(@NotNull Long attributeId);

    /**
     * Возвращает атрибут справочника по его алиасу
     *
     * @param refBookId      идентификатор справочника
     * @param attributeAlias алиас аттрибута
     * @return атрибут справочника
     */
    RefBookAttribute getAttribute(@NotNull Long refBookId, @NotNull String attributeAlias);

    /**
     * По коду справочника возвращает набор его атрибутов
     *
     * @param refBookId код справочника
     * @return набор атрибутов
     */
    List<RefBookAttribute> getAttributes(@NotNull Long refBookId);

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

    PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, String tableName, PagingParams pagingParams,
                                                       String filter, RefBookAttribute sortAttribute, String whereClause);

    /**
     * Получает данные справочника по sql-запросу, сформированному ранее
     *
     * @param ps      объект с sql-запросом и его параметрами
     * @param refBook справочник
     * @return
     */
    List<Map<String, RefBookValue>> getRecordsData(PreparedStatementData ps, RefBook refBook);

    /**
     * Получает данные записей справочника замапленные на сущности
     *
     * @param q       объект с sql-запросом и его параметрами
     * @param refBook справочник
     */
    <T extends RefBookSimple> List<T> getMappedRecordsData(QueryBuilder q, RefBook refBook);

    List<Map<String, RefBookValue>> getRecordsWithHasChild(PreparedStatementData ps, RefBook refBook);

    /**
     * row_num
     */
    Long getRowNum(Long refBookId, String tableName, Long recordId, String filter, RefBookAttribute sortAttribute,
                   boolean isSortAscending, String whereClause);

    /**
     * row_num
     */
    Long getRowNum(PreparedStatementData ps, Long recordId);

    PreparedStatementData getSimpleQuery(RefBook refBook, String tableName, RefBookAttribute sortAttribute,
                                         String filter, PagingParams pagingParams, boolean isSortAscending, String whereClause);

    PreparedStatementData getSimpleQuery(RefBook refBook, String tableName, RefBookAttribute sortAttribute, String filter, PagingParams pagingParams, String whereClause);

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
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, String tableName, PagingParams pagingParams,
                                                       String filter, RefBookAttribute sortAttribute, boolean isSortAscending, String whereClause);

    List<Long> getUniqueRecordIds(Long refBookId, String tableName, String filter);

    /**
     * Получает количество уникальных записей, удовлетворяющих условиям фильтра
     *
     * @param refBookId ид справочника
     * @param tableName название таблицы
     * @param filter    условие фильтрации строк. Может быть не задано
     * @return количество
     */
    int getRecordsCount(Long refBookId, String tableName, String filter);

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

    PagingResult<Map<String, RefBookValue>> getRecordsWithVersionInfo(RefBook refBook, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, String direction);
}

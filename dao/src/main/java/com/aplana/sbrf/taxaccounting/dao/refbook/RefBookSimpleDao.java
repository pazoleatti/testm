package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DAO для RefBookSimpleDataProvider, поддерживающий редактируемые версионируемые справочники.
 * Иногда такие справочники могут не поддерживать по отдельности редактирование или версионирование.
 * <p>
 * Справочники должны иметь поля STATUS, VERSION, RECORD_ID
 */
public interface RefBookSimpleDao {

    /**
     * Загружает данные справочника из отдельной таблицы на определенную дату актуальности
     *
     * @param refBook         справочник
     * @param version         дата актуальности
     * @param pagingParams    определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter          условие фильтрации строк. Может быть не задано
     * @param sortAttribute   сортируемый столбец. Может быть не задан
     * @param isSortAscending признак сортировки по возрастанию
     * @return список записей
     */
    PagingResult<Map<String, RefBookValue>> getRecords(RefBook refBook, Date version, PagingParams pagingParams,
                                                       String filter, RefBookAttribute sortAttribute, boolean isSortAscending);

    /**
     * Загружает данные справочника из отдельной таблицы на определенную дату актуальности, замапленные на определенную сущность
     *
     * @param refBook       справочник
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @param direction     навправление сортировки - asc, desc
     * @param pagingParams  определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param columns       список колонок в таблице, по которым выполняется фильтрация. Может быть не задано
     * @param searchPattern шаблон поиска. Может быть не задано
     * @param filter        условие фильтрации строк. Может быть не задано
     * @return список записей
     */
    <T extends RefBookSimple> PagingResult<T> getRecords(RefBook refBook, RefBookAttribute sortAttribute, String direction,
                                                         PagingParams pagingParams, List<String> columns,
                                                         String searchPattern, String filter);

    PagingResult<Map<String, RefBookValue>> getVersionsInPeriod(RefBook refBook, Date versionFrom, Date versionTo, String filter);

    /**
     * Получает запись по уникальному идентификатору
     *
     * @param refBook справочник
     * @param id      уникальный идентификатор записи
     * @return Map, где key - alias атрибута, а value - его значение ({@link RefBookValue})
     */
    Map<String, RefBookValue> getRecordData(final RefBook refBook, final Long id);

    /**
     * Получает запись, замапленную на определенную сущность по уникальному идентификатору
     *
     * @param refBook  справочник
     * @param recordId уникальный идентификатор записи
     * @return запись справочника
     */
    <T extends RefBookSimple> T getRecord(final RefBook refBook, final Long recordId);

    /**
     * Получение структуры Код строки → Строка справочника по списку кодов строк
     *
     * @param refBook   справочник
     * @param recordIds список кодов строк справочника
     */
    Map<Long, Map<String, RefBookValue>> getRecordData(RefBook refBook, List<Long> recordIds);


    /**
     * Получение структуры Код строки → Строка справочника по списку кодов строк
     *
     * @param refBook     справочник
     * @param whereClause список кодов строк справочника
     * @return
     */
    Map<Long, Map<String, RefBookValue>> getRecordDataWhere(RefBook refBook, String whereClause);

    /**
     * Получение структуры Код строки → Строка справочника по списку кодов строк
     *
     * @param refBook     справочник
     * @param whereClause список кодов строк справочника
     * @param version     версия справочника
     * @return
     */
    Map<Long, Map<String, RefBookValue>> getRecordDataVersionWhere(RefBook refBook, String whereClause, Date version);

    /**
     * Получает уникальные идентификаторы записей, удовлетворяющих условиям фильтра
     *
     * @param refBook справочник
     * @param version дата актуальности
     * @param filter  условие фильтрации строк. Может быть не задано
     * @return список идентификаторов
     */
    List<Long> getUniqueRecordIds(RefBook refBook, Date version, String filter);

    /**
     * Получает количество уникальных записей, удовлетворяющих условиям фильтра
     *
     * @param version дата актуальности
     * @param filter  условие фильтрации строк. Может быть не задано
     * @return количество
     */
    int getRecordsCount(RefBook refBook, Date version, String filter);

    /**
     * Возвращает информацию по версии записи справочника
     *
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @return версия
     */
    RefBookRecordVersion getRecordVersionInfo(RefBook refBook, Long uniqueRecordId);

    /**
     * Возвращает идентификатор записи справочника без учета версий
     *
     * @param refBook        справочник
     * @param uniqueRecordId уникальный идентификатор версии записи
     * @return
     */
    Long getRecordId(RefBook refBook, Long uniqueRecordId);

    /**
     * Возвращает все версии из указанной группы версий записи справочника
     *
     * @param refBook       справочник
     * @param recordId      идентификатор группы версий записи справочника
     * @param pagingParams  определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter        условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecordVersionsByRecordId(RefBook refBook, Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

    /**
     * Поиск среди всех элементов справочника (без учета версий) значений уникальных атрибутов, которые бы дублировались с новыми
     * Обеспечение соблюдения уникальности атрибутов в пределах справочника
     *
     * @param refBook        справочник
     * @param uniqueRecordId уникальный идентификатор записи справочника. Может быть null (при создании нового элемента). Используется для исключения из проверки указанного элемента справочника
     * @param record         новые значения полей элемента справочника
     * @return список пар идентификатор записи-имя атрибута, у которых совпали значения уникальных атрибутов
     */
    List<Pair<Long, String>> getMatchedRecordsByUniqueAttributes(RefBook refBook, Long uniqueRecordId,
                                                                 RefBookRecord record);

    /**
     * Проверка на пересечение версий у записей справочника, в которых совпали уникальные атрибуты
     *
     * @param recordPairs записи, у которых совпали уникальные атрибуты
     * @param versionFrom дата начала актуальности новой версии
     * @param versionTo   дата конца актуальности новой версии
     * @return список идентификаторов записей, в которых есть пересечение
     */
    List<Long> checkConflictValuesVersions(RefBook refBook, List<Pair<Long, String>> recordPairs, Date versionFrom, Date versionTo);

    /**
     * Поиск существующих версий, которые могут пересекаться с новой версией
     *
     * @param refBook          справочник
     * @param recordId         идентификатор записи справочника (без учета версий)
     * @param versionFrom      дата начала актуальности новой версии
     * @param versionTo        дата окончания актуальности новой версии
     * @param excludedRecordId идентификатор версии записи справочника, которая исключается из проверки пересечения. Используется только при редактировании
     * @return результат проверки по каждой версии, с которой есть пересечение
     */
    List<CheckCrossVersionsResult> checkCrossVersions(RefBook refBook, Long recordId,
                                                      Date versionFrom, Date versionTo, Long excludedRecordId);

    /**
     * Возвращает дату начала версии следующей за указанной
     *
     * @param refBook справочник
     * @param version дата актуальности
     * @param filter  фильтр для отбора записей. Обязательное поле, т.к записи не фильтруются по RECORD_ID
     * @return дата начала следующей версии
     */
    Date getNextVersion(RefBook refBook, Date version, String filter);

    /**
     * Возвращает данные о версии следующей за указанной
     *
     * @param refBook     справочник
     * @param recordId    идентификатор записи справочника (без учета версий)
     * @param versionFrom дата начала актуальности версии текущей версии, после которой будет выполняться поиск следующей версии
     * @return данные версии
     */
    RefBookRecordVersion getNextVersion(RefBook refBook, Long recordId, Date versionFrom);

    /**
     * Возвращает данные о версии следующей до указанной
     *
     * @param refBook     справочник
     * @param recordId    идентификатор записи справочника (без учета версий)
     * @param versionFrom дата начала актуальности версии текущей версии, после которой будет выполняться поиск следующей версии
     * @return данные версии
     */
    RefBookRecordVersion getPreviousVersion(RefBook refBook, Long recordId, Date versionFrom);

    /**
     * Создает фиктивную запись, являющуюся датой окончания периода актуальности какой то версии
     *
     * @param refBook  справочник
     * @param recordId идентификатор записи справочника без учета версий
     * @param version  версия записи справочника
     */
    void createFakeRecordVersion(RefBook refBook, Long recordId, Date version);

    /**
     * Создает новые версии записи в справочнике.
     * Если задан параметр recordId - то создается новая версия записи справочника
     *
     * @param refBook справочник
     * @param version дата актуальности новых записей
     * @param status  статус записи
     * @param records список новых записей
     * @return идентификатор записи справочника (без учета версий)
     */
    List<Long> createRecordVersion(final RefBook refBook, final Date version, final VersionedObjectStatus status,
                                   final List<RefBookRecord> records);

    /**
     * Возвращает уникальный идентификатор записи, удовлетворяющей указанным условиям
     *
     * @param refBook  справочник
     * @param recordId идентификатор записи справочника (без учета версий)
     * @param version  дата
     * @return уникальный идентификатор записи, удовлетворяющей указанным условиям
     */
    Long findRecord(RefBook refBook, Long recordId, Date version);

    /**
     * Возвращает идентификаторы фиктивных версии, являющихся окончанием указанных версии.
     * Без привязки ко входным параметрам, т.к метод используется просто для удаления по id
     *
     * @param refBook         справочник
     * @param uniqueRecordIds идентификаторы версии записи справочника
     * @return идентификаторы фиктивных версии
     */
    List<Long> getRelatedVersions(RefBook refBook, List<Long> uniqueRecordIds);

    /**
     * Проверяет существование версий записи справочника
     *
     * @param refBook   справочник
     * @param recordIds идентификаторы записей справочника без учета версий
     * @param version   версия записи справочника
     * @return
     */
    boolean isVersionsExist(RefBook refBook, List<Long> recordIds, Date version);

    /**
     * Обновляет значения атрибутов у указанной версии
     *
     * @param refBook        справочник
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @param records        список значений атрибутов
     */
    void updateRecordVersion(RefBook refBook, Long uniqueRecordId, Map<String, RefBookValue> records);

    /**
     * Удаляет все версии записи из справочника
     *
     * @param refBook         справочник
     * @param uniqueRecordIds список идентификаторов записей, все версии которых будут удалены
     */
    void deleteAllRecordVersions(RefBook refBook, List<Long> uniqueRecordIds);
}

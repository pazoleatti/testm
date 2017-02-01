package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

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
     * @param refBookId код справочника
     * @param version дата актуальности
     * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @param isSortAscending признак сортировки по возрастанию
     * @return список записей
     */
    PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, Date version, PagingParams pagingParams,
                                                       String filter, RefBookAttribute sortAttribute, boolean isSortAscending);

    Map<String, RefBookValue> getRecordData(RefBook refBook, Long id);

    /**
     * Получение структуры Код строки → Строка справочника по списку кодов строк
     *
     * @param refBook справочник
     * @param recordIds список кодов строк справочника
     */
    Map<Long, Map<String, RefBookValue>> getRecordData(@NotNull RefBook refBook, @NotNull List<Long> recordIds);

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
    PagingResult<Map<String, RefBookValue>> getChildrenRecords(String tableName, Long refBookId, Date version, Long parentRecordId,
                                                               PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

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

    /**
     * Возвращает все версии указанной записи справочника
     * @param refBook идентификатор справочник
     * @param uniqueRecordId уникальный идентификатор записи, все версии которой будут получены
     * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecordVersions(RefBook refBook, Long uniqueRecordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending);

    /**
     * Возвращает идентификатор записи справочника без учета версий
     * @param uniqueRecordId уникальный идентификатор версии записи
     * @return
     */
    Long getRecordId(String tableName, @NotNull Long uniqueRecordId);

    /**
     * Возвращает все версии из указанной группы версий записи справочника
     * @param refBookId идентификатор справочника
     * @param recordId идентификатор группы версий записи справочника
     * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return
     */
    PagingResult<Map<String,RefBookValue>> getRecordVersionsByRecordId(Long refBookId, Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

    /**
     *
     * Поиск среди всех элементов справочника (без учета версий) значений уникальных атрибутов, которые бы дублировались с новыми
     * Обеспечение соблюдения уникальности атрибутов в пределах справочника
     *
     * @param refBook справочник
     * @param uniqueRecordId уникальный идентификатор записи справочника. Может быть null (при создании нового элемента). Используется для исключения из проверки указанного элемента справочника
     * @param record новые значения полей элемента справочника
     * @return список пар идентификатор записи-имя атрибута, у которых совпали значения уникальных атрибутов
     */
    List<Pair<Long,String>> getMatchedRecordsByUniqueAttributes(@NotNull RefBook refBook, Long uniqueRecordId,
                                                                @NotNull RefBookRecord record);

    /**
     * Проверка на пересечение версий у записей справочника, в которых совпали уникальные атрибуты
     * @param recordPairs записи, у которых совпали уникальные атрибуты
     * @param versionFrom дата начала актуальности новой версии
     * @param versionTo дата конца актуальности новой версии
     * @return список идентификаторов записей, в которых есть пересечение
     */
    public List<Long> checkConflictValuesVersions(@NotNull RefBook refBook, List<Pair<Long, String>> recordPairs,
                                                  Date versionFrom, Date versionTo);

    /**
     * Проверяет существуют ли конфликты в датах актуальности у проверяемых записей и их родительских записей (в иерархических справочниках)
     * @param versionFrom дата начала актуальности
     * @param records проверяемые записи
     */
    List<Pair<Long, Integer>> checkParentConflict(@NotNull RefBook refBook, Date versionFrom, List<RefBookRecord> records);

    /**
     * Поиск существующих версий, которые могут пересекаться с новой версией
     * @param refBook справочник
     * @param recordId идентификатор записи справочника (без учета версий)
     * @param versionFrom дата начала актуальности новой версии
     * @param versionTo дата окончания актуальности новой версии
     * @param excludedRecordId идентификатор версии записи справочника, которая исключается из проверки пересечения. Используется только при редактировании
     * @return результат проверки по каждой версии, с которой есть пересечение
     */
    List<CheckCrossVersionsResult> checkCrossVersions(RefBook refBook, Long recordId,
                                                      Date versionFrom, Date versionTo, Long excludedRecordId);

    /**
     * Проверяет использование записи как родителя для дочерних
     * @param refBook справочник
     * @param recordId уникальный идентификатор записи
     * @param versionFrom дата начала актуальности новой версии
     * @return список пар <дата начала - дата окончания> периода актуальности обнаруженных дочерних записей
     */
    List<Pair<Date, Date>> isVersionUsedLikeParent(@NotNull RefBook refBook, @NotNull Long recordId, @NotNull Date versionFrom);

    /**
     * Возвращает дату начала версии следующей за указанной
     * @param refBook справочник
     * @param version дата актуальности
     * @param filter фильтр для отбора записей. Обязательное поле, т.к записи не фильтруются по RECORD_ID
     * @return дата начала следующей версии
     */
    Date getNextVersion(@NotNull RefBook refBook, Date version, @NotNull String filter);

    /**
     * Возвращает данные о версии следующей за указанной
     * @param refBook справочник
     * @param recordId идентификатор записи справочника (без учета версий)
     * @param versionFrom дата начала актуальности версии текущей версии, после которой будет выполняться поиск следующей версии
     * @return данные версии
     */
    RefBookRecordVersion getNextVersion(@NotNull RefBook refBook, @NotNull Long recordId, @NotNull Date versionFrom);

    /**
     * Создает фиктивную запись, являющуюся датой окончания периода актуальности какой то версии
     * @param refBook справочник
     * @param recordId идентификатор записи справочника без учета версий
     * @param version версия записи справочника
     */
    void createFakeRecordVersion(@NotNull RefBook refBook, @NotNull Long recordId, @NotNull Date version);

    /**
     * Создает новые версии записи в справочнике.
     * Если задан параметр recordId - то создается новая версия записи справочника
     * @param refBook справочник
     * @param version дата актуальности новых записей
     * @param status статус записи
     * @param records список новых записей
     * @return идентификатор записи справочника (без учета версий)
     */
    List<Long> createRecordVersion(@NotNull RefBook refBook, @NotNull Date version, @NotNull VersionedObjectStatus status,
                                   List<RefBookRecord> records);

    /**
     * Проверяет существует ли циклическая зависимость для указанных записей справочника
     * Если среди дочерних элементов указанной записи существует указанный родительский элемент, то существует цикл
     * @param uniqueRecordId идентификатор записи
     * @param parentRecordId идентификатор родительской записи
     * @return циклическая зависимость существует?
     */
    boolean hasLoops(Long uniqueRecordId, Long parentRecordId);
}

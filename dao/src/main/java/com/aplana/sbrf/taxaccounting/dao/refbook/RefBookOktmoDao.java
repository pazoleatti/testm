package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Дао для октмо
 *
 * @author auldanov, dloshkarev
 */
public interface RefBookOktmoDao {

    /**
     * Загружает данные справочника на определенную дату актуальности
     * @param tableName название таблицы
     * @param refBookId код справочника
     * @param version дата актуальности
     * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecords(String tableName, Long refBookId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending);

    /**
     * Получение row_num записи по заданным параметрам
     * @param tableName название таблицы
     * @param refBookId код справочника
     * @param version дата актуальности
     * @param recordId id записи справочника
     * @param filter условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return
     */
    Long getRowNum(String tableName, Long refBookId, Date version, Long recordId, String filter, RefBookAttribute sortAttribute, boolean isSortAscending);

    /**
     * Получение записи справочника по recordId
     * @param recordId
     * @return
     */
    Map<String, RefBookValue> getRecordData(String tableName, Long refBookId, Long recordId);

    /**
     * Возвращает все версии указанной записи справочника
     * @param tableName название таблицы
     * @param refBookId идентификатор справочника
     * @param uniqueRecordId уникальный идентификатор записи, все версии которой будут получены
     * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecordVersions(String tableName, Long refBookId, Long uniqueRecordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending);

    /**
     * Возвращает количество существующих версий для элемента справочника
     * @param tableName название таблицы
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @return
     */
    int getRecordVersionsCount(String tableName, Long uniqueRecordId);

    /**
     * Перечень версий записей за период
     * @param tableName название таблицы
     * @return
     */
    List<Date> getVersions(String tableName, Date startDate, Date endDate);

    /**
     * Возвращает идентификатор записи справочника без учета версий
     * @param tableName название таблицы
     * @param uniqueRecordId уникальный идентификатор версии записи
     * @return
     */
    Long getRecordId(String tableName, Long uniqueRecordId);

    /**
     * Загружает данные иерархического справочника на определенную дату актуальности
     * @param tableName название таблицы
     * @param parentRecordId код родительского элемента
     * @param version дата актуальности
     * @param pagingParams определяет параметры запрашиваемой страницы данных
     * @param filter условие фильтрации строк
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getChildrenRecords(String tableName, Long refBookId, Date version, Long parentRecordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

    /**
     * Возвращает информацию о версии записи справочника
     * @param tableName название таблицы
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @return
     */
    RefBookRecordVersion getRecordVersionInfo(String tableName, Long uniqueRecordId);

    /**
     * Возвращает дату начала периода актуальности для указанных версий записей справочника
     * @param tableName название таблицы
     * @param uniqueRecordIds уникальные идентификаторы версий записей справочника
     * @return идентификатор версии - дата начала периода актуальности
     */
    Map<Long,Date> getRecordsVersionStart(String tableName, List<Long> uniqueRecordIds);

    /**
     *
     * Поиск среди всех элементов справочника (без учета версий) значений уникальных атрибутов, которые бы дублировались с новыми
     * Обеспечение соблюдения уникальности атрибутов в пределах справочника
     * @param tableName название таблицы
     * @param attributes атрибуты справочника
     * @param records новые значения полей элемента справочника
     * @return список пар идентификатор записи-имя атрибута, у которых совпали значения уникальных атрибутов
     */
    List<Pair<Long,String>> getMatchedRecordsByUniqueAttributes(String tableName, List<RefBookAttribute> attributes, List<RefBookRecord> records);

    /**
     * Проверка на пересечение версий у записей справочника, в которых совпали уникальные атрибуты
     * @param tableName название таблицы
     * @param recordPairs записи, у которых совпали уникальные атрибуты
     * @param versionFrom дата начала актуальности новой версии
     * @param versionTo дата конца актуальности новой версии
     */
    void checkConflictValuesVersions(String tableName, List<Pair<Long,String>> recordPairs, Date versionFrom, Date versionTo);

    /**
     * Проверяет существование версий записи справочника
     * @param tableName название таблицы
     * @param recordIds идентификаторы записей справочника без учета версий
     * @param version версия записи справочника
     * @return
     */
    boolean isVersionsExist(String tableName, List<Long> recordIds, Date version);

    /**
     * Поиск существующих версий, которые могут пересекаться с новой версией
     * @param tableName название таблицы
     * @param recordId идентификатор записи справочника (без учета версий)
     * @param versionFrom дата начала актуальности новой версии
     * @param versionTo дата окончания актуальности новой версии
     * @param excludedRecordId идентификатор версии записи справочника, которая исключается из проверки пересечения. Используется только при редактировании
     * @return результат проверки по каждой версии, с которой есть пересечение
     */
    List<CheckCrossVersionsResult> checkCrossVersions(String tableName, Long recordId, Date versionFrom, Date versionTo, Long excludedRecordId);

    /**
     * Проверяет есть ли ссылки на версию в каких либо точках запроса
     * @param tableName название таблицы
     * @param refBookId идентификатор справочника
     * @param uniqueRecordId уникальный идентификатор версии записи
     * @param versionFrom дата начала актуальности новой версии
     * @return есть ссылки на версию?
     */
    boolean isVersionUsed(String tableName, Long refBookId, Long uniqueRecordId, Date versionFrom);

    /**
     * Проверяет есть ли ссылки на версию в каких либо точках запроса
     * @param tableName название таблицы
     * @param refBookId идентификатор справочника
     * @param uniqueRecordIds список идентификаторов версий записей
     * @return есть ссылки на версию?
     */
    boolean isVersionUsed(String tableName, Long refBookId, List<Long> uniqueRecordIds);

    /**
     * Возвращает данные о версии следующей за указанной
     * @param tableName название таблицы
     * @param recordId идентификатор записи справочника (без учета версий)
     * @param versionFrom дата начала актуальности версии текущей версии, после которой будет выполняться поиск следующей версии
     * @return данные версии
     */
    RefBookRecordVersion getNextVersion(String tableName, Long recordId, Date versionFrom);

    /**
     * Создает фиктивную запись, являющуюся датой окончания периода актуальности какой то версии
     * @param tableName название таблицы
     * @param recordId идентификатор записи справочника без учета версий
     * @param version версия записи справочника
     */
    void createFakeRecordVersion(String tableName, Long recordId, Date version);

    /**
     * Создает новые версии записи в справочнике.
     * Если задан параметр recordId - то создается новая версия записи справочника
     * @param tableName название таблицы
     * @param version дата актуальности новых записей
     * @param status статус записи
     * @param records список новых записей
     * @return идентификатор записи справочника (без учета версий)
     */
    List<Long> createRecordVersion(String tableName, Long refBookId, Date version, VersionedObjectStatus status, List<RefBookRecord> records);

    /**
     * Обновляет значения атрибутов у указанной версии
     * @param tableName название таблицы
     * @param refBookId код справочника
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @param records список значений атрибутов
     */
    void updateRecordVersion(String tableName, Long refBookId, Long uniqueRecordId, Map<String, RefBookValue> records);

    /**
     * Возвращает идентификаторы фиктивных версии, являющихся окончанием указанных версии
     * @param tableName название таблицы
     * @param uniqueRecordIds идентификаторы версии записи справочника
     * @return идентификаторы фиктивных версии
     */
    List<Long> getRelatedVersions(String tableName, List<Long> uniqueRecordIds);

    /**
     * Получает идентификатор записи, который имеет наименьшую дату начала актуальности для указанной версии
     * @param tableName название таблицы
     * @param refBookId идентификатор справочника
     * @param uniqueRecordId идентификатор версии записи справочника
     * @return
     */
    Long getFirstRecordId(String tableName, Long refBookId, Long uniqueRecordId);

    /**
     * Проверяет есть ли дочерние элементы для указанных версий записей
     * @param tableName название таблицы
     * @param uniqueRecordIds уникальные идентификаторы версий записей справочника
     * @return возвращает список дат начала периода актуальности, для версий у которых были найдены дочерние элементы. Либо null, если их нет
     */
    List<Date> hasChildren(String tableName, List<Long> uniqueRecordIds);

    /**
     * Возвращает значения атрибутов для указанных записей
     * @param attributePairs список пар идентификатор записи-идентификатор атрибута
     * @return
     *      ключ - пара идентификатор записи-идентификатор атрибута
     *      значение - строковое представление значения атрибута
     */
    Map<RefBookAttributePair,String> getAttributesValues(List<RefBookAttributePair> attributePairs);

    /**
     * Получает уникальные идентификаторы записей, удовлетворяющих условиям фильтра
     * @param version дата актуальности
     * @param filter условие фильтрации строк. Может быть не задано
     * @return список идентификаторов
     */
    List<Long> getUniqueRecordIds(Long oktmoRefBookId, String tableName, Date version, String filter);
    /**
     * Получает количество уникальных записей, удовлетворяющих условиям фильтра
     * @param version дата актуальности
     * @param filter условие фильтрации строк. Может быть не задано
     * @return количество
     */
    int getRecordsCount(Long oktmoRefBookId, String tableName, Date version, String filter);

    /**
     * Проверяет действуют ли записи справочника в указанном периоде
     * @param recordIds уникальные идентификаторы записей справочника
     * @param periodFrom начало периода
     * @param periodTo окончание периода
     * @return список id записей при проверке которых были обнаружены ошибки + код ошибки
     */
    Map<Long, CheckResult> getInactiveRecordsInPeriod(String tableName, @NotNull List<Long> recordIds, @NotNull Date periodFrom, Date periodTo);

    /**
     * Проверяет, существуют ли версии элемента справочника, удовлетворяющие указанному фильтру
     * @param version дата актуальности. Может быть null - тогда не учитывается
     * @param needAccurateVersion признак того, что нужно точное совпадение по дате начала действия записи
     * @param filter фильтр для отбора записей
     * @return пары идентификатор версии элемента - идентификатор элемента справочника
     */
    List<Pair<Long, Long>> getRecordIdPairs(String tableName, @NotNull Long refBookId, Date version, Boolean needAccurateVersion, String filter);

    boolean isRecordsExist(List<Long> uniqueRecordIds);
}

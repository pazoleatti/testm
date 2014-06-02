package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

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
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 04.07.13 12:25
 */

public interface RefBookDao {

	/**
	 * Загружает метаданные справочника
	 * @param id код справочника
	 * @return
	 */
	RefBook get(Long id);

	/**
	 * Загружает список всех справочников
	 * @return
     * @param typeId тип справочника
     *               0 - линейный
     *               1 - иерархический
     *               null - все
	 */
	List<RefBook> getAll(Integer typeId);

	/**
	 * Загружает список всех справочников
	 * @return
     * @param typeId тип справочника
	 *               0 - линейный
	 *               1 - иерархический
     *               null - все
	 */
	List<RefBook> getAllVisible(Integer typeId);

	/**
	 * Ищет справочник по коду атрибута
	 * @param attributeId код атрибута, входящего в справочник
	 * @throws com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException если справочник не найден
	 * @return
	 */
	RefBook getByAttribute(Long attributeId);

	/**
	 * Загружает данные справочника на определенную дату актуальности
	 * @param refBookId код справочника
	 * @param version дата актуальности
	 * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
	 * @param filter условие фильтрации строк. Может быть не задано
	 * @param sortAttribute сортируемый столбец. Может быть не задан
	 * @return
	 */
	PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, Date version, PagingParams pagingParams,
		String filter, RefBookAttribute sortAttribute);

    /**
	 * Перегруженный вариант метода, для сохранения обратной совместимости
	 * @param refBookId код справочника
	 * @param version дата актуальности
	 * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
	 * @param filter условие фильтрации строк. Может быть не задано
	 * @param sortAttribute сортируемый столбец. Может быть не задан
	 * @return
	 */
	PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, Date version, PagingParams pagingParams,
		String filter, RefBookAttribute sortAttribute, boolean isSortAscending);

    /**
     * Проверяет, существуют ли версии элемента справочника, удовлетворяющие указанному фильтру
     * @param version дата актуальности. Может быть null - тогда не учитывается
     * @param filter
     * @return пары идентификатор версии элемента - идентификаторидентификатор элемента справочника
     */
    List<Pair<Long, Long>> getRecordIdPairs(Long refBookId, Date version, String filter);

	/**
	 * Загружает данные иерархического справочника на определенную дату актуальности
	 *
	 * @param refBookId код справочника
	 * @param parentRecordId код родительского элемента
	 * @param version дата актуальности
	 * @param pagingParams определяет параметры запрашиваемой страницы данных
	 * @param filter условие фильтрации строк
	 * @param sortAttribute сортируемый столбец. Может быть не задан
	 * @return
	 */
	PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long refBookId, Long parentRecordId, Date version,
		PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

	/**
	 * По коду возвращает строку справочника
	 * @param refBookId код справочника
	 * @param recordId код строки справочника
	 * @return
	 * @throws org.springframework.dao.EmptyResultDataAccessException если строка не найдена
	 */
	Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId);

    /**
     * Получение структуры Код строки → Строка справочника по списку кодов строк
     *
     * @param refBookId код справочника
     * @param recordIds список кодов строк справочника
     */
    Map<Long, Map<String, RefBookValue>> getRecordData(Long refBookId, List<Long> recordIds);

    /**
	 * Создает новые версии записи в справочнике.
     * Если задан параметр recordId - то создается новая версия записи справочника
	 * @param refBookId код справочника
	 * @param version дата актуальности новых записей
     * @param status статус записи
	 * @param records список новых записей
     * @return идентификатор записи справочника (без учета версий)
	 */
	List<Long> createRecordVersion(Long refBookId, Date version, VersionedObjectStatus status, List<RefBookRecord> records);

    /**
     * Создает фиктивную запись, являющуюся датой окончания периода актуальности какой то версии
     * @param refBookId код справочника
     * @param recordId идентификатор записи справочника без учета версий
     * @param version версия записи справочника
     */
    void createFakeRecordVersion(Long refBookId, Long recordId, Date version);

    /**
     * Обновляет значения атрибутов у указанной версии
     * @param refBookId код справочника
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @param records список значений атрибутов
     */
    void updateRecordVersion(Long refBookId, Long uniqueRecordId, Map<String, RefBookValue> records);

    /**
     * Проверяет существование версий записи справочника
     * @param refBookId идентификатор справочника
     * @param recordIds идентификаторы записей справочника без учета версий
     * @param version версия записи справочника
     * @return
     */
    boolean isVersionsExist(Long refBookId, List<Long> recordIds, Date version);

    /**
     * Проверка и поиск Id записи по:
     * @param refBookId Id справочника
     * @param version Версия
     * @param rowId Id строки справочника
     * @return Id первой найденной записи
     */
    Long checkRecordUnique(Long refBookId, Date version, Long rowId);

    /**
     * Значение справочника по Id записи и Id атрибута
     * @param recordId
     * @param attributeId
     * @return
     */
    RefBookValue getValue(Long recordId, Long attributeId);

    /**
     * Возвращает информацию по версии записи справочника
     *
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @return
     */
    RefBookRecordVersion getRecordVersionInfo(Long uniqueRecordId);

    /**
     * Возвращает количество существующих версий для элемента справочника
     * @param refBookId идентификатор справочника
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @return
     */
    int getRecordVersionsCount(Long refBookId, Long uniqueRecordId);

    /**
     * Возвращает все версии указанной записи справочника
     * @param refBookId идентификатор справочника
     * @param uniqueRecordId уникальный идентификатор записи, все версии которой будут получены
     * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return
     */
    PagingResult<Map<String,RefBookValue>> getRecordVersions(Long refBookId, Long uniqueRecordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

    /**
     * Возвращает значения уникальных атрибутов справочника
     * @param refBookId идентификатор справочника
     * @param recordId идентификатор записи
     * @return
     */
    List<Pair<RefBookAttribute, RefBookValue>> getUniqueAttributeValues(Long refBookId, Long recordId);

    /**
     * По коду справочника возвращает набор его атрибутов
     *
     * @param refBookId код справочника
     * @return набор атрибутов
     */
    List<RefBookAttribute> getAttributes(Long refBookId);

    /**
     *
     * Поиск среди всех элементов справочника (без учета версий) значений уникальных атрибутов, которые бы дублировались с новыми
     * Обеспечение соблюдения уникальности атрибутов в пределах справочника
     *
     * @param refBookId идентификатор справочника
     * @param uniqueRecordId уникальный идентификатор записи справочника. Может быть null (при создании нового элемента). Используется для исключения из проверки указанного элемента справочника
     * @param attributes атрибуты справочника
     * @param records новые значения полей элемента справочника
     * @return список пар идентификатор записи-имя атрибута, у которых совпали значения уникальных атрибутов
     */
    List<Pair<Long,String>> getMatchedRecordsByUniqueAttributes(Long refBookId, Long uniqueRecordId, List<RefBookAttribute> attributes, List<RefBookRecord> records);

    /**
     * Поиск существующих версий, которые могут пересекаться с новой версией
     * @param refBookId идентификатор справочника
     * @param recordId идентификатор записи справочника (без учета версий)
     * @param versionFrom дата начала актуальности новой версии
     * @param versionTo дата окончания актуальности новой версии
     * @param excludedRecordId идентификатор версии записи справочника, которая исключается из проверки пересечения. Используется только при редактировании
     * @return результат проверки по каждой версии, с которой есть пересечение
     */
    List<CheckCrossVersionsResult> checkCrossVersions(Long refBookId, Long recordId, Date versionFrom, Date versionTo, Long excludedRecordId);

    /**
     * Проверка на пересечение версий у записей справочника, в которых совпали уникальные атрибуты
     * @param recordPairs записи, у которых совпали уникальные атрибуты
     * @param versionFrom дата начала актуальности новой версии
     * @param versionTo дата конца актуальности новой версии
     * @return список идентификаторов записей, в которых есть пересечение
     */
    List<Long> checkConflictValuesVersions(List<Pair<Long,String>> recordPairs, Date versionFrom, Date versionTo);

    /**
     * Проверяет есть ли ссылки на версию в каких либо точках запроса
     *
     * @param uniqueRecordId уникальный идентификатор версии записи
     * @param versionFrom дата начала актуальности новой версии
     * @return есть ссылки на версию?
     */
    boolean isVersionUsed(Long refBookId, Long uniqueRecordId, Date versionFrom);

    /**
     * Проверяет есть ли ссылки на версию в каких либо точках запроса
     *
     *
     * @param uniqueRecordIds список идентификаторов версий записей
     * @param versionFrom
     *@param versionTo
     * @param isValuesChanged признак того, что были изменены атрибуты  @return результаты проверки. Сообщения об ошибках
     */
    List<String> isVersionUsed(Long refBookId, List<Long> uniqueRecordIds, Date versionFrom, Date versionTo, boolean isValuesChanged);

    Collection<String> isVersionUsedInRefBooks(Long refBookId, List<Long> uniqueRecordIds);

    /**
     * Возвращает данные о версии следующей за указанной
     * @param refBookId идентификатор справочника
     * @param recordId идентификатор записи справочника (без учета версий)
     * @param versionFrom дата начала актуальности версии текущей версии, после которой будет выполняться поиск следующей версии
     * @return данные версии
     */
    RefBookRecordVersion getNextVersion(Long refBookId, Long recordId, Date versionFrom);

    /**
     * Возвращает данные о версии следующей до указанной
     * @param refBookId идентификатор справочника
     * @param recordId идентификатор записи справочника (без учета версий)
     * @param versionFrom дата начала актуальности версии текущей версии, после которой будет выполняться поиск следующей версии
     * @return данные версии
     */
    RefBookRecordVersion getPreviousVersion(Long refBookId, Long recordId, Date versionFrom);

    /**
     * Возвращает идентификатор записи справочника без учета версий
     * @param uniqueRecordId уникальный идентификатор версии записи
     * @return
     */
    Long getRecordId(Long uniqueRecordId);

    /**
     * Возвращает идентификаторы фиктивных версии, являющихся окончанием указанных версии
     * @param uniqueRecordIds идентификаторы версии записи справочника
     * @return идентификаторы фиктивных версии
     */
    List<Long> getRelatedVersions(List<Long> uniqueRecordIds);

    /**
     * Удаляет все версии записи из справочника
     * @param refBookId идентификатор справочника
     * @param uniqueRecordIds список идентификаторов записей, все версии которых будут удалены
     */
    void deleteAllRecordVersions(Long refBookId, List<Long> uniqueRecordIds);

    /**
     * Возвращает список версий элементов справочника за указанный период времени
     * @param refBookId идентификатор справочника
     * @param startDate начальная дата
     * @param endDate конечная дата
     * @return
     */
    List<Date> getVersions(Long refBookId, Date startDate, Date endDate);

    /**
     * Получает идентификатор записи, который имеет наименьшую дату начала актуальности для указанной версии
     * @param refBookId идентификатор справочника
     * @param uniqueRecordId идентификатор версии записи справочника
     * @return
     */
    Long getFirstRecordId(Long refBookId, Long uniqueRecordId);

    /**
     * Возвращает дату начала периода актуальности для указанных версий записей справочника
     * @param uniqueRecordIds уникальные идентификаторы версий записей справочника
     * @return идентификатор версии - дата начала периода актуальности
     */
    Map<Long,Date> getRecordsVersionStart(List<Long> uniqueRecordIds);

    /**
     * Проверяет есть ли дочерние элементы для указанных версий записей
     * @param refBookId код справочника
     * @param uniqueRecordIds уникальные идентификаторы версий записей справочника
     * @return возвращает список дат начала периода актуальности, для версий у которых были найдены дочерние элементы. Либо null, если их нет
     */
    List<Date> hasChildren(Long refBookId, List<Long> uniqueRecordIds);

    /**
     * Возвращает список идентификаторов элементов справочника, являющихся родительскими  по иерархии вверх для указанного элемента
     * Список упорядочен и начинается с главного корневого элемента
     * @param uniqueRecordId идентификатор записи справочника
     * @return иерархия родительских элементов
     */
    List<Long> getParentsHierarchy(Long uniqueRecordId);

    /**
     * Проверяет существуют ли конфликты в датах актуальности у проверяемых записей и их родительских записей (в иерархических справочниках)
     * @param versionFrom дата начала актуальности
     * @param versionTo дата окончания актуальности
     * @param records проверяемые записи
     */
    List<Pair<Long, Integer>> checkParentConflict(Date versionFrom, Date versionTo, List<RefBookRecord> records);

    /**
     * Устанавливает SCRIPT_ID для справочника
     * @param refBookId идентификатор справочника
     * @param scriptId идентификатор скрипта
     */
    void setScriptId(Long refBookId, String scriptId);

    /**
     * Создает новые записи в справочнике
     * @param refBookId код справочника
     * @param version дата актуальности новых записей
     * @param records список новых записей
     */
    @Deprecated
    void createRecords(Long refBookId, Date version, List<Map<String, RefBookValue>> records);
    
    /**
     * Обновляет значения в справочнике
     * @param refBookId код справочника
     * @param version задает дату актуальности
     * @param records список обновленных записей
     *
     * Вместо этого метода, надо использовать {@link com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao#updateRecordVersion}
     */
    @Deprecated
    void updateRecords(Long refBookId, Date version, List<Map<String, RefBookValue>> records);

    /**
     * Удаляет записи из справочника
     * @param refBookId код справочника
     * @param version задает дату удаления данных
     * @param recordIds список кодов удаляемых записей. {@link com.aplana.sbrf.taxaccounting.model.refbook.RefBook#RECORD_ID_ALIAS Код записи}
     *
     * Вместо этого метода, надо использовать {@link com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao#deleteRecordVersions}
     */
    @Deprecated    
    void deleteRecords(Long refBookId, Date version, List<Long> recordIds);

    /**
     * Удаление всех записей справочника.<br>
     * Записи ближайшей меньшей версии будут отмечены как удаленные на дату удаления
     *
     * @param refBookId Id справочника
     * @param version Дата удаления записей
     *
     * Вместо этого метода, надо использовать {@link com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao#deleteAllRecordVersions}
     */
    @Deprecated
    void deleteAllRecords(Long refBookId, Date version);
}

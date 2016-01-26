package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

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
	RefBook get(@NotNull Long id);

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
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если справочник не найден
     * @return
     */
    RefBook getByAttribute(@NotNull Long attributeId);

    /**
     * Ищет справочник по его записи
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если справочник не найден
     * @return
     */
    RefBook getByRecord(@NotNull Long uniqueRecordId);

	/**
	 * Загружает данные справочника на определенную дату актуальности
	 * @param refBookId код справочника
	 * @param version дата актуальности
	 * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
	 * @param filter условие фильтрации строк. Может быть не задано
	 * @param sortAttribute сортируемый столбец. Может быть не задан
	 * @return
	 */
	PagingResult<Map<String, RefBookValue>> getRecords(@NotNull Long refBookId, @NotNull Date version,
                                                       PagingParams pagingParams, String filter,
                                                       RefBookAttribute sortAttribute);

    /**
     * Получение row_num записи по заданным параметрам
     * @param refBookId код справочника
     * @param version дата актуальности
     * @param recordId
     * @param filter условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return
     */
    Long getRowNum(@NotNull Long refBookId, Date version, Long recordId,
                   String filter, RefBookAttribute sortAttribute, boolean isSortAscending);

    /**
	 * Перегруженный вариант метода, для сохранения обратной совместимости
	 * @param refBookId код справочника
	 * @param version дата актуальности
	 * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
	 * @param filter условие фильтрации строк. Может быть не задано
	 * @param sortAttribute сортируемый столбец. Может быть не задан
	 * @return
	 */
	PagingResult<Map<String, RefBookValue>> getRecords(@NotNull Long refBookId, Date version, PagingParams pagingParams,
		String filter, RefBookAttribute sortAttribute, boolean isSortAscending);

    /**
     * Проверяет, существуют ли версии элемента справочника, удовлетворяющие указанному фильтру
     * @param version дата актуальности. Может быть null - тогда не учитывается
     * @param needAccurateVersion признак того, что нужно точное совпадение по дате начала действия записи
     * @param filter фильтр для отбора записей
     * @return пары идентификатор версии элемента - идентификатор группы версий справочника
     */
    List<Pair<Long, Long>> getRecordIdPairs(Long refBookId, Date version, Boolean needAccurateVersion, String filter);

    /**
     * Возвращает дату начала версии следующей за указанной
     * @param version дата актуальности
     * @param filter фильтр для отбора записей. Обязательное поле, т.к записи не фильтруются по RECORD_ID
     * @return дата начала следующей версии
     */
    Date getNextVersion(Long refBookId, Date version, @NotNull String filter);

    /**
     * Получает количество уникальных записей, удовлетворяющих условиям фильтра
     * @param version дата версии
     * @param filter условие фильтрации строк. Может быть не задано
     * @return количество
     */
    int getRecordsCount(Long refBookId, Date version, String filter);

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
	PagingResult<Map<String, RefBookValue>> getChildrenRecords(@NotNull Long refBookId, Long parentRecordId,
                                                               @NotNull Date version, PagingParams pagingParams,
                                                               String filter, RefBookAttribute sortAttribute);

	/**
	 * По коду возвращает строку справочника
	 * @param refBookId код справочника
	 * @param recordId код строки справочника
	 * @return
	 * @throws org.springframework.dao.EmptyResultDataAccessException если строка не найдена
	 */
	Map<String, RefBookValue> getRecordData(@NotNull Long refBookId, @NotNull Long recordId);

    /**
     * Получение структуры Код строки → Строка справочника по списку кодов строк
     *
     * @param refBookId код справочника
     * @param recordIds список кодов строк справочника
     */
    Map<Long, Map<String, RefBookValue>> getRecordData(@NotNull Long refBookId, @NotNull List<Long> recordIds);

    /**
	 * Создает новые версии записи в справочнике.
     * Если задан параметр recordId - то создается новая версия записи справочника
	 * @param refBookId код справочника
	 * @param version дата актуальности новых записей
     * @param status статус записи
	 * @param records список новых записей
     * @return идентификатор записи справочника (без учета версий)
	 */
	List<Long> createRecordVersion(@NotNull Long refBookId, @NotNull Date version, @NotNull VersionedObjectStatus status,
                                   List<RefBookRecord> records);

    /**
     * Создает фиктивную запись, являющуюся датой окончания периода актуальности какой то версии
     * @param refBookId код справочника
     * @param recordId идентификатор записи справочника без учета версий
     * @param version версия записи справочника
     */
    void createFakeRecordVersion(@NotNull Long refBookId, @NotNull Long recordId, @NotNull Date version);

    /**
     * Обновляет значения атрибутов у указанной версии
     * @param refBookId код справочника
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @param records список значений атрибутов
     */
    void updateRecordVersion(@NotNull Long refBookId, @NotNull Long uniqueRecordId, @NotNull Map<String, RefBookValue> records);

    /**
     * Проверяет существование версий записи справочника
     * @param refBookId идентификатор справочника
     * @param recordIds идентификаторы записей справочника без учета версий
     * @param version версия записи справочника
     * @return
     */
    boolean isVersionsExist(@NotNull Long refBookId, @NotNull List<Long> recordIds, @NotNull Date version);

    /**
     * Проверяет действуют ли записи справочника в указанном периоде
     * @param recordIds уникальные идентификаторы записей справочника
     * @param periodFrom начало периода
     * @param periodTo окончание периода
     * @return список id записей при проверке которых были обнаружены ошибки + код ошибки
     */
    Map<Long, CheckResult> getInactiveRecordsInPeriod(@NotNull List<Long> recordIds, @NotNull Date periodFrom, Date periodTo);

    /**
     * Проверка и поиск Id записи по:
     * @param refBookId Id справочника
     * @param version Версия
     * @param rowId Id строки справочника
     * @return Id первой найденной записи
     */
    Long checkRecordUnique(@NotNull Long refBookId, @NotNull Date version, @NotNull Long rowId);

    /**
     * Значение справочника по Id записи и Id атрибута
     * @param recordId
     * @param attributeId
     * @return
     */
    RefBookValue getValue(@NotNull Long recordId, @NotNull Long attributeId);

    /**
     * Возвращает информацию по версии записи справочника
     *
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @return
     */
    RefBookRecordVersion getRecordVersionInfo(@NotNull Long uniqueRecordId);

    /**
     * Возвращает количество существующих версий для элемента справочника
     * @param refBookId идентификатор справочника
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @return
     */
    int getRecordVersionsCount(@NotNull Long refBookId, @NotNull Long uniqueRecordId);

    /**
     * Возвращает количество существующих версий для элемента справочника
     * @param refBookId идентификатор справочника
     * @param recordId идентификатор группы версий справочника
     * @return
     */
    int getRecordVersionsCountByRecordId(Long refBookId, Long recordId);

    /**
     * Возвращает все версии указанной записи справочника
     * @param refBookId идентификатор справочника
     * @param uniqueRecordId уникальный идентификатор записи, все версии которой будут получены
     * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return
     */
    PagingResult<Map<String,RefBookValue>> getRecordVersionsById(@NotNull Long refBookId, @NotNull Long uniqueRecordId,
                                                                 PagingParams pagingParams, String filter,
                                                                 RefBookAttribute sortAttribute);

    /**
     * Возвращает все версии из указанной группы версий записи справочника
     * @param refBookId идентификатор справочника
     * @param recordId идентификатор группы версий записи справочника
     * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return
     */
    PagingResult<Map<String,RefBookValue>> getRecordVersionsByRecordId(Long refBookId, Long recordId,
                                                                       PagingParams pagingParams, String filter,
                                                                       RefBookAttribute sortAttribute);

    /**
     * Возвращает значения уникальных атрибутов справочника
     * @param refBookId идентификатор справочника
     * @param recordId идентификатор записи
     * @return
     */
    Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> getUniqueAttributeValues(@NotNull Long refBookId, @NotNull Long recordId);

    /**
     * По коду справочника возвращает набор его атрибутов
     *
     * @param refBookId код справочника
     * @return набор атрибутов
     */
    List<RefBookAttribute> getAttributes(@NotNull Long refBookId);

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
    List<Pair<Long,String>> getMatchedRecordsByUniqueAttributes(@NotNull Long refBookId, Long uniqueRecordId,
                                                                @NotNull List<RefBookAttribute> attributes,
                                                                @NotNull List<RefBookRecord> records);

    /**
     * Поиск среди всех элементов справочника (без учета версий) значений уникальных атрибутов, которые бы дублировались с новыми
     * Обеспечение соблюдения уникальности атрибутов в пределах справочника
     *
     * @param attributes      атрибуты справочника
     * @param records         новые значения полей элемента справочника
     * @param accountPeriodId идентификатор периода и подразделения БО
     * @return список пар идентификатор записи-имя атрибута, у которых совпали значения уникальных атрибутов
     */
    List<String> getMatchedRecordsByUniqueAttributesIncome102(@NotNull List<RefBookAttribute> attributes,
                                                              @NotNull List<Map<String, RefBookValue>> records, Integer accountPeriodId);

    /**
     * Поиск существующих версий, которые могут пересекаться с новой версией
     * @param refBookId идентификатор справочника
     * @param recordId идентификатор записи справочника (без учета версий)
     * @param versionFrom дата начала актуальности новой версии
     * @param versionTo дата окончания актуальности новой версии
     * @param excludedRecordId идентификатор версии записи справочника, которая исключается из проверки пересечения. Используется только при редактировании
     * @return результат проверки по каждой версии, с которой есть пересечение
     */
    List<CheckCrossVersionsResult> checkCrossVersions(@NotNull Long refBookId, @NotNull Long recordId,
                                                      @NotNull Date versionFrom, @NotNull Date versionTo,
                                                      Long excludedRecordId);

    /**
     * Проверка на пересечение версий у записей справочника, в которых совпали уникальные атрибуты
     * @param recordPairs записи, у которых совпали уникальные атрибуты
     * @param versionFrom дата начала актуальности новой версии
     * @param versionTo дата конца актуальности новой версии
     * @return список идентификаторов записей, в которых есть пересечение
     */
    List<Long> checkConflictValuesVersions(List<Pair<Long,String>> recordPairs, Date versionFrom, Date versionTo);

    /**
     * Проверяет использование записи как родителя для дочерних
     * @param refBookId идентификатор справочника
     * @param recordId уникальный идентификатор записи
     * @param versionFrom дата начала актуальности новой версии
     * @return список пар <дата начала - дата окончания> периода актуальности обнаруженных дочерних записей
     */
    List<Pair<Date, Date>> isVersionUsedLikeParent(@NotNull Long refBookId, @NotNull Long recordId, @NotNull Date versionFrom);

    /**
     * Проверяет есть ли ссылки на запись справочника в настройках подразделений
     * @param uniqueRecordIds список уникальных идентификаторов записей справочника
     * @param versionFrom дата начала периода
     * @param versionTo дата конца периода
     * @param restrictPeriod
     *      false - возвращает ссылки-использования, период которых НЕ пересекается с указанным периодом
     *      true - возвращает ссылки-использования, период которых пересекается с указанным периодом
     *      null - возвращает все ссылки-использования на указанную запись справочника, без учета периода
     * @param excludeUseCheck идентификаторы справочников, которые игнорируются при проверке использования
     */
    List<String> isVersionUsedInDepartmentConfigs(@NotNull Long refBookId, @NotNull List<Long> uniqueRecordIds, Date versionFrom, Date versionTo, Boolean restrictPeriod, List<Long> excludeUseCheck);

    /**
     * Проверка использования записи в налоговых формах
     * @param refBookId идентификатор справочника
     * @param uniqueRecordIds список уникальных идентификаторов записей справочника
     * @param versionFrom дата начала периода
     * @param versionTo дата конца периода
     * @param restrictPeriod
     *      false - возвращает ссылки-использования, период которых НЕ пересекается с указанным периодом
     *      true - возвращает ссылки-использования, период которых пересекается с указанным периодом
     *      null - возвращает все ссылки-использования на указанную запись справочника, без учета периода
     * @return результаты проверки. Сообщения об ошибках
     */
    List<FormLink> isVersionUsedInForms(Long refBookId, List<Long> uniqueRecordIds, Date versionFrom, Date versionTo,
                                                  Boolean restrictPeriod);

    /**
     * Проверка использования записи в справочниках
     * @param refBookId идентификатор справочника
     * @param uniqueRecordIds список уникальных идентификаторов записей справочника
     * @param versionFrom дата начала периода
     * @param versionTo дата конца периода
     * @param restrictPeriod
     *      false - возвращает ссылки-использования, период которых НЕ пересекается с указанным периодом
     *      true - возвращает ссылки-использования, период которых пересекается с указанным периодом
     *      null - возвращает все ссылки-использования на указанную запись справочника, без учета периода
     * @param excludeUseCheck идентификаторы справочников, которые игнорируются при проверке использования
     * @return результаты проверки. Сообщения об ошибках
     */
    List<String> isVersionUsedInRefBooks(Long refBookId, List<Long> uniqueRecordIds, Date versionFrom, Date versionTo,
                                                Boolean restrictPeriod, List<Long> excludeUseCheck);

    /**
     * Проверка использования записи в справочниках
     * @param refBookId идентификатор справочника
     * @param uniqueRecordIds уникальные идентификаторы версий записей справочника
     * @return результаты проверки. Сообщения об ошибках
     */
    List<String> isVersionUsedInRefBooks(Long refBookId, List<Long> uniqueRecordIds);

    /**
     * Возвращает данные о версии следующей за указанной
     * @param refBookId идентификатор справочника
     * @param recordId идентификатор записи справочника (без учета версий)
     * @param versionFrom дата начала актуальности версии текущей версии, после которой будет выполняться поиск следующей версии
     * @return данные версии
     */
    RefBookRecordVersion getNextVersion(@NotNull Long refBookId, @NotNull Long recordId, @NotNull Date versionFrom);

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
    Long getRecordId(@NotNull Long uniqueRecordId);

    /**
     * Возвращает уникальный идентификатор записи, удовлетворяющей указанным условиям
     * @param refBookId идентификатор справочника
     * @param recordId идентификатор записи справочника (без учета версий)
     * @param version дата
     * @return уникальный идентификатор записи, удовлетворяющей указанным условиям
     */
    Long findRecord(Long refBookId, Long recordId, Date version);

    /**
     * Возвращает идентификаторы фиктивных версии, являющихся окончанием указанных версии.
     * Без привязки ко входным параметрам, т.к метод используется просто для удаления по id
     * @param uniqueRecordIds идентификаторы версии записи справочника
     * @return идентификаторы фиктивных версии
     */
    List<Long> getRelatedVersions(@NotNull List<Long> uniqueRecordIds);

    /**
     * Удаляет все версии записи из справочника
     * @param refBookId идентификатор справочника
     * @param uniqueRecordIds список идентификаторов записей, все версии которых будут удалены
     */
    void deleteAllRecordVersions(@NotNull Long refBookId, @NotNull List<Long> uniqueRecordIds);

    /**
     * Возвращает список версий элементов справочника за указанный период времени
     * @param refBookId идентификатор справочника
     * @param startDate начальная дата
     * @param endDate конечная дата
     * @return
     */
    List<Date> getVersions(@NotNull Long refBookId, @NotNull Date startDate, @NotNull Date endDate);

    /**
     * Получает идентификатор записи, который имеет наименьшую дату начала актуальности для указанной версии
     * @param refBookId идентификатор справочника
     * @param uniqueRecordId идентификатор версии записи справочника
     * @return
     */
    Long getFirstRecordId(@NotNull Long refBookId, @NotNull Long uniqueRecordId);

    /**
     * Возвращает дату начала периода актуальности для указанных версий записей справочника
     * @param uniqueRecordIds уникальные идентификаторы версий записей справочника
     * @return идентификатор версии - дата начала периода актуальности
     */
    Map<Long,Date> getRecordsVersionStart(@NotNull @Size(min = 1) List<Long> uniqueRecordIds);

    /**
     * Проверяет есть ли дочерние элементы для указанных версий записей
     * @param refBookId код справочника
     * @param uniqueRecordIds уникальные идентификаторы версий записей справочника
     * @return возвращает список дат начала периода актуальности, для версий у которых были найдены дочерние элементы. Либо null, если их нет
     */
    List<Date> hasChildren(@NotNull Long refBookId, @NotNull List<Long> uniqueRecordIds);

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
     * @param records проверяемые записи
     */
    List<Pair<Long, Integer>> checkParentConflict(Date versionFrom, List<RefBookRecord> records);

    /**
     * Устанавливает SCRIPT_ID для справочника
     * @param refBookId идентификатор справочника
     * @param scriptId идентификатор скрипта
     */
    void setScriptId(Long refBookId, String scriptId);

    /**
     * Возвращает значения атрибутов для указанных записей
     * @param attributePairs список пар идентификатор записи-идентификатор атрибута
     * @return
     *      ключ - пара идентификатор записи-идентификатор атрибута
     *      значение - строковое представление значения атрибута
     */
    Map<RefBookAttributePair, String> getAttributesValues(List<RefBookAttributePair> attributePairs);

    /**
     * Проверяет существует ли циклическая зависимость для указанных записей справочника
     * Если среди дочерних элементов указанной записи существует указанный родительский элемент, то существует цикл
     * @param uniqueRecordId идентификатор записи
     * @param parentRecordId идентификатор родительской записи
     * @return циклическая зависимость существует?
     */
    boolean hasLoops(Long uniqueRecordId, Long parentRecordId);

    /**
     * Создает новые записи в справочнике
     * @param refBookId код справочника
     * @param version дата актуальности новых записей
     * @param records список новых записей
     */
    @Deprecated
    void createRecords(@NotNull Long refBookId, @NotNull Date version, @NotNull List<Map<String, RefBookValue>> records);
    
    /**
     * Обновляет значения в справочнике
     * @param refBookId код справочника
     * @param version задает дату актуальности
     * @param records список обновленных записей
     *
     * Вместо этого метода, надо использовать {@link com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao#updateRecordVersion}
     */
    @Deprecated
    void updateRecords(@NotNull Long refBookId, @NotNull Date version, @NotNull List<Map<String, RefBookValue>> records);

    /**
     * Удаляет записи из справочника
     * @param refBookId код справочника
     * @param version задает дату удаления данных
     * @param recordIds список кодов удаляемых записей. {@link com.aplana.sbrf.taxaccounting.model.refbook.RefBook#RECORD_ID_ALIAS Код записи}
     *
     * Вместо этого метода, надо использовать {@link com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao#deleteRecordVersions}
     */
    @Deprecated    
    void deleteRecords(@NotNull Long refBookId, @NotNull Date version, @NotNull List<Long> recordIds);

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
    void deleteAllRecords(@NotNull Long refBookId, @NotNull Date version);

    PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long refBookId, String tableName, Long parentId, PagingParams pagingParams,
                                                               String filter, RefBookAttribute sortAttribute, boolean isSortAscending);

    PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, String tableName, PagingParams pagingParams,
                                                       String filter, RefBookAttribute sortAttribute, String whereClause);

    List<Map<String, RefBookValue>> getRecordsData(PreparedStatementData ps, RefBook refBook);

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

    PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, String tableName, PagingParams pagingParams,
                                                       String filter, RefBookAttribute sortAttribute, boolean isSortAscending, String whereClause);

    List<Long> getUniqueRecordIds(Long refBookId, String tableName, String filter);

    /**
     * Получает количество уникальных записей, удовлетворяющих условиям фильтра
     * @param refBookId ид справочника
     * @param tableName название таблицы
     * @param filter условие фильтрации строк. Может быть не задано
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

    /**
     * Проверка ссылочных атрибутов. Их дата начала актуальности должна быть больше либо равна дате актуальности новой версии
     *
     * @param versionFrom дата актуальности новой версии
     * @param attributes  атрибуты справочника
     * @param records     новые значения полей элемента справочника
     * @param isConfig    признак того, что проверка выполняется для настроек подразделений
     */
    void isReferenceValuesCorrect(Logger logger, String tableName, @NotNull Date versionFrom,
                                  @NotNull List<RefBookAttribute> attributes, List<RefBookRecord> records, boolean isConfig);

    Map<String, RefBookValue> getRecordData(Long refBookId, String tableName, Long recordId);

    /**
     * Формирует имя для записи справочника, основанное на уникальных атрибутах
     *
     * @param refBook справочник
     * @param values  список значений уникальных атрибутов
     * @return
     */
    String buildUniqueRecordName(RefBook refBook, Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> values);

    /**
     * Удаляет указанные версии записи из справочника
     *
     * @param uniqueRecordIds список идентификаторов версий записей, которые будут удалены
     */
    void deleteRecordVersions(String tableName, @NotNull List<Long> uniqueRecordIds);

	/**
	 * Разыменование набора ссылок для универсальных справочников
	 * @param attributeId идентификатор атрибута-ссылки
	 * @param recordIds перечень ссылок
	 * @return ref_book_record.id - ref_book_value
	 */
	Map<Long, RefBookValue> dereferenceValues(Long attributeId, Collection<Long> recordIds);

	/**
	 * Разыменование набора ссылок для простых справочников: один справочник - одна таблица
	 * @param tableName название таблицы с данными
	 * @param attributeId идентификатор атрибута-ссылки
	 * @param recordIds перечень ссылок
	 * @return ref_book_record.id - ref_book_value
	 */
	Map<Long, RefBookValue> dereferenceValues(String tableName, Long attributeId, Collection<Long> recordIds);

    /**
     * Проверяет, существуют ли указанные версии элемента справочника
     * @param uniqueRecordIds список уникальных идентификаторов версий записей справочника
     * @return список записей, которые не существуют
     */
    List<Long> isRecordsExist(Set<Long> uniqueRecordIds);

    /**
     * Проверяет, существует ли указанный справочник
     * @param refBookId идентификатор справочника
     * @return все записи существуют?
     */
    boolean isRefBookExist(long refBookId);

}

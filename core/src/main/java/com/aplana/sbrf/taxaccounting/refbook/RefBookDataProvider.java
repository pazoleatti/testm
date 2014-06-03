package com.aplana.sbrf.taxaccounting.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Интерфейс провайдеров данных для справочников.
 *
 * Параметр filter функций getChildrenRecords, getRecords это условие фильтрации строк. Может быть не задано.
 * Фильтр строковый параметр - кусок sql запроса который добавляется в часть where, основного запроса для выборки данных из справочника.
 * Пример фильтра "ALIAS1 = ALIAS2 AND (ALIAS3 != 'groovy' or ALIAS3 LIKE 'java')"
 * Фильтр обрабатывается лексическим анализатором и парсером, для исключения sql инъекции, а так же для автоматического преобразования алиасов.
 * Поддерживаемые операторы сравнения: =, !=, like, is null, >, <
 * Строки должны быть включены в ординарные кавычки. (alias LIKE 'string')
 * Синтаксические операторы sql, такие как and, or, like, is null, регистронезависимые (like, LIKE, LiKe будут восприняты правильно)
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 10.07.13 11:53
 */

public interface RefBookDataProvider {

	/**
	 * Загружает данные справочника на определенную дату актуальности
	 * @param version дата актуальности
	 * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
	 * @param filter условие фильтрации строк. Может быть не задано
	 * @param sortAttribute сортируемый столбец. Может быть не задан
	 * @return
	 */
	PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams,
		String filter, RefBookAttribute sortAttribute, boolean isSortAscending);

    /**
     * Перегруженный метод с восходящей сортировкой по умолчанию
     *
     * @param version
     * @param pagingParams
     * @param filter
     * @param sortAttribute
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams,
                                                       String filter, RefBookAttribute sortAttribute);

    /**
     * Получает уникальные идентификаторы записей, удовлетворяющих условиям фильтра
     * @param version дата актуальности
     * @param filter условие фильтрации строк. Может быть не задано
     * @return
     */
    List<Long> getUniqueRecordIds(Date version, String filter);

    /**
     * Проверяет, существуют ли версии элемента справочника, удовлетворяющие указанному фильтру
     * @param version дата актуальности. Может быть null - тогда не учитывается
     * @param filter
     * @return пары идентификатор версии элемента - идентификаторидентификатор элемента справочника
     */
    List<Pair<Long, Long>> checkRecordExistence(Date version, String filter);

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
	PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version,
		PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

    /**
     * Возвращает список идентификаторов элементов справочника, являющихся родительскими  по иерархии вверх для указанного элемента
     * Список упорядочен и начинается с главного корневого элемента
     * @param uniqueRecordId идентификатор записи справочника
     * @return иерархия родительских элементов
     */
    List<Long> getParentsHierarchy(Long uniqueRecordId);

	/**
	 * По коду возвращает строку справочника
	 * @param recordId код строки справочника
	 * @return
	 */
	Map<String, RefBookValue> getRecordData(Long recordId);

    /**
     * Получение структуры Код строки → Строка справочника по списку кодов строк
     * @param recordIds код строки справочника
     */
    Map<Long, Map<String, RefBookValue>> getRecordData(List<Long> recordIds);

    /**
     * Значение справочника по Id записи и Id атрибута
     * @param recordId
     * @param attributeId
     * @return
     */
    RefBookValue getValue(Long recordId, Long attributeId);

    /**
     * Возвращает список версий элементов справочника за указанный период времени
     * @param startDate начальная дата
     * @param endDate конечная дата
     * @return
     */
    List<Date> getVersions(Date startDate, Date endDate);

    /**
     * Возвращает все версии указанной записи справочника
     * @param uniqueRecordId уникальный идентификатор записи, все версии которой будут получены
     * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecordVersions(Long uniqueRecordId, PagingParams pagingParams,
                                                              String filter, RefBookAttribute sortAttribute);

    /**
     * Возвращает информацию о версии записи справочника
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @return
     */
    RefBookRecordVersion getRecordVersionInfo(Long uniqueRecordId);

    /**
     * Возвращает дату начала периода актуальности для указанных версий записей справочника
     * @param uniqueRecordIds уникальные идентификаторы версий записей справочника
     * @return идентификатор версии - дата начала периода актуальности
     */
    Map<Long, Date> getRecordsVersionStart(List<Long> uniqueRecordIds);

    /**
     * Возвращает количество существующих версий для элемента справочника
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @return
     */
    int getRecordVersionsCount(Long uniqueRecordId);

    /**
     * Создает новую версию записи справочника
     * @param versionFrom дата начала актуальности новый версии
     * @param versionTo дата конца актуальности новый версии. Может быть null
     * @param records список новых значений записи справочника
     */
    List<Long> createRecordVersion(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records);

    /**
     * Возвращает значения уникальных атрибутов для конкретной версии записи справочника
     * @param uniqueRecordId идентификатор версии записи
     * @return not null всегда - ожидается возврат результата
     */
    List<Pair<RefBookAttribute, RefBookValue>> getUniqueAttributeValues(Long uniqueRecordId);

    /**
     * Обновляет данные версии записи справочника
     * Если был изменен период актуальности, выполняются дополнительные проверки пересечений
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @param versionFrom дата начала актуальности новый версии. Может быть null - тогда выполняется только обновление значений записи, без проверок
     * @param versionTo дата конца актуальности новый версии. Может быть null - тогда выполняется только обновление значений записи, без проверок
     * @param records список обновленных значений атрибутов записи справочника
     */
    void updateRecordVersion(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records);

    /**
     * Устанавливает дату окончания периода актуальности для указанных версий записей справочника
     * @param versionEnd задает дату окончания периода актуальности
     * @param uniqueRecordIds список уникальных идентификаторов версий записей справочника
     */
    void updateRecordsVersionEnd(Logger logger, Date versionEnd, List<Long> uniqueRecordIds);
    /**
     * Удаляет все версии записи из справочника
     * @param recordIds список идентификаторов записей, все версии которых будут удалены
     */
    void deleteAllRecords(Logger logger, List<Long> recordIds);
    /**
     * Удаляет указанные версии записи из справочника
     * @param uniqueRecordIds список идентификаторов версий записей, которые будут удалены
     * @param force флаг для получнеия информации из модального окна, в случае когда необходимо запросить
     *                   какое-нибудь подтверждение(например, об удалении
     *                   пример {@link com.aplana.sbrf.taxaccounting.refbook.impl.RefBookDepartment})
     */
    void deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds, boolean force);

    /**
     * Получает идентификатор записи, который имеет наименьшую дату начала актуальности для указанной версии
     * @param uniqueRecordId идентификатор версии записи справочника
     * @return
     */
    Long getFirstRecordId(Long uniqueRecordId);

    /**
     * Получает идентификатор записи без учета версии (record_id) по ее уникальному идентификатору
     * @param uniqueRecordId идентификатор версии записи справочника
     * @return
     */
    Long getRecordId(Long uniqueRecordId);

    /**
     * Возвращает данные по списку атрибутов
     * @param attributePairs связки атрибут-запись справочника
     * @return значения для связок
     */
    Map<RefBookAttributePair, String> getAttributesValues(List<RefBookAttributePair> attributePairs);

    /**
     * Создает новые записи в справочнике
     * @param version дата актуальности новых записей
     * @param records список новых записей
     *
     * Вместо этого метода надо использовать {@link RefBookDataProvider#createRecordVersion}
     */
    @Deprecated
    void insertRecords(Date version, List<Map<String, RefBookValue>> records);

    /**
     * Обновляет значения в справочнике
     * @param version задает дату актуальности
     * @param records список обновленных записей
     *
     * Вместо этого метода надо использовать {@link RefBookDataProvider#updateRecordVersion}
     */
    @Deprecated
    void updateRecords(Date version, List<Map<String, RefBookValue>> records);

    /**
     * Удаляет записи из справочника
     * @param version задает дату удаления данных
     * @param recordIds список уникальных идентификаторов версий записей справочника
     *
     * Вместо этого метода надо использовать {@link RefBookDataProvider#deleteRecordVersions}
     */
    @Deprecated
    void deleteRecords(Date version, List<Long> recordIds);

    /**
     * Удаление всех записей справочника
     * @param version Дата удаления записей
     *
     * Вместо этого метода надо использовать {@link RefBookDataProvider#deleteAllRecords}
     */
    @Deprecated
    void deleteAllRecords(Date version);
}

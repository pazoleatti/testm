package com.aplana.sbrf.taxaccounting.refbook;

import com.aplana.sbrf.taxaccounting.model.FormLink;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Интерфейс провайдеров данных для справочников.
 * <p>
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
    String LOCK_MESSAGE = "Справочник «%s» заблокирован, попробуйте выполнить операцию позже!";

    /**
     * Загружает данные справочника на определенную дату актуальности
     *
     * @param version       дата актуальности
     * @param pagingParams  определяет параметры запрашиваемой страницы данных. Могут быть не заданы. Отсчет идет с 1,а не с 0
     * @param filter        условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecords(@NotNull Date version, PagingParams pagingParams,
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
	 * Возвращает версии записей справочника когда-либо бывших актуальными за указанный период времени.
	 * Например, если какая-либо запись справочника имела в указанном интервале времени три версии, то все
	 * три они вернуться, то есть ищутся пересечения версий записей с указанным интервалом времени.
	 * <br>
	 * Изначальное назначение метода - использование в логических проверках консолидированных за период данных.
	 *
	 * @param versionFrom начало интервала времени
	 * @param versionTo окончание интервала времени
	 * @params pagingParams параметры пейджинга
	 * @param filter условие фильтрации
	 * @return
	 */
	PagingResult<Map<String, RefBookValue>> getRecordsVersion(Date versionFrom, Date versionTo, PagingParams pagingParams, String filter);

    /**
     * Возвращает версии элементов справочника, удовлетворяющие указанному фильтру
     *
     * @param version             дата актуальности. Может быть null - тогда не учитывается
     * @param needAccurateVersion признак того, что нужно точное совпадение по дате начала действия записи
     * @param filter              фильтр для отбора записей
     * @return пары идентификатор версии элемента - идентификатор элемента справочника
     */
    List<Pair<Long, Long>> getRecordIdPairs(Long refBookId, Date version, Boolean needAccurateVersion, String filter);

    /**
     * Возвращает дату начала версии следующей за указанной
     *
     * @param version дата актуальности
     * @param filter  фильтр для отбора записей. Обязательное поле, т.к записи не фильтруются по RECORD_ID
     * @return дата начала следующей версии
     */
    Date getNextVersion(Date version, @NotNull String filter);

    /**
     * Возвращает версию следующей за указанной датой
     *
     * @param recordId    идентификатор записи
     * @param versionFrom дата актуальности
     * @return дата начала следующей версии
     */
    Date getEndVersion(Long recordId, Date versionFrom);

    /**
     * Получает уникальные идентификаторы записей, удовлетворяющих условиям фильтра
     *
     * @param version дата актуальности
     * @param filter  условие фильтрации строк. Может быть не задано
     * @return
     */
    List<Long> getUniqueRecordIds(Date version, String filter);

    /**
     * Получает количество уникальных записей, удовлетворяющих условиям фильтра
     *
     * @param version дата актуальности
     * @param filter  условие фильтрации строк. Может быть не задано
     * @return
     */
    int getRecordsCount(Date version, String filter);

    /**
     * Проверяет, существуют ли версии элемента справочника, удовлетворяющие указанному фильтру
     *
     * @param version дата актуальности. Может быть null - тогда не учитывается
     * @param filter
     * @return пары идентификатор версии элемента - идентификатор элемента справочника
     */
    List<Pair<Long, Long>> checkRecordExistence(Date version, String filter);

    /**
     * Проверяет, существуют ли указанные версии элемента справочника
     *
     * @param uniqueRecordIds список уникальных идентификаторов версий записей справочника
     * @return все записи существуют?
     */
    List<Long> isRecordsExist(List<Long> uniqueRecordIds);

    /**
     * Загружает данные иерархического справочника на определенную дату актуальности
     *
     * @param parentRecordId код родительского элемента
     * @param version        дата актуальности
     * @param pagingParams   определяет параметры запрашиваемой страницы данных
     * @param filter         условие фильтрации строк
     * @param sortAttribute  сортируемый столбец. Может быть не задан
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version,
                                                               PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

    /**
     * Получение row_num записи по заданным параметрам
     *
     * @param version         дата актуальности
     * @param recordId        id записи справочника
     * @param filter          условие фильтрации строк
     * @param sortAttribute   условие фильтрации строк
     * @param isSortAscending сортируемый столбец. Может быть не задан
     * @return
     */
    Long getRowNum(Date version, Long recordId,
                   String filter, RefBookAttribute sortAttribute, boolean isSortAscending);

    /**
     * Возвращает список идентификаторов элементов справочника, являющихся родительскими  по иерархии вверх для указанного элемента
     * Список упорядочен и начинается с главного корневого элемента
     *
     * @param uniqueRecordId идентификатор записи справочника
     * @return иерархия родительских элементов
     */
    List<Long> getParentsHierarchy(Long uniqueRecordId);

    /**
     * По коду возвращает строку справочника
     *
     * @param uniqRecordId код строки справочника
     * @return
     */
    Map<String, RefBookValue> getRecordData(@NotNull Long uniqRecordId);

    /**
     * Получение структуры Код строки → Строка справочника по списку кодов строк
     *
     * @param uniqRecordIds коды строк справочника
     */
    Map<Long, Map<String, RefBookValue>> getRecordData(List<Long> uniqRecordIds);

    /**
     * Получение структуры Код строки → Строка справочника по списку кодов строк
     *
     * @param where коды строк справочника в виде запроса
     * @return
     */
    Map<Long, Map<String, RefBookValue>> getRecordDataWhere(String where);

    /**
     * Получение структуры Код строки → Строка справочника по списку кодов строк
     *
     * @param where коды строк справочника в виде запроса
     * @param version версия справочника
     * @return
     */
    Map<Long, Map<String, RefBookValue>> getRecordDataVersionWhere(String where, Date version);

    /**
     * Значение справочника по Id записи и Id атрибута
     *
     * @param recordId
     * @param attributeId
     * @return
     */
    RefBookValue getValue(Long recordId, Long attributeId);

    /**
     * Возвращает список версий элементов справочника за указанный период времени
     *
     * @param startDate начальная дата
     * @param endDate   конечная дата
     * @return
     */
    List<Date> getVersions(Date startDate, Date endDate);

    /**
     * Возвращает все версии указанной записи справочника
     *
     * @param uniqueRecordId уникальный идентификатор записи, все версии которой будут получены
     * @param pagingParams   определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter         условие фильтрации строк. Может быть не задано
     * @param sortAttribute  сортируемый столбец. Может быть не задан
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecordVersionsById(Long uniqueRecordId, PagingParams pagingParams,
                                                                  String filter, RefBookAttribute sortAttribute);

    /**
     * Возвращает все версии из указанной группы версий записи справочника
     *
     * @param recordId      идентификатор группы версий записи справочника
     * @param pagingParams  определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter        условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecordVersionsByRecordId(Long recordId, PagingParams pagingParams,
                                                                        String filter, RefBookAttribute sortAttribute);

    /**
     * Возвращает информацию о версии записи справочника
     *
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @return
     */
    RefBookRecordVersion getRecordVersionInfo(Long uniqueRecordId);

    /**
     * Возвращает дату начала периода актуальности для указанных версий записей справочника
     *
     * @param uniqueRecordIds уникальные идентификаторы версий записей справочника
     * @return идентификатор версии - дата начала периода актуальности
     */
    Map<Long, Date> getRecordsVersionStart(List<Long> uniqueRecordIds);

    /**
     * Возвращает количество существующих версий для элемента справочника
     *
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @return
     */
    int getRecordVersionsCount(Long uniqueRecordId);

    /**
     * Создает новую версию записи справочника
     *
     * @param versionFrom дата начала актуальности новый версии
     * @param versionTo   дата конца актуальности новый версии. Может быть null
     * @param records     список новых значений записи справочника
     */
    List<Long> createRecordVersion(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records);

    /**
     * То же самое что и {@link RefBookDataProvider#createRecordVersion(com.aplana.sbrf.taxaccounting.model.log.Logger, java.util.Date, java.util.Date, java.util.List<com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord>)}
     * Но без блокировок
     */
    List<Long> createRecordVersionWithoutLock(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records);

    /**
     * Возвращает значения уникальных атрибутов для конкретной версии записи справочника
     *
     * @param uniqueRecordId идентификатор версии записи
     * @return not null всегда - ожидается возврат результата
     */
    Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> getUniqueAttributeValues(Long uniqueRecordId);

    /**
     * Обновляет данные версии записи справочника
     * Если был изменен период актуальности, выполняются дополнительные проверки пересечений
     *
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @param versionFrom    дата начала актуальности новый версии. Может быть null - тогда выполняется только обновление значений записи, без проверок
     * @param versionTo      дата конца актуальности новый версии. Может быть null - тогда выполняется только обновление значений записи, без проверок
     * @param records        список обновленных значений атрибутов записи справочника
     */
    void updateRecordVersion(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records);

    /**
     * То же самое что и {@link RefBookDataProvider#updateRecordVersion(com.aplana.sbrf.taxaccounting.model.log.Logger, java.lang.Long, java.util.Date, java.util.Date, java.util.Map<java.lang.String,com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue>)}
     * Но без блокировок
     */
    void updateRecordVersionWithoutLock(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records);

    /**
     * Устанавливает дату окончания периода актуальности для указанных версий записей справочника
     *
     * @param versionEnd      задает дату окончания периода актуальности
     * @param uniqueRecordIds список уникальных идентификаторов версий записей справочника
     */
    void updateRecordsVersionEnd(Logger logger, Date versionEnd, List<Long> uniqueRecordIds);

    /**
     * То же самое что и {@link RefBookDataProvider#updateRecordsVersionEnd(com.aplana.sbrf.taxaccounting.model.log.Logger, java.util.Date, java.util.List<java.lang.Long>)}
     * Но без блокировок
     */
    void updateRecordsVersionEndWithoutLock(Logger logger, Date versionEnd, List<Long> uniqueRecordIds);

    /**
     * Удаляет все версии записи из справочника
     *
     * @param uniqueRecordIds список уникальных идентификаторов записей, все версии которых будут удалены
     */
    void deleteAllRecords(Logger logger, List<Long> uniqueRecordIds);

    /**
     * То же самое что и {@link RefBookDataProvider#deleteAllRecords(com.aplana.sbrf.taxaccounting.model.log.Logger, java.util.List<java.lang.Long>)}
     * Но без блокировок
     */
    void deleteAllRecordsWithoutLock(Logger logger, List<Long> uniqueRecordIds);

    /**
     * Удаляет указанные версии записи из справочника
     *
     * @param uniqueRecordIds список идентификаторов версий записей, которые будут удалены
     */
    void deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds);

    /**
     * То же самое что и {@link RefBookDataProvider#deleteRecordVersions(com.aplana.sbrf.taxaccounting.model.log.Logger, java.util.List<java.lang.Long>)}
     * Но без блокировок
     */
    void deleteRecordVersionsWithoutLock(Logger logger, List<Long> uniqueRecordIds);

    /**
     * Удаляет указанные версии записи из справочника
     *
     * @param uniqueRecordIds список идентификаторов версий записей, которые будут удалены
     * @param force           флаг для получнеия информации из модального окна, в случае когда необходимо запросить
     *                        какое-нибудь подтверждение(например, об удалении
     *                        пример {@link com.aplana.sbrf.taxaccounting.refbook.impl.RefBookDepartment})
     */
    void deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds, boolean force);

    /**
     * Получает идентификатор записи, который имеет наименьшую дату начала актуальности для указанной версии
     *
     * @param uniqueRecordId идентификатор версии записи справочника
     * @return
     */
    Long getFirstRecordId(Long uniqueRecordId);

    /**
     * Получает идентификатор записи без учета версии (record_id) по ее уникальному идентификатору
     *
     * @param uniqueRecordId идентификатор версии записи справочника
     * @return
     */
    Long getRecordId(Long uniqueRecordId);

    /**
     * Возвращает данные по списку атрибутов
     *
     * @param attributePairs связки атрибут-запись справочника
     * @return значения для связок
     */
    @Deprecated
    Map<RefBookAttributePair, String> getAttributesValues(List<RefBookAttributePair> attributePairs);

    /**
     * Проверяет действуют ли записи справочника в указанном периоде
     * http://conf.aplana.com/pages/viewpage.action?pageId=23245326
     *
     * @param recordIds  уникальные идентификаторы записей справочника
     * @param periodFrom начало периода
     * @param periodTo   окончание периода
     * @return идентификаторы записей, которые не действуют в указанном периоде + результат проверки
     */
    List<ReferenceCheckResult> getInactiveRecordsInPeriod(@NotNull List<Long> recordIds, @NotNull Date periodFrom, Date periodTo);

    /**
     * Создает новые записи в справочнике без учета версионирования
     *
     * @param version дата актуальности новых записей
     * @param records список новых записей
     */
    void insertRecords(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records);

    /**
     * То же самое что и {@link RefBookDataProvider#insertRecords(com.aplana.sbrf.taxaccounting.model.TAUserInfo, java.util.Date, java.util.List<java.util.Map<java.lang.String,com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue>>)}
     * Но без блокировок
     */
    void insertRecordsWithoutLock(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records);

    /**
     * Обновляет значения в справочнике без учета версионирования
     *
     * @param version задает дату актуальности
     * @param records список обновленных записей
     */
    void updateRecords(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records);

    /**
     * То же самое что и {@link RefBookDataProvider#updateRecords(com.aplana.sbrf.taxaccounting.model.TAUserInfo, java.util.Date, java.util.List<java.util.Map<java.lang.String,com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue>>)}
     * Но без блокировок
     */
    void updateRecordsWithoutLock(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records);

    /**
     * Разыменование набора ссылок
     *
     * @param attributeId идентификатор атрибута-ссылки для отображения
     * @param recordIds   перечень ссылок
     * @return ref_book_record.id - ref_book_value.value. Может вернуть null
     */
    Map<Long, RefBookValue> dereferenceValues(Long attributeId, Collection<Long> recordIds);

    /**
     * Получить записи с одинаковыми значениями уникальных атрибутов
     *
     * @param attributes      атрибуты
     * @param records         записи
     * @param accountPeriodId идентификатор периода и подразделения БО
     * @return значения уникальных атрибутов
     */
    List<String> getMatchedRecords(List<RefBookAttribute> attributes, List<Map<String, RefBookValue>> records, Integer accountPeriodId);


}

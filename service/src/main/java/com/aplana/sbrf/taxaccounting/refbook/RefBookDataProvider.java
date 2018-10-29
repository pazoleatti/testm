package com.aplana.sbrf.taxaccounting.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecordVersion;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.refbook.ReferenceCheckResult;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Интерфейс провайдеров данных для справочников.
 * <p>
 * Параметр filter функций getRecords это условие фильтрации строк. Может быть не задано.
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
     * Загружает данные справочника (включая информацию о дате начала и окончания действия версии) на определенную дату актуальности
     *
     * @param version       дата актуальности
     * @param pagingParams  определяет параметры запрашиваемой страницы данных. Могут быть не заданы. Отсчет идет с 1,а не с 0
     * @param filter        условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @param direction     направление сортировки (asc, desc)
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecordsWithVersionInfo(@NotNull Date version, PagingParams pagingParams,
                                                                      String filter, RefBookAttribute sortAttribute, String direction);

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
     * @param versionTo   окончание интервала времени
     * @param filter      условие фильтрации
     * @return
     * @params pagingParams параметры пейджинга
     */
    PagingResult<Map<String, RefBookValue>> getRecordsVersion(Date versionFrom, Date versionTo, PagingParams pagingParams, String filter);

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
     * По коду возвращает строку справочника
     *
     * @param uniqueRecordId код строки справочника
     * @return
     */
    Map<String, RefBookValue> getRecordData(@NotNull Long uniqueRecordId);

    /**
     * Получение структуры Код строки → Строка справочника по списку кодов строк
     *
     * @param uniqueRecordIds коды строк справочника
     */
    Map<Long, Map<String, RefBookValue>> getRecordData(List<Long> uniqueRecordIds);

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
     * @param where   коды строк справочника в виде запроса
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
     * Массовое обновление данных версии записей справочника
     * Если был изменен период актуальности, выполняются дополнительные проверки пересечений
     *
     * @param versionFrom дата начала актуальности новый версии. Может быть null - тогда выполняется только обновление значений записи, без проверок
     * @param versionTo   дата конца актуальности новый версии. Может быть null - тогда выполняется только обновление значений записи, без проверок
     * @param records     список обновленных значений атрибутов записи справочника
     */
    void updateRecordVersions(Logger logger, Date versionFrom, Date versionTo, Set<Map<String, RefBookValue>> records);

    /**
     * То же самое что и {@link RefBookDataProvider#updateRecordVersion(com.aplana.sbrf.taxaccounting.model.log.Logger, java.lang.Long, java.util.Date, java.util.Date, java.util.Map<java.lang.String,com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue>)}
     * Но без блокировок
     */
    void updateRecordVersionWithoutLock(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records);

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
     * Разыменование набора ссылок
     *
     * @param attributeId идентификатор атрибута-ссылки для отображения
     * @param recordIds   перечень ссылок
     * @return ref_book_record.id - ref_book_value.value. Может вернуть null
     */
    Map<Long, RefBookValue> dereferenceValues(Long attributeId, Collection<Long> recordIds);
}
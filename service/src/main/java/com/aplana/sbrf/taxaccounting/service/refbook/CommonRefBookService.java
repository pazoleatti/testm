package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Общий сервис для работы со справочниками. Содержимт методы обработки небольших справочников, не имеющих собственной крупной логики из которых требуется только получать данные
 */
@ScriptExposed
public interface CommonRefBookService {

    /**
     * Загружает метаданные справочника
     *
     * @param refBookId идентификатор справочника
     * @return метаданные справочника
     */
    RefBook get(Long refBookId);

    /**
     * Получает описание действия над справочников в текстовом виде. Используется для формирования текста об ошибках
     *
     * @param descriptionTemplate шаблон описания
     * @param refBookId           идентификатор справочника
     */
    String getRefBookActionDescription(DescriptionTemplate descriptionTemplate, Long refBookId);

    /**
     * Формирует ключ блокировки для справочника
     *
     * @param refBookId идентификатор справочника
     * @return строка-ключ для блокировки
     */
    String generateTaskKey(long refBookId);

    /**
     * Возвращает описание блокировки для справочника
     *
     * @param lockData  данные блокировки
     * @param refBookId идентификатор справочника
     * @return информация о блокировке с деталями
     */
    String getRefBookLockDescription(LockData lockData, long refBookId);

    /**
     * Возвращает данные атрибута справочника по его алиасу
     *
     * @param refBookId      идентификатор справочника
     * @param attributeAlias алиас аттрибута
     * @return данные аттрибута
     */
    RefBookAttribute getAttributeByAlias(long refBookId, String attributeAlias);

    /**
     * Возвращает метаданные справочника по идентификатору атрибута
     *
     * @param attributeId код атрибута, входящего в справочник
     * @return метаданные справочника
     */
    RefBook getByAttribute(Long attributeId);

    /**
     * Возвращает все видимые справочники
     *
     * @return список видимых справочников
     */
    List<RefBook> findAllVisible();

    /**
     * Поиск видимых справочников по названию.
     *
     * @param name         часть названия справочника
     * @param pagingParams параметры сортировки и пагинации
     * @return искомые справочники
     */
    List<RefBookShortInfo> findAllShortInfo(String name, PagingParams pagingParams);

    /**
     * Метод возвращает строку для фильтрации справочника по
     * определенному строковому ключу
     *
     * @param tablePrefix префикс для основной таблицы
     * @param query       ключ для поиска
     * @param refBookId   справочник
     * @param exactSearch точное соответствие
     * @return строку фильтрации для поиска по справочнику
     */
    String getSearchQueryStatement(String tablePrefix, String query, Long refBookId, boolean exactSearch);

    /**
     * Перегруженный метод com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService#getSearchQueryStatement
     * с дефолтным префиксом tablePrefix = "frb"
     *
     * @return строку фильтрации для поиска по справочнику
     */
    String getSearchQueryStatement(String query, Long refBookId, boolean exactSearch);

    /**
     * Метод возвращает строку для фильтрации справочника по
     * определенному строковому ключу и дополнительным строковым параметрам присоединенным с условием "И"
     *
     * @param tablePrefix префикс для основной таблицы
     * @param parameters  дополнительные строковые параметры, ключ - имя поля в таблице, значение поля в таблице
     * @param refBookId   справочник
     * @param exactSearch точное соответствие
     * @return строку фильтрации для поиска по справочнику
     */
    String getSearchQueryStatementWithAdditionalStringParameters(String tablePrefix, Map<String, String> parameters, String searchPattern, Long refBookId, boolean exactSearch);

    /**
     * Перегруженный метод com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService#getSearchQueryStatementWithAdditionalStringParameters
     * с дефолтным префиксом tablePrefix = "frb"
     *
     * @return строку фильтрации для поиска по справочнику
     */
    String getSearchQueryStatementWithAdditionalStringParameters(Map<String, String> parameters, String searchPattern, Long refBookId, boolean exactSearch);

    /**
     * Получение всех значений указанного справочника
     *
     * @param refBookId     идентификатор справочника
     * @param columns       список столбцов таблицы справочника, по которым будет выполняться фильтрация
     * @param searchPattern шаблон поиска по полям справочника
     * @param filter        параметр фильтрации
     * @param pagingParams  параметры пейджинга
     * @return значения справочника
     */
    <T extends RefBookSimple> PagingResult<T> fetchAllRecords(long refBookId, List<String> columns, String searchPattern, String filter, PagingParams pagingParams);

    /**
     * Получение одного значения указанного справочника
     *
     * @param refBookId идентификатор справочника
     * @param recordId  идентификатор записи
     * @return значение записи справочника
     */
    <T extends RefBookSimple> T fetchRecord(Long refBookId, Long recordId);

    /**
     * Сохраняет изменения в записи справочника
     *
     * @param userInfo  текущий пользователь
     * @param refBookId идентификатор справочника
     * @param recordId  идентификатор записи справочника
     * @param record    данные записи в структуре аттрибут-значение
     * @return результат сохранения
     */
    ActionResult editRecord(TAUserInfo userInfo, long refBookId, long recordId, Map<String, RefBookValue> record);

    /**
     * Создает новую запись справочника
     *
     * @param refBookId идентификатор справочника
     * @param record    данные записи в структуре аттрибут-значение
     * @return результат сохранения
     */
    ActionResult createRecord(TAUserInfo userInfo, Long refBookId, Map<String, RefBookValue> record);

    /**
     * Удаляет все версии указанных записей справочника
     *
     * @param refBookId идентификатор справочника
     * @param recordIds идентификаторы записей для удаления
     * @return результат удаления
     */
    ActionResult deleteRecords(TAUserInfo userInfo, Long refBookId, List<Long> recordIds);

    /**
     * Удаляет указанные записи (версии) правочника
     *
     * @param refBookId идентификатор справочника
     * @param recordIds идентификаторы записей для удаления
     * @return результат удаления
     */
    ActionResult deleteVersions(TAUserInfo userInfo, Long refBookId, List<Long> recordIds);

    /**
     * Формирование отчета по записям справочника в формате XLSX/CSV
     *
     * @param refBookId     идентификатор справочника
     * @param version       версия, на которую строится отчет (для версионируемых справочников)
     * @param pagingParams  параметры сортировки для отображения записей в отчете так же как и в GUI
     * @param searchPattern Строка с запросом поиска по справочнику
     * @param exactSearch   Признак того, что результат поиска должен быть с полным соответствием поисковой строке
     * @param extraParams   дополнительные параметры для фильтрации записей
     * @param reportType    тип отчета
     * @return информация о создании отчета
     */
    ActionResult createReport(TAUserInfo userInfo, long refBookId, Date version, PagingParams pagingParams,
                              String searchPattern, boolean exactSearch, Map<String, String> extraParams, AsyncTaskType reportType);

    /**
     * Получение всех значений указанного справочника с возможностью самостоятельно сформировать SQL-условие по входным параметрам фильтрации и поиска
     *
     * @param refBookId     идентификатор справочника
     * @param recordId      идентификатор группы версий записей справочника. Если указано - отбираются только версии этой группы, иначе игнорируется
     * @param version       версия, на которую будут отобраны записи
     * @param searchPattern строка для полнотекстового поиска
     * @param exactSearch   флаг для полнотекстового поиска с точным совпадением
     * @param extraParams   дополнительные параметры для фильтрации записей
     * @param pagingParams  параметры пейджинга
     * @param sortAttribute атрибут, по которому будут отсортированы записи
     * @param direction     направление сортировки - asc, desc
     * @return значения справочника
     */
    PagingResult<Map<String, RefBookValue>> fetchAllRecords(Long refBookId, Long recordId, Date version,
                                                            String searchPattern, boolean exactSearch, Map<String, String> extraParams,
                                                            PagingParams pagingParams, RefBookAttribute sortAttribute, String direction);

    /**
     * Получает количество записей справочника, удовлетворяющим условиям поиска
     *
     * @param refBookId     идентификатор справочника
     * @param version       дата актуальности
     * @param searchPattern строка для полнотекстового поиска
     * @param exactSearch   флаг для полнотекстового поиска с точным совпадением
     * @param extraParams   дополнительные параметры для фильтрации записей
     * @return количество записей
     */
    int getRecordsCount(Long refBookId, Date version, String searchPattern, boolean exactSearch, Map<String, String> extraParams);

    /**
     * Разыменовывает справочные атрибуты записей, т.е те, которые ссылаются на другие записи справочников. Получает полный объект вместо id-ссылки
     *
     * @param refBook справочник, для которого выполняется разыменование
     * @param records список записей, для которых выполняется разыменование
     */
    PagingResult<Map<String, RefBookValue>> dereference(RefBook refBook, PagingResult<Map<String, RefBookValue>> records);

    /**
     * Создать идентификатор версии справочника
     * @return созданный идентификатор
     */
    Long createNextRefBookRecordId();
}
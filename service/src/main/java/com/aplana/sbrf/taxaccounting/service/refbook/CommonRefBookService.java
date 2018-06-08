package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookSimple;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Общий сервис для работы со справочниками. Содержимт методы обработки небольших справочников, не имеющих собственной крупной логики из которых требуется только получать данные
 */
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
     * Проверяет целесообразность запуска скрипта для указанного справочника. Если скрипт пустой или не содержит
     * обработчика указанного события, то он выполняться не будет.
     *
     * @param refBookId идентификатор справочника
     * @param event     событие, которе обрабатывается в скрипте
     * @return можно выполнить скрипт?
     */
    boolean getEventScriptStatus(long refBookId, FormDataEvent event);

    /**
     * Проверяет целесообразность запуска скрипта для всех событий указанного справочника. Если скрипт пустой или не содержит
     * обработчика указанного события, то он выполняться не будет.
     *
     * @param refBookId идентификатор справочника
     * @return список пар событие - можно выполнить скрипт по событию?
     */
    Map<FormDataEvent, Boolean> getEventScriptStatus(long refBookId);

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
     * Получает список справочников
     *
     * @param visible признак того, что нужно получить только видимые справочники
     *                true - только видимые
     *                false - только невидимые
     *                null - все
     * @return список справочников
     */
    List<RefBook> fetchAll(Boolean visible);

    /**
     * Возвращает все видимые справочники
     * @return все видимые справочники
     */
    List<RefBook> fetchAll();

    /**
     * Метод возвращает строку для фильтрации справочника по
     * определенному строковому ключу
     *
     * @param query       - ключ для поиска
     * @param refBookId   - справочник
     * @param exactSearch - точное соответствие
     * @return строку фильтрации для поиска по справочнику
     */
    String getSearchQueryStatement(String query, Long refBookId, boolean exactSearch);

    /**
     * Метод возвращает строку для фильтрации справочника с неточным соответствием по определенному строковому ключу
     *
     * @param query     - ключ для поиска
     * @param refBookId - справочник
     * @return строку фильтрации для поиска по справочнику
     */
    String getSearchQueryStatement(String query, Long refBookId);

    /**
     * Метод возвращает строку для фильтрации справочника по
     * определенному строковому ключу и дополнительным строковым параметрам присоединенным с условием "И"
     *
     * @param parameters  - дополнительные строковые параметры, ключ - имя поля в таблице, значение поля в таблице
     * @param refBookId   - справочник
     * @param exactSearch - точное соответствие
     * @return строку фильтрации для поиска по справочнику
     */
    String getSearchQueryStatementWithAdditionalStringParameters(Map<String, String> parameters, String searchPattern, Long refBookId, boolean exactSearch);

    /**
     * Получение всех значений указанного справочника
     *
     * @param refBookId    идентификатор справочника
     * @param columns      список столбцов таблицы справочника, по которым будет выполняться фильтрация
     * @param filter       параметр фильтрации
     * @param pagingParams параметры пейджинга
     * @return значения справочника
     */
    <T extends RefBookSimple> PagingResult<T> fetchAllRecords(long refBookId, List<String> columns, String filter, PagingParams pagingParams);

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
     * @param reportType    тип отчета
     * @return информация о создании отчета
     */
    ActionResult createReport(TAUserInfo userInfo, long refBookId, Date version, PagingParams pagingParams,
                              String searchPattern, boolean exactSearch, AsyncTaskType reportType);

    /**
     * Получает данные иерархического справочника. В случае применения фильтра для элементов дополнительно подгружаются
     * родительские элементы вплоть до корневого для корректного отображения в дереве
     *
     * @param refBookId     идентификатор справочника
     * @param searchPattern Строка с запросом поиска по справочнику
     * @param exactSearch   Признак того, что результат поиска должен быть с полным соответствием поисковой строке
     * @return
     */
    Collection<Map<String, RefBookValue>> fetchHierRecords(Long refBookId, String searchPattern, boolean exactSearch);

    /**
     * Получение всех значений указанного справочника с возможностью самостоятельно сформировать SQL-условие по входным параметрам фильтрации и поиска
     *
     * @param refBookId     идентификатор справочника
     * @param recordId      идентификатор группы версий записей справочника. Если указано - отбираются только версии этой группы, иначе игнорируется
     * @param version       версия, на которую будут отобраны записи
     * @param searchPattern строка для полнотекстового поиска
     * @param exactSearch   флаг для полнотекстового поиска с точным совпадением
     * @param pagingParams  параметры пейджинга
     * @return значения справочника
     */
    PagingResult<Map<String, RefBookValue>> fetchAllRecords(Long refBookId, Long recordId, Date version,
                                                            String searchPattern, boolean exactSearch, PagingParams pagingParams);

    /**
     * Разыменовывает справочные атрибуты записей, т.е те, которые ссылаются на другие записи справочников. Получает полный объект вместо id-ссылки
     *
     * @param refBook справочник, для которого выполняется разыменование
     * @param records список записей, для которых выполняется разыменование
     */
    PagingResult<Map<String, RefBookValue>> dereference(RefBook refBook, PagingResult<Map<String, RefBookValue>> records);
}

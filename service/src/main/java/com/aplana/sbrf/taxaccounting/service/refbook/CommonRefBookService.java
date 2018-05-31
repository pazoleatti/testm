package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookSimple;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.RefBookListResult;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Общий сервис для работы со справочниками. Содержимт методы обработки небольших справочников, не имеющих собственной крупной логики из которых требуется только получать данные
 */
public interface CommonRefBookService {

    /**
     * Получить все справочники
     *
     * @return список объектов содержащих данные о справочниках
     */
    PagingResult<RefBookListResult> fetchAllRefBooks();

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
     * Удаляет указанные записи правочника
     *
     * @param refBookId идентификатор справочника
     * @param recordIds идентификаторы записей для удаления
     * @return результат удаления
     */
    ActionResult deleteRecords(TAUserInfo userInfo, Long refBookId, List<Long> recordIds);

    /**
     * Получение количества версий для записи справочника
     *
     * @param refBookId идентификатор справочника
     * @param recordId  идентификатор записи
     * @return значение записи справочника
     */
    int getRecordVersionCount(Long refBookId, Long recordId);

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
}
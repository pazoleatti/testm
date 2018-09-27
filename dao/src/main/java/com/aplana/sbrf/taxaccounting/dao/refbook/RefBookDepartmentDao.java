package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User: ekuvshinov
 */
public interface RefBookDepartmentDao {

	Long REF_BOOK_ID = Department.REF_BOOK_ID;

    /**
     * Загружает данные справочника
     * в данном случае даты актуальности нет смотри SBRFACCTAX-3245
     *
     * @param pagingParams
     * @param sortAttribute может быть не задан (null)
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecords(PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending);

    /**
     * Перегруженный метод с восходящей сортировкой по умолчанию
     *
     * @param pagingParams
     * @param filter
     * @param sortAttribute
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecords(PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

    /**
     * По коду возвращает строку справочника
     *
     *
     * @param recordId код строки справочника
     * @return
     */
    Map<String, RefBookValue> getRecordData(Long recordId);

    /**
     * Получение структуры Код строки → Строка справочника по списку кодов строк
     *
     * @param uniqRecordIds коды строк справочника
     */
    Map<Long, Map<String, RefBookValue>> getRecordData(List<Long> uniqRecordIds);

    /**
     * Получение row_num записи по заданным параметрам
     * @param recordId
     * @param filter
     * @param sortAttribute
     * @param isSortAscending
     * @return
     */
    Long getRowNum(Long recordId, String filter, RefBookAttribute sortAttribute, boolean isSortAscending);

    /**
     * Поиск среди всех элементов справочника (без учета версий) значений уникальных атрибутов, которые бы дублировались с новыми,
     * отдельных справочников.
     * Обеспечение соблюдения уникальности атрибутов в пределах справочника
     * @param recordId идентификатор справочника
     * @param attributes атрибуты справочника
     * @param records новые значения полей элемента справочника
     * @return список пар идентификатор записи-имя атрибута, у которых совпали значения уникальных атрибутов
     */
    List<Pair<String,String>> getMatchedRecordsByUniqueAttributes(Long recordId, List<RefBookAttribute> attributes, List<RefBookRecord> records);

    void update(int uniqueId, Map<String, RefBookValue> records, List<RefBookAttribute> attributes);

    int create(Map<String, RefBookValue> records, List<RefBookAttribute> attributes);

    void remove(long uniqueId);

    /**
     * Получает количетсво уникальных записей, удовлетворяющих условиям фильтра
     * @param filter условие фильтрации строк. Может быть не задано
     * @return
     */
    int getRecordsCount(String filter);

    /**
     * Возвращает наменование периода ориентируясь только на дату начала(только на день-месяц, без года)
     * Должно быть только одно значение, даже не учитывая вид налога{@link com.aplana.sbrf.taxaccounting.model.TaxType}
     * Справвочник Параметры подразделения по УКС
     * @param startDate Начало периода
     */
    String getReportPeriodNameByDate(TaxType taxType, Date startDate);

    List<Long> isRecordsExist(List<Long> uniqueRecordIds);

    /**
     * Проверяет действуют ли записи справочника
     * @param recordIds уникальные идентификаторы записей справочника
     * @return список id записей при проверке которых были обнаружены ошибки + код ошибки
     */
    List<ReferenceCheckResult> getInactiveRecords(@NotNull List<Long> recordIds);
}
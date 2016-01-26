package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User: ekuvshinov
 */
public interface RefBookDepartmentDao {

	Long REF_BOOK_ID = Department.REF_BOOK_ID;

    String UNIQUE_ATTRIBUTES_ALIAS = "uniqueAttributes";
    String STRING_VALUE_COLUMN_ALIAS = "string_value";
    String NUMBER_VALUE_COLUMN_ALIAS = "number_value";
    String DATE_VALUE_COLUMN_ALIAS = "date_value";
    String REFERENCE_VALUE_COLUMN_ALIAS = "reference_value";
    String REFBOOK_NAME_ALIAS = "refbookName";
    String REFBOOK_ID_ALIAS = "ref_book_id";
    String VERSION_START_ALIAS = "versionStart";
    String VERSION_END_ALIAS = "versionEnd";

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
     * Возвращает значения атрибутов для указанных записей
     * @param attributePairs список пар идентификатор записи-идентификатор атрибута
     * @return
     *      ключ - пара идентификатор записи-идентификатор атрибута
     *      значение - строковое представление значения атрибута
     */
    Map<RefBookAttributePair,String> getAttributesValues(List<RefBookAttributePair> attributePairs);

    /**
     * Проверяет действуют ли записи справочника в указанном периоде
     * @param recordIds уникальные идентификаторы записей справочника
     * @param periodFrom начало периода
     * @param periodTo окончание периода
     * @return список id записей при проверке которых были обнаружены ошибки + код ошибки
     */
    Map<Long, CheckResult> getInactiveRecordsInPeriod(@NotNull List<Long> recordIds, @NotNull Date periodFrom, Date periodTo);

    /**
     * Проверка использования записи в справочниках
     * @param refBookId идентификатор справочника
     * @param uniqueRecordIds уникальные идентификаторы версий записей справочника
     * @return справочник где 1-й ключ - id из ref_book_record(для уникальности ключа)
     */
    Map<Integer, Map<String, Object>> isVersionUsedInRefBooks(List<Long> refBookId, List<Long> uniqueRecordIds);

    /**
     * Возвращает наменование периода ориентируясь только на дату начала(только на день-месяц, без года)
     * Должно быть только одно значение, даже не учитывая вид налога{@link com.aplana.sbrf.taxaccounting.model.TaxType}
     * Справвочник Параметры подразделения по УКС
     * @param startDate Начало периода
     */
    String getReportPeriodNameByDate(TaxType taxType, Date startDate);

    List<Long> isRecordsExist(List<Long> uniqueRecordIds);
}
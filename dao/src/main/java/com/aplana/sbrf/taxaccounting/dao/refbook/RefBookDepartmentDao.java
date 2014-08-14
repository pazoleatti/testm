package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributePair;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User: ekuvshinov
 */
public interface RefBookDepartmentDao {

	static final Long REF_BOOK_ID = 30L;

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

    /**
     * Получает отчетные периоды по виду налога и департаментам
     * @param taxTypes
     * @param departmentList
     * @return
     */
    public List<Long> getPeriodsByTaxTypesAndDepartments(List<TaxType> taxTypes, List<Integer> departmentList);

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
     * @return все записи действуют в указанном периоде?
     */
    List<Long> isRecordsActiveInPeriod(@NotNull List<Long> recordIds, @NotNull Date periodFrom, Date periodTo);
}

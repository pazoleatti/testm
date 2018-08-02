package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Сервис для работы со справочником физ. лиц, для специфики по дубликатам.
 */
public interface PersonService {

    /**
     * Переводит запись в статус дубликата
     *
     * @param recordIds  - идентификаторы записей
     * @param originalId - идентификатор ФЛ оригинала
     */
    void setDuplicate(List<Long> recordIds, Long originalId);

    /**
     * Меняем родителя (RECORD_ID) у дубликатов
     *
     * @param recordIds
     * @param originalId
     */
    void changeRecordId(List<Long> recordIds, Long originalId);

    /**
     * Переводит запись в статус оригинала
     *
     * @param recordIds - идентификаторы записей
     */
    void setOriginal(List<Long> recordIds);

    /**
     * Получение оригинала ФЛ
     *
     * @param personId Идентификатор ФЛ (RECORD_ID)
     * @return оригинал ФЛ
     */
    RefBookPerson getOriginal(Long personId);

    /**
     * Возвращает серию + номер ДУЛ ФЛ
     *
     * @param personId идентификатор ФЛ
     * @return серия + номер ДУЛ
     */
    String getPersonDocNumber(long personId);

    /**
     * Получение списка дубликатов ФЛ по идентификатору ФЛ
     *
     * @param personId     Идентификатор ФЛ (RECORD_ID)
     * @param pagingParams Параметры пейджинга
     * @return Страница списка дубликатов ФЛ
     */
    PagingResult<RefBookPerson> getDuplicates(Long personId, PagingParams pagingParams);

    /**
     * Получает список ФЛ с возможностью самостоятельно сформировать sql-условие по входящим параметрам фильтрации
     *
     * @param recordId      идентификатор группы версий (в терминах системы, это Идентификатор ФЛ). Используется для отбора версий конкретного ФЛ
     * @param version       версия для отбора записей
     * @param pagingParams  параметры пэйджинга
     * @param firstName     имя ФЛ
     * @param lastName      фамилия ФЛ
     * @param searchPattern условие для полнотекстового поиска
     * @param exactSearch   условие для полнотекстового поиска с точным совпадением
     * @return список ФЛ
     */
    PagingResult<RefBookPerson> getPersons(Long recordId, Date version, PagingParams pagingParams, String firstName, String lastName, String searchPattern, boolean exactSearch);

    /**
     * Получает список ФЛ учитывая условия фильтрации и сортировки. Копия метода из провайдера справочника, переделанная для лучшей производительности
     * Все или отдельные параметры могут быть null, тогда они не учитываются при отборе записей
     *
     * @param version       версия для отбора записей
     * @param pagingParams  параметры пэйджинга
     * @param filter        условие отбора записей. Фактически кусок sql-запроса для where части
     * @param sortAttribute атрибут, по которому будут отсортированы записи
     * @return список ФЛ
     */
    PagingResult<RefBookPerson> getPersons(@Nullable Date version, @Nullable PagingParams pagingParams, @Nullable String filter, @Nullable RefBookAttribute sortAttribute);

    /**
     * Получает список версий конкретного ФЛ
     *
     * @param recordId     идентификатор группы записей (в терминах системы, это Идентификатор ФЛ)
     * @param pagingParams параметры пэйджинга
     * @return список версий ФЛ
     */
    PagingResult<RefBookPerson> getPersonVersions(Long recordId, PagingParams pagingParams);

    /**
     * Получает список ФЛ учитывая условия фильтрации и сортировки. Метод делает то же самое что и {@link com.aplana.sbrf.taxaccounting.service.PersonService#getPersons(java.util.Date, com.aplana.sbrf.taxaccounting.model.PagingParams, java.lang.String, com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute)}
     * но возвращает объекты в виде мапы.
     * @param version       версия для отбора записей
     * @param pagingParams  параметры пэйджинга
     * @param filter        условие отбора записей. Фактически кусок sql-запроса для where части
     * @param sortAttribute атрибут, по которому будут отсортированы записи
     * @return  список ФЛ
     */
    PagingResult<Map<String, RefBookValue>> fetchPersonsAsMap(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

    /**
     * Сохраняет изменения списке дубликатов и оригинале ФЛ
     *
     * @param currentPerson     запись в справочнике ФЛ, для которой назначаются оригинал/дубли
     * @param original          оригинал ФЛ
     * @param newDuplicates     новые дубликаты
     * @param deletedDuplicates дубликаты, которые были удалены
     * @return результат выполнения операции
     */
    ActionResult saveOriginalAndDuplicates(TAUserInfo userInfo, RefBookPerson currentPerson, RefBookPerson original, List<RefBookPerson> newDuplicates, List<RefBookPerson> deletedDuplicates);

    /**
     * Создает фильтр поиска
     * @param firstName     значение имени
     * @param lastName      значение фамилии
     * @param searchPattern строка для по всем полям
     * @param exactSearch   искать по точному совпадению
     * @return              часть sql запроса в виде строки
     */
    String createSearchFilter(String firstName, String lastName, String searchPattern, Boolean exactSearch);
}

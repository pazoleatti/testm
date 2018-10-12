package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.action.PersonOriginalAndDuplicatesAction;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.CheckDulResult;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Сервис для работы со справочником физ. лиц, для специфики по дубликатам.
 */
public interface PersonService {

    /**
     * Получение оригинала ФЛ
     *
     * @param id идентификатор версии ФЛ (ID)
     * @return оригинал ФЛ
     */
    RegistryPersonDTO fetchOriginal(Long id, Date actualDate);

    /**
     * Получение списка дубликатов ФЛ по идентификатору версии ФЛ
     *
     * @param personId     идентификатор версии ФЛ (ID)
     * @param pagingParams параметры пейджинга
     * @return Страница списка дубликатов ФЛ
     */
    PagingResult<RegistryPersonDTO> fetchDuplicates(Long personId, Date actualDate, PagingParams pagingParams);

    /**
     * Получает список ФЛ.
     *
     * @param pagingParams параметры постраничной выдачи и сортировки
     * @return список ФЛ
     */
    PagingResult<RefBookPerson> getPersons(PagingParams pagingParams, RefBookPersonFilter filter, TAUser requestingUser);

    /**
     * Возвращяет кол-во ФЛ по фильтру
     */
    int getPersonsCount(RefBookPersonFilter filter);

    /**
     * Получает список ФЛ учитывая условия фильтрации и сортировки. Метод возвращает объекты в виде мапы.
     *
     * @param version       версия для отбора записей
     * @param pagingParams  параметры пэйджинга
     * @param filter        условие отбора записей. Фактически кусок sql-запроса для where части
     * @param sortAttribute атрибут, по которому будут отсортированы записи
     * @return список ФЛ
     */
    PagingResult<Map<String, RefBookValue>> fetchPersonsAsMap(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

    /**
     * Сохраняет изменения списке дубликатов и оригинале ФЛ
     * @param userInfo  информация о пользователей
     * @param data      данные об изменении оригинала и дубликатов
     * @return результат действия
     */
    ActionResult saveOriginalAndDuplicates(TAUserInfo userInfo, PersonOriginalAndDuplicatesAction data);

    /**
     * Создает фильтр поиска
     *
     * @param firstName     значение имени
     * @param lastName      значение фамилии
     * @param searchPattern строка для по всем полям
     * @param exactSearch   искать по точному совпадению
     * @return часть sql запроса в виде строки
     */
    String createSearchFilter(String firstName, String lastName, String searchPattern, Boolean exactSearch);

    /**
     * Получает версию физлица из реестра ФЛ и инициализирует ее ссылочными справочными значениями
     *
     * @param id идентификатор версии Физлица
     * @return объект версии ФЛ
     */
    RegistryPersonDTO fetchPerson(Long id);

    /**
     * Получить список значений справочника ссылающихся на физлицо, для всех версий физлица, в т.ч. и дубликатов
     *
     * @param recordId  идентификатор Физлица
     * @param refBookId идентификатор справочника
     * @return список значений справочника
     */
    PagingResult<Map<String, RefBookValue>> fetchReferencesList(Long recordId, Long refBookId, PagingParams pagingParams);

    /**
     * Создаёт ассинхронную задачу на формирование Excel ФЛ по фильтру
     *
     * @param filter       фильтр по ФЛ
     * @param pagingParams параметры сортировки
     * @param userInfo     пользователь запустивший операцию
     * @return результат создания задачи
     */
    ActionResult createTaskToCreateExcel(RefBookPersonFilter filter, PagingParams pagingParams, TAUserInfo userInfo);

    /**
     * Обновить запись в реестре ФЛ
     *
     * @param person данные ФЛ для обновления
     */
    void updateRegistryPerson(RegistryPersonDTO person);

    /**
     * Проверка пересечений версий
     * @param person проверяемое физическое лицо
     */
    void checkVersionOverlapping(RegistryPersonDTO person);

    /**
     * Проверяет корректность ДУЛ
     * @param docCode   код документа
     * @param docNumber номер документа
     */
    CheckDulResult checkDul(String docCode, String docNumber);

    /**
     * Получение записей реестра ФЛ для назначения Оригиналом/Дубликатом
     * @param filter        фильтр выборки
     * @param pagingParams  параметры постраничной выдачи
     * @return  Страница списка записей
     */
    PagingResult<RefBookPerson> fetchOriginalDuplicatesCandidates(PagingParams pagingParams, RefBookPersonFilter filter, TAUser requestingUser);

    /**
     * Сохранить группу Физлиц.
     * @param personList коллекция Физлиц
     */
    void savePersons(List<RegistryPerson> personList);
}

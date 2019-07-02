package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.IdDoc;
import com.aplana.sbrf.taxaccounting.model.refbook.PersonFor2NdflFL;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPersonDTO;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.CheckDulResult;

import java.util.List;

/**
 * Сервис для работы со справочником физ. лиц, для специфики по дубликатам.
 */
public interface PersonService {

    /**
     * Получает список ФЛ.
     *
     * @param pagingParams параметры постраничной выдачи и сортировки
     * @return список ФЛ
     */
    PagingResult<RegistryPersonDTO> getPersonsData(PagingParams pagingParams, RefBookPersonFilter filter);

    /**
     * Возвращяет списк ФЛ для формирования 2-НДФЛ (ФЛ)
     *
     * @param pagingParams параметры постраничной выдачи и сортировки
     * @param filter       фильтр
     * @return список ФЛ
     */
    PagingResult<PersonFor2NdflFL> findAllFor2NdflFL(PagingParams pagingParams, RefBookPersonFilter filter);

    /**
     * Возвращяет кол-во ФЛ по фильтру
     */
    int getPersonsCount(RefBookPersonFilter filter);

    /**
     * Получает версию физлица из реестра ФЛ и инициализирует ее ссылочными справочными значениями
     *
     * @param id идентификатор версии Физлица
     * @return объект версии ФЛ
     */
    RegistryPersonDTO fetchPersonData(Long id);

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
     * @param person   данные ФЛ для обновления
     * @param userInfo пользователь, запустивший операцию
     */
    void updateRegistryPerson(RegistryPersonDTO person, TAUserInfo userInfo);

    /**
     * Обновить несколько записей объектов идентификации - физлиц
     *
     * @param personList список ФЛ
     */
    void updateIdentificatedPersons(List<NaturalPerson> personList);

    /**
     * Проверяет корректность ДУЛ
     *
     * @param docCode   код документа
     * @param docNumber номер документа
     */
    CheckDulResult checkDul(String docCode, String docNumber);

    /**
     * Получение записей реестра ФЛ для назначения Оригиналом/Дубликатом
     *
     * @param filter       фильтр выборки
     * @param pagingParams параметры постраничной выдачи
     * @return Страница списка записей
     */
    PagingResult<RegistryPersonDTO> fetchOriginalDuplicatesCandidates(PagingParams pagingParams, RefBookPersonFilter filter, TAUser requestingUser);

    /**
     * Сохранить группу Физлиц.
     *
     * @param personList коллекция Физлиц
     * @return список ФЛ с добавленными идентификаторами ФЛ
     */
    List<RegistryPerson> savePersons(List<RegistryPerson> personList);

    /**
     * Найти актуальные на текущую дату записи реестра ФЛ связанные с определенной налоговой формой
     *
     * @param declarationDataId идентификатор налоговой формы
     * @return список найденных записей реестра ФЛ
     */
    List<RegistryPerson> findActualRefPersonsByDeclarationDataId(Long declarationDataId);

    /**
     * Определить ДУЛ включаемый в отчетность.
     *
     * @param person физическое лицо
     * @return ДУЛ включаемый в отчетность
     */
    IdDoc selectIncludeReportDocument(RegistryPersonDTO person);
}

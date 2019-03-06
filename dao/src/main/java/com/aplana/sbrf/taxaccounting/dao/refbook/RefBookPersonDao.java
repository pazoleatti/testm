package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.dao.PermissionDao;
import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.person.NaturalPersonMapper;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import org.springframework.jdbc.core.RowMapper;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Интерфейс DAO для работы со справочником физлиц
 */
public interface RefBookPersonDao extends PermissionDao {
    /**
     * Очищает в NDFL_PERSON столбец PERSON_ID по declarationDataId
     */
    void clearRnuNdflPerson(Long declarationDataId);

    void fillRecordVersions();

    /**
     * Найти всех ФЛ по определяющим параметрам
     *
     * @param declarationDataId идентификатор НФ
     */
    Map<Long, Map<Long, NaturalPerson>> findPersonForUpdateFromPrimaryRnuNdfl(Long declarationDataId, NaturalPersonRefbookHandler naturalPersonHandler);

    /**
     * Найти всех ФЛ по полному списку параметров
     */
    Map<Long, Map<Long, NaturalPerson>> findPersonForCheckFromPrimaryRnuNdfl(Long declarationDataId, NaturalPersonRefbookHandler naturalPersonHandler);

    /**
     * Найти данные о ФЛ в ПНФ
     */
    List<NaturalPerson> findNaturalPersonPrimaryDataFromNdfl(long declarationDataId, RowMapper<NaturalPerson> naturalPersonRowMapper);

    /**
     * Установить дубликаты
     *
     * @param addedDuplicateRecordIds идентификаторы ФЛ которые необходимо установить дубликатами
     * @param changingPersonRecordId  идентификатор изменяемого ФЛ
     */
    void setDuplicates(List<Long> addedDuplicateRecordIds, Long changingPersonRecordId);

    /**
     * Удалить дубликаты
     *
     * @param deletedDuplicateOldIds идентификаторы ФЛ удаляемые из дубликатов
     */
    void deleteDuplicates(List<Long> deletedDuplicateOldIds);

    /**
     * Установить оригинал
     *
     * @param originalRecordId идентификатор оригинала
     * @param personOldId      исходный идентификатор изменяемого ФЛ
     */
    void setOriginal(Long originalRecordId, Long personOldId);


    /**
     * Возвращяет список версий ФЛ-оригинала у заданного ФЛ
     *
     * @param id идентификатро версии ФЛ
     * @return Оригинал ФЛ
     */
    List<RegistryPerson> findAllOriginalVersions(Long id);

    /**
     * Возвращяет список версий ФЛ-дубликатов у заданного ФЛ
     *
     * @param id идентификатор версии ФЛ
     * @return список дубликатов ФЛ
     */
    List<RegistryPerson> findAllDuplicatesVersions(Long id);

    /**
     * Получает список идентификаторов ФЛ, являющихся дуликатами указанных ФЛ
     *
     * @param originalRecordIds список оригиналов ФЛ
     * @return список дубликатов для каждого из ФЛ
     */
    List<Long> getDuplicateIds(Set<Long> originalRecordIds);

    /**
     * Получить количество уникальных записей в справочнике физлиц, которые не являются дублями для налоговой формы.
     *
     * @param declarationDataId идентификатор налоговой формы
     * @return количество записей
     */
    int getCountOfUniqueEntries(long declarationDataId);

    /**
     * Возвращает список ID тербанков ФЛ
     */
    List<Integer> getPersonTbIds(long personId);

    /**
     * Получает список ФЛ с пагинацией и фильтрацией.
     *
     * @param pagingParams параметры постраничной выдачи и сортировки
     * @param filter       параметры фильтрации результатов
     * @return страница списка ФЛ, подходящих под фильтр
     */
    PagingResult<RegistryPerson> getPersons(PagingParams pagingParams, RefBookPersonFilter filter);

    /**
     * Возвращяет кол-во ФЛ по фильтру
     */
    int getPersonsCount(RefBookPersonFilter filter);

    /**
     * Получает версию физлица c информацией о дате начала и конца версии
     *
     * @param id идентификатор версии
     * @return объект версии ФЛ
     */
    RegistryPerson fetchPersonVersion(Long id);

    /**
     * Обновить данные записи реестра ФЛ
     *
     * @param person данные ФЛ
     */
    void updateRegistryPerson(RegistryPerson person);

    /**
     * Получить все версии физлица, которые не являются дубликатами
     *
     * @param recordId идентификатор ФЛ
     * @return список объектов найденных версий
     */
    List<RegistryPerson> fetchNonDuplicatesVersions(long recordId);

    /**
     * Получение записей реестра ФЛ для назначения Оригиналом/Дубликатом
     *
     * @param filter       фильтр выборки
     * @param pagingParams параметры постраничной выдачи
     * @return Страница списка записей
     */
    PagingResult<RegistryPerson> fetchOriginalDuplicatesCandidates(PagingParams pagingParams, RefBookPersonFilter filter);

    /**
     * Сохранить группу Физлиц.
     *
     * @param persons коллекция Физлиц
     */
    void saveBatch(Collection<RegistryPerson> persons);

    /**
     * Обновить группу Физлиц. При групповом обновлении не обновляются поля id, record_id, old_id, start_date, end_date.
     * В версии 3.2 нет необходимости обновлять вышеуказанные поля массово и поскольку их обновление без проверок
     * потенциально ведёт к дефектам их массовое обновление недоступно.
     *
     * @param persons коллекция Физлиц
     */
    void updateBatch(Collection<RegistryPerson> persons);

    /**
     * Найти актуальные на текущую дату записи реестра ФЛ связанные с определенной налоговой формой
     *
     * @param declarationDataId идентификатор налоговой формы
     * @param actualDate        дата актуальности
     * @return список найденных записей реестра ФЛ
     */
    List<RegistryPerson> findActualRefPersonsByDeclarationDataId(Long declarationDataId, Date actualDate);

    /**
     * Найти идентификатор версии ФЛ в реестре ФЛ с максимальным идентификатором для даты актуальности. Дубликаты также исключаются из результата.
     * @param currentDate   дата актуальности для определения версии ФЛ
     * @return идентификатор записи в Реестре ФЛ
     */
    Long findMaxRegistryPersonId(Date currentDate);

    /**
     * Найти список физлиц в реестре ФЛ для даты актуальности, идентификатор которых больше указанного в параметре. Дубликаты также исключаются из результата.
     * @param oldMaxId              предыдущий идентификатор версии ФЛ
     * @param currentDate           дата актуальности для определения версии ФЛ
     * @param naturalPersonMapper   проинициализированный справониками маппер
     * @return                      список найденных ФЛ
     */
    List<NaturalPerson> findNewRegistryPersons(Long oldMaxId, Date currentDate, NaturalPersonMapper naturalPersonMapper);
}

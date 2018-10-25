package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import org.springframework.jdbc.core.RowMapper;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Интерфейс DAO для работы со справочником физлиц
 */
public interface RefBookPersonDao {
    /**
     * Очищает в NDFL_PERSON столбец PERSON_ID по declarationDataId
     */
    void clearRnuNdflPerson(Long declarationDataId);

    void fillRecordVersions();

    /**
     * Найти всех ФЛ по определяющим параметрам
     * @param declarationDataId идентификатор НФ
     */
    Map<Long, Map<Long, NaturalPerson>> findPersonForUpdateFromPrimaryRnuNdfl(Long declarationDataId, NaturalPersonRefbookHandler naturalPersonHandler);

    /**
     * Найти всех ФЛ по полному списку параметров
     *
     */
    Map<Long, Map<Long, NaturalPerson>> findPersonForCheckFromPrimaryRnuNdfl(Long declarationDataId, NaturalPersonRefbookHandler naturalPersonHandler);

    /**
     * Найти данные о ФЛ в ПНФ
     */
    List<NaturalPerson> findNaturalPersonPrimaryDataFromNdfl(long declarationDataId, RowMapper<NaturalPerson> naturalPersonRowMapper);

    /**
     * Установить дубликаты
     * @param addedDuplicateRecordIds   идентификаторы ФЛ которые необходимо установить дубликатами
     * @param changingPersonRecordId    идентификатор изменяемого ФЛ
     */
    void setDuplicates(List<Long> addedDuplicateRecordIds, Long changingPersonRecordId);

    /**
     * Удалить дубликаты
     * @param deletedDuplicateOldIds идентификаторы ФЛ удаляемые из дубликатов
     */
    void deleteDuplicates(List<Long> deletedDuplicateOldIds);

    /**
     * Установить оригинал
     * @param changingPersonRecordId    идентификатор изменяемого ФЛ
     * @param changingPersonOldId       исходный идентификатор изменяемого ФЛ
     * @param addedOriginalRecordId     идентификатор добавляемого оригинала
     */
    void setOriginal(Long changingPersonRecordId, Long changingPersonOldId, @NotNull Long addedOriginalRecordId);

    /**
     * Удалить оригинал
     * @param changingPersonRecordId    идентификатор изменяемого ФЛ
     * @param changingPersonOldId       исходный идентификатор изменяемого ФЛ
     */
    void deleteOriginal(Long changingPersonRecordId, Long changingPersonOldId);

    /**
     * Получение оригинала ФЛ
     *
     * @param id идентификатро версии ФЛ
     * @return Оригинал ФЛ
     */
    List<RegistryPerson> fetchOriginal(Long id);

    /**
     * Получение дубликатов ФЛ
     *
     * @param id           идентификатор версии ФЛ
     * @return список дубликатов ФЛ
     */
    List<RegistryPerson> fetchDuplicates(Long id);

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

    PagingResult<Map<String, RefBookValue>> fetchPersonsAsMap(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

    /**
     * Получить объект справочника Физические лица
     *
     * @return объект справочника
     */
    RefBook getRefBook();

    /**
     * Получает версию физлица c информацией о дате начала и конца версии
     *
     * @param id идентификатор версии
     * @return объект версии ФЛ
     */
    RegistryPerson fetchPersonVersion(Long id);

    /**
     * Обновить данные записи реестра ФЛ
     * @param person    данные ФЛ
     */
    void updateRegistryPerson(RegistryPerson person);

    /**
     * Обновить флаг "включается в отчетность"
     * @param oldReportDocId    старое значение флага
     * @param newReportDocId    новое значение флага
     */
    void updateRegistryPersonIncRepDocId(Long oldReportDocId, Long newReportDocId);

    /**
     * Удалить фиктивную версию ФЛ
     * @param recordId идентификатор ФЛ
     */
    void deleteRegistryPersonFakeVersion(long recordId);

    /**
     * Сохранить фиктивную версию Физлица
     * @param person объект ФЛ
     */
    void saveRegistryPersonFakeVersion(RegistryPersonDTO person);

    /**
     * Получить все версии физлица, которые не являются дубликатами
     * @param recordId идентификатор ФЛ
     * @return список объектов найденных версий
     */
    List<RegistryPerson> fetchNonDuplicatesVersions(long recordId);

    /**
     * Получение записей реестра ФЛ для назначения Оригиналом/Дубликатом
     * @param filter        фильтр выборки
     * @param pagingParams  параметры постраничной выдачи
     * @return  Страница списка записей
     */
    PagingResult<RegistryPerson> fetchOriginalDuplicatesCandidates(PagingParams pagingParams, RefBookPersonFilter filter);

    /**
     * Сохранить группу Физлиц.
     * @param persons коллекция Физлиц
     */
    void saveBatch(Collection<RegistryPerson> persons);

    /**
     * Обновить группу Физлиц. При групповом обновлении не обновляются поля id, record_id, old_id, start_date, end_date.
     * В версии 3.2 нет необходимости обновлять вышеуказанные поля массово и поскольку их обновление без проверок
     * потенциально ведёт к дефектам их массовое обновление недоступно.
     * @param persons коллекция Физлиц
     */
    void updateBatch(Collection<RegistryPerson> persons);
}

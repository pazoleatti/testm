package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import org.springframework.jdbc.core.RowMapper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Интерфейс DAO для работы со справочником физлиц
 *
 * @author Andrey Drunk
 */
public interface RefBookPersonDao {
    /**
     * Очищает в NDFL_PERSON столбец PERSON_ID по declarationDataId
     *
     * @param declarationDataId
     */
    void clearRnuNdflPerson(Long declarationDataId);

    /**
     * @param version
     */
    void fillRecordVersions(Date version);

    /**
     * Найти всех ФЛ по определяющим параметрам
     *
     * @param declarationDataId идентификатор НФ
     * @param asnuId            идентификатор АСНУ загрузившей данные
     * @param version           версия записи
     * @return
     */
    Map<Long, Map<Long, NaturalPerson>> findPersonForUpdateFromPrimaryRnuNdfl(Long declarationDataId, Long asnuId, Date version, NaturalPersonRefbookHandler naturalPersonHandler);

    /**
     * Найти всех ФЛ по полному списку параметров
     *
     * @param declarationDataId
     * @param asnuId            идентификатор АСНУ загрузившей данные
     * @param version           версия записи
     * @return
     */
    Map<Long, Map<Long, NaturalPerson>> findPersonForCheckFromPrimaryRnuNdfl(Long declarationDataId, Long asnuId, Date version, NaturalPersonRefbookHandler naturalPersonHandler);

    /**
     * Найти данные о ФЛ в ПНФ
     *
     * @param declarationDataId
     * @param naturalPersonRowMapper
     * @return
     */
    List<NaturalPerson> findNaturalPersonPrimaryDataFromNdfl(long declarationDataId, RowMapper<NaturalPerson> naturalPersonRowMapper);

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
     * @param id идентификатро версии ФЛ
     * @return Оригинал ФЛ
     */
    List<RegistryPerson> fetchOriginal(Long id);

    /**
     * Получение дубликатов ФЛ
     * @param id            идентификатор версии ФЛ
     * @param pagingParams  параметры пейджинга
     * @return  список дубликатов ФЛ
     */
    List<RegistryPerson> fetchDuplicates(Long id, PagingParams pagingParams);

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
     * Возвращает серию + номер ДУЛ ФЛ
     *
     * @param personId идентификатор ФЛ
     * @return серия + номер ДУЛ
     */
    String getPersonDocNumber(long personId);

    /**
     * Получение списка дубликатов ФЛ по идентификатору ФЛ
     *
     * @param personId     Идентификатор ФЛ (RECORD_ID). Если
     * @param pagingParams Параметры пейджинга
     * @return Страница списка дубликатов ФЛ
     */
    PagingResult<RefBookPerson> getDuplicates(Long personId, PagingParams pagingParams);

    /**
     * Получает список ФЛ всех версий.
     *
     * @param pagingParams параметры постраничной выдачи и сортировки
     * @return список ФЛ
     */
    PagingResult<RefBookPerson> getPersons(PagingParams pagingParams);

    PagingResult<Map<String, RefBookValue>> fetchPersonsAsMap(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

    /**
     * Получить объект справочника Физические лица
     *
     * @return объект справочника
     */
    RefBook getRefBook();

    /**
     * Получает версию физлица c информацией о дате начала и конца версии
     * @param id    идентификатор версии
     * @return  объект версии ФЛ
     */
    RegistryPerson fetchPersonWithVersionInfo(Long id);
}

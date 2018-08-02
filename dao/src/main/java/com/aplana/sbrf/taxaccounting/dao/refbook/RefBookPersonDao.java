package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.springframework.jdbc.core.RowMapper;

import javax.annotation.Nullable;
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
     *
     * @param personId Идентификатор ФЛ (RECORD_ID)
     * @return оригинал ФЛ
     */
    RefBookPerson getOriginal(Long personId);

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
     * Получает список ФЛ актуальных на указанную дату с учитыванием пэйджинга, фильтрации и сортировки
     * Все или отдельные параметры могут быть null, тогда они не учитываются при отборе записей
     *
     * @param version       версия, на которую будут отобраны записи
     * @param pagingParams  параметры пэйджинга
     * @param filter        фильтр для отбора записей. Фактически кусок SQL-запроса для WHERE части
     * @param sortAttribute атрибут, по которому записи будут отсортированы
     * @return список ФЛ
     */
    PagingResult<RefBookPerson> getPersons(@Nullable Date version, @Nullable PagingParams pagingParams, @Nullable String filter, @Nullable RefBookAttribute sortAttribute);

    PagingResult<Map<String, RefBookValue>> fetchPersonsAsMap(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

    /**
     * Получить объект справочника Физические лица
     * @return объект справочника
     */
    RefBook getRefBook();

    /**
     * Получает список версий ФЛ
     *
     * @param recordId     идентификатор группы версий ФЛ (фактически идентификатор ФЛ)
     * @param pagingParams параметры пэйджинга
     * @return список версий ФЛ
     */
    PagingResult<RefBookPerson> getPersonVersions(long recordId, PagingParams pagingParams);
}

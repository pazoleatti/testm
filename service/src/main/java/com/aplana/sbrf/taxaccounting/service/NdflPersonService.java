package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.KppSelect;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.NdflFilter;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonDeductionDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonIncomeDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonPrepaymentDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с {@link NdflPerson ФЛ }
 */
public interface NdflPersonService {

    /**
     * Найти данные НДФЛ ФЛ по ид
     */
    NdflPerson findOne(long id);

    /**
     * Найти все данные НДФЛ ФЛ привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param parameters        значения фильтра
     * @return список NdflPerson заполненый данными из таблицы NDFL_PERSON
     */
    PagingResult<NdflPerson> findPersonByFilter(long declarationDataId, Map<String, Object> parameters, PagingParams pagingParams);

    /**
     * Найти все данные НДФЛ ФЛ привязанные к декларации
     *
     * @param ndflFilter значения фильтра
     * @return список NdflPerson заполненый данными из таблицы NDFL_PERSON
     */
    PagingResult<NdflPerson> findPersonByFilter(NdflFilter ndflFilter, PagingParams pagingParams);

    /**
     * Возвращает данные только 1ого раздела формы РНУ
     *
     * @param declarationDataId идентификатор формы РНУ
     */
    List<NdflPerson> findAllByDeclarationId(long declarationDataId);

    /**
     * Найти все "Сведения о доходах физического лица" привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     */
    List<NdflPersonIncome> findNdflPersonIncome(long declarationDataId);

    /**
     * Найти все данные о доходах и НДФЛ привязанные к декларации
     *
     * @param ndflFilter параметры фильтра
     * @return список NdflPersonIncomeFilter заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_INCOME
     */
    PagingResult<NdflPersonIncomeDTO> findPersonIncomeByFilter(NdflFilter ndflFilter, PagingParams pagingParams);

    /**
     * Найти все данные о вычетах привязанные к декларации
     *
     * @param ndflFilter параметры фильтра
     * @return список NdflPersonDeduction заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_DEDUCTION
     */
    PagingResult<NdflPersonDeductionDTO> findPersonDeductionsByFilter(NdflFilter ndflFilter, PagingParams pagingParams);

    /**
     * Найти все данные о доходах в виде авансовых платежей привязанные к декларации
     *
     * @param ndflFilter параметры фильтра
     * @return список NdflPersonPrepayment заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_PREPAYMENT
     */
    PagingResult<NdflPersonPrepaymentDTO> findPersonPrepaymentByFilter(NdflFilter ndflFilter, PagingParams pagingParams);

    /**
     * Получает название ДУЛ для ФЛ по его коду
     *
     * @param idDocType Документ удостоверяющий личность.Код (Графа 10)
     * @return название ДУЛ
     */
    String getPersonDocTypeName(long idDocType);

    /**
     * Получить число ФЛ в NDFL_PERSON
     *
     * @param declarationDataId идентификатор налоговой формы
     * @return число ФЛ
     */
    int getNdflPersonCount(Long declarationDataId);

    /**
     * Возвращяет страницу из значений КПП, полученных из строк раздела 2 формы РНУ
     *
     * @param declarationDataId ид формы РНУ
     * @param kpp               фильтр поиска
     * @param pagingParams      данные пагинатора
     * @return страница из значений КПП, полученных из строк раздела 2 формы РНУ
     */
    PagingResult<KppSelect> findAllKppByDeclarationDataId(long declarationDataId, String kpp, PagingParams pagingParams);

    /**
     * Заполнить поля раздела 2 используемые для сортировки
     * @param ndflPersonList  список записей раздела 1 которым нужно заполнить поля раздела 2
     */
    void fillNdflPersonIncomeSortFields(List<NdflPerson> ndflPersonList);

    /**
     * Получить информацию о вычетах ФЛ (раздел 3), которые связаны со сведениями о доходах и НДФЛ (раздел 2)
     *
     * @param personId идентификатор ФЛ
     * @param incomesIds список идентификаторов строк для сведений о доходах и НДФЛ
     * @return список идентификаторов вычетов ФЛ
     */
    List<Long> getDeductionsIdsByPersonAndIncomes(long personId, Collection<Long> incomesIds);

    /**
     * Получить информацию о доходах в виде авансовых платежей у ФЛ (раздел 4),
     * которые связаны со сведениями о доходах и НДФЛ (раздел 2)
     *
     * @param personId идентификатор ФЛ
     * @param incomesIds список идентификаторов строк для сведений о доходах и НДФЛ
     * @return список идентификаторов сведений о доходах в виде аваносовых платежей у ФЛ
     */
    List<Long> getPrepaymentsIdsByPersonAndIncomes(long personId, Collection<Long> incomesIds);

}

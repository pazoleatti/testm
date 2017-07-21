package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment;
import com.mysema.query.types.Predicate;

import java.util.Map;

/**
 * Сервис для работы с {@link NdflPerson ФЛ }
 */
public interface NdflPersonService {

    /**
     * Найти количество записей данных НДФЛ ФЛ привязанных к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return количество записей в списке NdflPerson заполненый данными из таблицы NDFL_PERSON
     */
    int findPersonCount(long declarationDataId);

    /**
     * Найти все данные НДФЛ ФЛ привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param parameters значения фильтра
     * @return список NdflPerson заполненый данными из таблицы NDFL_PERSON
     */
    PagingResult<NdflPerson> findPersonByFilter(long declarationDataId, Map<String, Object> parameters, int startIndex, int pageSize);

    /**
     * Найти количество записей данных о доходах и НДФЛ привязанных к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return количество записей в списке NdflPersonIncomeFilter заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_INCOME
     */
    int findPersonIncomeCount(long declarationDataId);

    /**
     * Найти все данные о доходах и НДФЛ привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param parameters значения фильтра
     * @return список NdflPersonIncomeFilter заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_INCOME
     */
    PagingResult<NdflPersonIncome> findPersonIncomeByFilter(long declarationDataId, Map<String, Object> parameters, int startIndex, int pageSize);


    /**
     * Найти количество записей данных о вычетах привязанных к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return количество записей в списке NdflPersonDeduction заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_DEDUCTION
     */
    int findPersonDeductionsCount(long declarationDataId);

    /**
     * Найти все данные о вычетах привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param parameters значения фильтра
     * @return список NdflPersonDeduction заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_DEDUCTION
     */
    PagingResult<NdflPersonDeduction> findPersonDeductionsByFilter(long declarationDataId, Map<String, Object> parameters, int startIndex, int pageSize);

    /**
     * Найти количество записей данных о доходах в виде авансовых платежей привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return количество записей в списке NdflPersonPrepayment заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_PREPAYMENT
     */
    int findPersonPrepaymentCount(long declarationDataId);

    /**
     * Найти все данные о доходах в виде авансовых платежей привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param parameters значения фильтра
     * @return список NdflPersonPrepayment заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_PREPAYMENT
     */
    PagingResult<NdflPersonPrepayment> findPersonPrepaymentByFilter(long declarationDataId, Map<String, Object> parameters, int startIndex, int pageSize);
}

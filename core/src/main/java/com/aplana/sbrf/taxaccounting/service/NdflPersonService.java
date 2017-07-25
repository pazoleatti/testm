package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonDeductionFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonIncomeFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonPrepaymentFilter;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment;

import java.util.Map;

/**
 * Сервис для работы с {@link NdflPerson ФЛ }
 */
public interface NdflPersonService {

    /**
     * Найти количество записей данных НДФЛ ФЛ привязанных к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return количество записей в списке заполненном данными из таблицы NDFL_PERSON
     */
    int findPersonCount(long declarationDataId);

    /**
     * Найти все данные НДФЛ ФЛ привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param parameters значения фильтра
     * @return список NdflPerson заполненый данными из таблицы NDFL_PERSON
     */
    PagingResult<NdflPerson> findPersonByFilter(long declarationDataId, Map<String, Object> parameters, PagingParams pagingParams);

    /**
     * Найти количество записей данных о доходах и НДФЛ привязанных к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return количество записей в списке заполненном данными из таблиц NDFL_PERSON и NDFL_PERSON_INCOME
     */
    int findPersonIncomeCount(long declarationDataId);

    /**
     * Найти все данные о доходах и НДФЛ привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param ndflPersonIncomeFilter параметры фильтра
     * @return список NdflPersonIncomeFilter заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_INCOME
     */
    PagingResult<NdflPersonIncome> findPersonIncomeByFilter(long declarationDataId, NdflPersonIncomeFilter ndflPersonIncomeFilter, PagingParams pagingParams);


    /**
     * Найти количество записей данных о вычетах привязанных к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return количество записей в списке заполненном данными из таблиц NDFL_PERSON и NDFL_PERSON_DEDUCTION
     */
    int findPersonDeductionsCount(long declarationDataId);

    /**
     * Найти все данные о вычетах привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param ndflPersonDeductionFilter параметры фильтра
     * @return список NdflPersonDeduction заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_DEDUCTION
     */
    PagingResult<NdflPersonDeduction> findPersonDeductionsByFilter(long declarationDataId, NdflPersonDeductionFilter ndflPersonDeductionFilter, PagingParams pagingParams);

    /**
     * Найти количество записей данных о доходах в виде авансовых платежей привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return количество записей в списке заполненном данными из таблиц NDFL_PERSON и NDFL_PERSON_PREPAYMENT
     */
    int findPersonPrepaymentCount(long declarationDataId);

    /**
     * Найти все данные о доходах в виде авансовых платежей привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param ndflPersonPrepaymentFilter параметры фильтра
     * @return список NdflPersonPrepayment заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_PREPAYMENT
     */
    PagingResult<NdflPersonPrepayment> findPersonPrepaymentByFilter(long declarationDataId, NdflPersonPrepaymentFilter ndflPersonPrepaymentFilter, PagingParams pagingParams);
}

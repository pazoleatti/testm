package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.NdflFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonDeductionFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonIncomeFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonPrepaymentFilter;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonDeductionDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonIncomeDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonPrepaymentDTO;

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
}

package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
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
import com.aplana.sbrf.taxaccounting.service.NdflPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Сервис для работы с ФЛ
 */
@Service
public class NdflPersonServiceImpl implements NdflPersonService{

    @Autowired
    private NdflPersonDao ndflPersonDao;

    /**
     * Найти количество записей данных НДФЛ ФЛ привязанных к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return количество записей в списке заполненном данными из таблицы NDFL_PERSON
     */
    @Override
    public int findPersonCount(long declarationDataId) {
        return ndflPersonDao.findPersonCount(declarationDataId);
    }

    /**
     * Найти все данные НДФЛ ФЛ привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param parameters значения фильтра
     * @return список NdflPerson заполненый данными из таблицы NDFL_PERSON
     */
    @Override
    public PagingResult<NdflPerson> findPersonByFilter(long declarationDataId, Map<String, Object> parameters, PagingParams pagingParams) {
        return ndflPersonDao.findNdflPersonByParameters(declarationDataId, parameters, pagingParams);
    }

    /**
     * Найти все данные НДФЛ ФЛ привязанные к декларации
     *
     * @param ndflPersonFilter значения фильтра
     * @return список NdflPerson заполненый данными из таблицы NDFL_PERSON
     */
    @Override
    public PagingResult<NdflPerson> findPersonByFilter(NdflPersonFilter ndflPersonFilter, PagingParams pagingParams) {
        return ndflPersonDao.findNdflPersonByParameters(ndflPersonFilter, pagingParams);
    }

    /**
     * Найти количество записей данных о доходах и НДФЛ привязанных к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return количество записей в списке заполненном данными из таблиц NDFL_PERSON и NDFL_PERSON_INCOME
     */
    @Override
    public int findPersonIncomeCount(long declarationDataId) {
        return ndflPersonDao.findPersonIncomeCount(declarationDataId);
    }

    /**
     * Найти все данные о доходах и НДФЛ привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param ndflPersonIncomeFilter параметры фильтра
     * @return список NdflPersonIncomeFilter заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_INCOME
     */
    @Override
    public PagingResult<NdflPersonIncomeDTO> findPersonIncomeByFilter(long declarationDataId, NdflPersonIncomeFilter ndflPersonIncomeFilter, PagingParams pagingParams) {
        return ndflPersonDao.findPersonIncomeByParameters(declarationDataId, ndflPersonIncomeFilter, pagingParams);
    }

    /**
     * Найти количество записей данных о вычетах привязанных к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return количество записей в списке заполненном данными из таблиц NDFL_PERSON и NDFL_PERSON_DEDUCTION
     */
    @Override
    public int findPersonDeductionsCount(long declarationDataId) {
        return ndflPersonDao.findPersonDeductionsCount(declarationDataId);
    }

    /**
     * Найти все данные о вычетах привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param ndflPersonDeductionFilter параметры фильтра
     * @return список NdflPersonDeduction заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_DEDUCTION
     */
    @Override
    public PagingResult<NdflPersonDeductionDTO> findPersonDeductionsByFilter(long declarationDataId, NdflPersonDeductionFilter ndflPersonDeductionFilter, PagingParams pagingParams) {
        return ndflPersonDao.findPersonDeductionByParameters(declarationDataId, ndflPersonDeductionFilter, pagingParams);
    }

    /**
     * Найти количество записей данных о доходах в виде авансовых платежей привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return количество записей в списке заполненном данными из таблиц NDFL_PERSON и NDFL_PERSON_PREPAYMENT
     */
    @Override
    public int findPersonPrepaymentCount(long declarationDataId) {
        return ndflPersonDao.findPersonPrepaymentCount(declarationDataId);
    }

    /**
     * Найти все данные о доходах в виде авансовых платежей привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param ndflPersonPrepaymentFilter параметры фильтра
     * @return список NdflPersonPrepayment заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_PREPAYMENT
     */
    @Override
    public PagingResult<NdflPersonPrepaymentDTO> findPersonPrepaymentByFilter(long declarationDataId, NdflPersonPrepaymentFilter ndflPersonPrepaymentFilter, PagingParams pagingParams) {
        return ndflPersonDao.findPersonPrepaymentByParameters(declarationDataId, ndflPersonPrepaymentFilter, pagingParams);
    }
}

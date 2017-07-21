package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment;
import com.aplana.sbrf.taxaccounting.service.NdflPersonService;
import com.mysema.query.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
     * @return список NdflPerson заполненый данными из таблицы NDFL_PERSON
     */
    @Override
    public int findPersonCount(long declarationDataId) {
        return ndflPersonDao.findPerson(declarationDataId).size();
    }

    /**
     * Найти все данные НДФЛ ФЛ привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param parameters значения фильтра
     * @return список NdflPerson заполненый данными из таблицы NDFL_PERSON
     */
    @Override
    public PagingResult<NdflPerson> findPersonByFilter(long declarationDataId, Map<String, Object> parameters, int startIndex, int pageSize) {
        return ndflPersonDao.findNdflPersonByParameters(declarationDataId, parameters, new PagingParams(startIndex, pageSize));
    }

    /**
     * Найти количество записей данных о доходах и НДФЛ привязанных к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return список NdflPersonIncomeFilter заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_INCOME
     */
    @Override
    public int findPersonIncomeCount(long declarationDataId) {
        return ndflPersonDao.findPersonIncome(declarationDataId).size();
    }

    /**
     * Найти все данные о доходах и НДФЛ привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param parameters значения фильтра
     * @return список NdflPersonIncomeFilter заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_INCOME
     */
    @Override
    public PagingResult<NdflPersonIncome> findPersonIncomeByFilter(long declarationDataId, Map<String, Object> parameters, int startIndex, int pageSize) {
        return ndflPersonDao.findPersonIncomeByParameters(declarationDataId, parameters, new PagingParams(startIndex, pageSize));
    }

    /**
     * Найти количество записей данных о вычетах привязанных к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return количество записей в списке NdflPersonDeduction заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_DEDUCTION
     */
    @Override
    public int findPersonDeductionsCount(long declarationDataId) {
        return ndflPersonDao.findNdflPersonDeduction(declarationDataId).size();
    }

    /**
     * Найти все данные о вычетах привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param parameters значения фильтра
     * @return список NdflPersonDeduction заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_DEDUCTION
     */
    @Override
    public PagingResult<NdflPersonDeduction> findPersonDeductionsByFilter(long declarationDataId, Map<String, Object> parameters, int startIndex, int pageSize) {
        return ndflPersonDao.findPersonDeductionByParameters(declarationDataId, parameters, new PagingParams(startIndex, pageSize));
    }

    /**
     * Найти количество записей данных о доходах в виде авансовых платежей привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return количество записей в списке NdflPersonPrepayment заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_PREPAYMENT
     */
    @Override
    public int findPersonPrepaymentCount(long declarationDataId) {
        return ndflPersonDao.findNdflPersonPrepayment(declarationDataId).size();
    }

    /**
     * Найти все данные о доходах в виде авансовых платежей привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param parameters значения фильтра
     * @return список NdflPersonPrepayment заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_PREPAYMENT
     */
    @Override
    public PagingResult<NdflPersonPrepayment> findPersonPrepaymentByFilter(long declarationDataId, Map<String, Object> parameters, int startIndex, int pageSize) {
        return ndflPersonDao.findPersonPrepaymentByParameters(declarationDataId, parameters, new PagingParams(startIndex, pageSize));
    }
}

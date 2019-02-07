package com.aplana.sbrf.taxaccounting.script.service.impl;

import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.consolidation.ConsolidationIncome;
import com.aplana.sbrf.taxaccounting.model.consolidation.ConsolidationSourceDataSearchFilter;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonOperation;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment;
import com.aplana.sbrf.taxaccounting.script.service.NdflPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrey Drunk
 */
@Service("ndflPersonService")
public class NdflPersonServiceImpl implements NdflPersonService {

    @Autowired
    NdflPersonDao ndflPersonDao;

    @Autowired
    com.aplana.sbrf.taxaccounting.service.NdflPersonService ndflPersonService;

    @Override
    public List<NdflPersonIncome> findIncomesByPeriodAndNdflPersonId(long ndflPersonId, Date startDate, Date endDate, boolean prFequals1) {
        return ndflPersonDao.fetchNdflPersonIncomeByPeriodNdflPersonId(ndflPersonId, startDate, endDate, prFequals1);
    }

    @Override
    public List<NdflPersonIncome> findIncomesByPeriodAndNdflPersonIdTemp(long ndflPersonId, Date startDate, Date endDate, boolean prFequals1) {
        return ndflPersonDao.fetchNdflPersonIncomeByPeriodNdflPersonIdTemp(ndflPersonId, startDate, endDate, prFequals1);
    }

    @Override
    public List<NdflPersonIncome> findIncomesByPeriodAndNdflPersonIdAndTaxDate(long ndflPersonId, int taxRate, Date startDate, Date endDate) {
        return ndflPersonDao.fetchNdflPersonIncomeByPeriodNdflPersonIdTaxDate(ndflPersonId, taxRate, startDate, endDate);
    }

    @Override
    public List<NdflPersonIncome> findIncomesByPayoutDate(long ndflPersonId, int taxRate, Date startDate, Date endDate) {
        return ndflPersonDao.fetchNdflPersonIncomeByPayoutDate(ndflPersonId, taxRate, startDate, endDate);
    }

    @Override
    public List<NdflPersonDeduction> findDeductionsWithDeductionsMarkOstalnie(long ndflPersonId, Date startDate, Date endDate) {
        return ndflPersonDao.fetchNdflPersonDeductionWithDeductionsMarkOstalnie(ndflPersonId, startDate, endDate);
    }

    @Override
    public List<NdflPersonDeduction> findDeductionsWithDeductionsMarkNotOstalnie(long ndflPersonId, Date startDate, Date endDate, boolean prFequals1) {
        return ndflPersonDao.fetchNdflpersonDeductionWithDeductionsMarkNotOstalnie(ndflPersonId, startDate, endDate, prFequals1);
    }

    @Override
    public int getCountNdflPerson(long declarationDataId) {
        String query = "select np.id from ndfl_person np where declaration_data_id = :declarationDataId";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("declarationDataId", declarationDataId);
        return ndflPersonDao.getCount(query, parameters);
    }

    @Override
    public List<NdflPerson> findNdflPersonByPairKppOktmo(long declarationDataId, String kpp, String oktmo, boolean is2Ndfl2) {
        return ndflPersonDao.fetchNdflPersonByPairKppOktmo(declarationDataId, kpp, oktmo, is2Ndfl2);
    }

    @Override
    public List<NdflPersonPrepayment> findPrepaymentsByPeriodAndNdflPersonId(long ndflPersonId, int taxRate, Date startDate, Date endDate, boolean prFequals1) {
        return ndflPersonDao.fetchNdflPersonPrepaymentByPeriodNdflPersonId(ndflPersonId, taxRate, startDate, endDate, prFequals1);
    }

    @Override
    public int[] updateRefBookPersonReferences(List<NaturalPerson> ndflPersonList) {
        return ndflPersonDao.updateRefBookPersonReferences(ndflPersonList);
    }

    @Override
    public Long save(NdflPerson ndflPerson) {
        return ndflPersonDao.save(ndflPerson);
    }

    @Override
    public void save(Collection<NdflPerson> ndflPersons) {
        ndflPersonDao.save(ndflPersons);
    }

    @Override
    public NdflPerson get(Long ndflPersonId) {
        return ndflPersonDao.fetchOne(ndflPersonId);
    }

    @Override
    public List<NdflPerson> findNdflPerson(long declarationDataId) {
        return ndflPersonDao.fetchByDeclarationData(declarationDataId);
    }

    @Override
    public List<NdflPerson> findAllNdflPersonsByDeclarationIds(List<Long> declarationDataIds) {
        return ndflPersonDao.findAllNdflPersonsByDeclarationIds(declarationDataIds);
    }

    @Override
    public List<NdflPerson> findNdflPersonWithOperations(long declarationDataId) {
        List<NdflPerson> persons = ndflPersonDao.fetchByDeclarationData(declarationDataId);
        Map<Long, List<NdflPersonIncome>> incomesByPersonId = groupByPersonId(ndflPersonDao.findAllIncomesByDeclarationId(declarationDataId));
        Map<Long, List<NdflPersonDeduction>> deductionsByPersonId = groupByPersonId(ndflPersonDao.findAllDeductionsByDeclarationId(declarationDataId));
        Map<Long, List<NdflPersonPrepayment>> prepaymentsByPersonId = groupByPersonId(ndflPersonDao.fetchNdflPersonPrepaymentByDeclarationData(declarationDataId));
        for (NdflPerson person : persons) {
            person.setIncomes(incomesByPersonId.get(person.getId()));
            person.setDeductions(deductionsByPersonId.get(person.getId()));
            person.setPrepayments(prepaymentsByPersonId.get(person.getId()));
        }
        return persons;
    }

    private <T extends NdflPersonOperation> Map<Long, List<T>> groupByPersonId(List<T> operations) {
        Map<Long, List<T>> operationsByPersonId = new HashMap<>();
        for (T operation : operations) {
            List<T> operationsOfPerson = operationsByPersonId.get(operation.getNdflPersonId());
            if (operationsOfPerson == null) {
                operationsOfPerson = new ArrayList<>();
                operationsByPersonId.put(operation.getNdflPersonId(), operationsOfPerson);
            }
            operationsOfPerson.add(operation);
        }
        return operationsByPersonId;
    }

    @Override
    public List<NdflPersonIncome> findNdflPersonIncome(long declarationDataId) {
        return ndflPersonDao.findAllIncomesByDeclarationId(declarationDataId);
    }

    @Override
    public List<NdflPersonIncome> findAllIncomesByDeclarationIdByOrderByRowNumAsc(long declarationDataId) {
        return ndflPersonDao.findAllIncomesByDeclarationIdByOrderByRowNumAsc(declarationDataId);
    }

    @Override
    public List<NdflPersonIncome> findAllIncomesByDeclarationIds(List<Long> declarationDataIds) {
        return ndflPersonDao.findAllIncomesByDeclarationIds(declarationDataIds);
    }

    @Override
    public List<NdflPersonIncome> findIncomesForPersonByKppOktmo(List<Long> ndflPersonId, String kpp, String oktmo) {
        return ndflPersonDao.fetchNdflPersonIncomeByNdflPersonKppOktmo(ndflPersonId, kpp, oktmo);
    }

    @Override
    public List<NdflPersonIncome> findAllIncomesByDeclarationIdAndKppAndOktmo(long declarationId, String kpp, String oktmo) {
        return ndflPersonDao.findAllIncomesByDeclarationIdAndKppAndOktmo(declarationId, kpp, oktmo);
    }

    @Override
    public List<NdflPersonIncome> findIncomesForPersonByKppOktmoAndPeriod(List<Long> ndflPersonId, String kpp, String oktmo, Date startDate, Date endDate) {
        return ndflPersonDao.fetchNdflPersonIncomeByNdflPersonKppOktmoPeriod(ndflPersonId, kpp, oktmo, startDate, endDate);
    }

    @Override
    public List<NdflPersonDeduction> findNdflPersonDeduction(long declarationDataId) {
        return ndflPersonDao.findAllDeductionsByDeclarationId(declarationDataId);
    }

    @Override
    public List<NdflPersonDeduction> findAllDeductionsByDeclarationIds(List<Long> declarationDataIds) {
        return ndflPersonDao.findAllDeductionsByDeclarationIds(declarationDataIds);
    }

    @Override
    public List<NdflPersonPrepayment> findNdflPersonPrepayment(long declarationDataId) {
        return ndflPersonDao.fetchNdflPersonPrepaymentByDeclarationData(declarationDataId);
    }

    @Override
    public List<NdflPersonIncome> findIncomes(long ndflPersonId) {
        return ndflPersonDao.fetchNdflPersonIncomeByNdflPerson(ndflPersonId);
    }

    @Override
    public List<NdflPersonDeduction> findDeductions(long ndflPersonId) {
        return ndflPersonDao.fetchNdflPersonDeductionByNdflPerson(ndflPersonId);
    }

    @Override
    public List<NdflPersonPrepayment> findPrepayments(long ndflPersonId) {
        return ndflPersonDao.fetchNdflPersonPrepaymentByNdflPerson(ndflPersonId);
    }

    @Override
    public PagingResult<NdflPerson> findNdflPersonByParameters(long declarationDataId, Map<String, Object> subreportParameters) {
        return ndflPersonDao.fetchNdflPersonByParameters(declarationDataId, subreportParameters, new PagingParams());
    }

    @Override
    public PagingResult<NdflPerson> findNdflPersonByParameters(long declarationDataId, Map<String, Object> parameters, int startIndex, int pageSize) {
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }
        return ndflPersonDao.fetchNdflPersonByParameters(declarationDataId, parameters, new PagingParams(startIndex, pageSize));
    }

    @Override
    public int findNdflPersonCountByParameters(long declarationDataId, Map<String, Object> parameters) {
        return ndflPersonDao.getNdflPersonCountByParameters(declarationDataId, parameters);
    }

    @Override
    public List<NdflPersonPrepayment> findPrepaymentsByNdflPersonIdList(List<Long> ndflPersonIdList) {
        return ndflPersonDao.fetchNdlPersonPrepaymentByNdflPersonIdList(ndflPersonIdList);
    }

    @Override
    public List<NdflPersonIncome> findIncomesByPeriodAndNdflPersonIdList(List<Long> ndflPersonIdList, Date startDate, Date endDate) {
        return ndflPersonDao.fetchIncomesByPeriodAndNdflPersonIdList(ndflPersonIdList, startDate, endDate);
    }

    @Override
    public long deleteAll(long declarationDataId) {
        return ndflPersonDao.deleteByDeclarationId(declarationDataId);
    }

    @Override
    public List<NdflPersonIncome> findNdflPersonIncomeConsolidatedRNU2Ndfl(long declarationDataId, String kpp, String oktmo) {
        return ndflPersonDao.fetchNdflPersonIncomeConsolidatedRNU2Ndfl(declarationDataId, kpp, oktmo);
    }

    @Override
    public List<NdflPersonIncome> findNdflPersonIncomeConsolidatedRNU6Ndfl(long declarationDataId, String kpp, String oktmo) {
        return ndflPersonDao.fetchNdflPersonIncomeConsolidatedRNU6Ndfl(declarationDataId, kpp, oktmo);
    }

    @Override
    public List<NdflPersonDeduction> findDeductionsByNdflPersonAndOperation(long ndflPersonId, String operationId) {
        return ndflPersonDao.fetchNdflPersonDeductionByNdflPersonAndOperation(ndflPersonId, operationId);
    }

    @Override
    public List<NdflPersonPrepayment> findPrepaymentsByNdflPersonAndOperation(long ndflPersonId, String operationId) {
        return ndflPersonDao.fetchNdflPeronPrepaymentByNdflPersonAndOperation(ndflPersonId, operationId);
    }

    @Override
    public NdflPersonIncome getIncome(long id) {
        return ndflPersonDao.fetchOneNdflPersonIncome(id);
    }

    @Override
    public NdflPersonDeduction getDeduction(long id) {
        return ndflPersonDao.fetchOneNdflPersonDeduction(id);
    }

    @Override
    public NdflPersonPrepayment getPrepayment(long id) {
        return ndflPersonDao.fetchOneNdflPersonPrepayment(id);
    }

    @Override
    public List<NdflPerson> findByIdList(List<Long> ndflPersonIdList) {
        return ndflPersonDao.fetchNdflPersonByIdList(ndflPersonIdList);
    }

    @Override
    public List<Integer> findDublRowNum(String tableName, Long declarationDataId) {
        return ndflPersonDao.fetchDublByRowNum(tableName, declarationDataId);
    }


    @Override
    public Map<Long, List<Integer>> findDublRowNumMap(String tableName, Long declarationDataId) {
        return ndflPersonDao.fetchDublByRowNumMap(tableName, declarationDataId);
    }

    @Override
    public List<Integer> findMissingRowNum(String tableName, Long declarationDataId) {
        return ndflPersonDao.findMissingRowNum(tableName, declarationDataId);
    }

    @Override
    public Map<Long, List<Integer>> findMissingRowNumMap(String tableName, Long declarationDataId) {
        return ndflPersonDao.findMissingRowNumMap(tableName, declarationDataId);
    }

    @Override
    public List<String> fetchIncomeOperationIdRange(String startOperationId, String endOperationId) {
        return ndflPersonDao.fetchIncomeOperationIdRange(startOperationId, endOperationId);
    }

    @Override
    public List<String> findIncomeOperationId(List<String> operationIdList) {
        return ndflPersonDao.findIncomeOperationId(operationIdList);
    }

    @Override
    public List<Long> fetchIncomeIdByNdflPerson(long ndflPersonId) {
        return ndflPersonDao.fetchIncomeIdByNdflPerson(ndflPersonId);
    }

    @Override
    public List<Long> fetchDeductionIdByNdflPerson(long ndflPersonId) {
        return ndflPersonDao.fetchDeductionIdByNdflPerson(ndflPersonId);
    }

    @Override
    public List<Long> fetchPrepaymentIdByNdflPerson(long ndflPersonId) {
        return ndflPersonDao.fetchPrepaymentIdByNdflPerson(ndflPersonId);
    }

    @Override
    public void deleteNdflPersonIncome(List<Long> ids) {
        ndflPersonDao.deleteNdflPersonIncomeBatch(ids);
    }

    @Override
    public void deleteNdflPersonDeduction(List<Long> ids) {
        ndflPersonDao.deleteNdflPersonDeductionBatch(ids);
    }

    @Override
    public void deleteNdflPersonPrepayment(List<Long> ids) {
        ndflPersonDao.deleteNdflPersonPrepaymentBatch(ids);
    }

    @Override
    public void deleteNdflPersonBatch(List<Long> ids) {
        ndflPersonDao.deleteNdflPersonBatch(ids);
    }

    @Override
    public boolean incomeExistsByDeclarationId(long declarationDataId) {
        return ndflPersonDao.incomeExistsByDeclarationId(declarationDataId);
    }

    @Override
    public boolean checkIncomeExists(long ndflPersonIncomeId, long declarationDataId) {
        return ndflPersonDao.checkIncomeExists(ndflPersonIncomeId, declarationDataId);
    }

    @Override
    public boolean checkDeductionExists(long ndflPersonDeductionId, long declarationDataId) {
        return ndflPersonDao.checkDeductionExists(ndflPersonDeductionId, declarationDataId);
    }

    @Override
    public boolean checkPrepaymentExists(long ndflPersonPrepaymentId, long declarationDataId) {
        return ndflPersonDao.checkPrepaymentExists(ndflPersonPrepaymentId, declarationDataId);
    }

    @Override
    public void saveIncomes(List<NdflPersonIncome> incomes) {
        ndflPersonDao.saveIncomes(incomes);
    }

    @Override
    public void saveDeductions(List<NdflPersonDeduction> deductions) {
        ndflPersonDao.saveDeductions(deductions);
    }

    @Override
    public void savePrepayments(List<NdflPersonPrepayment> prepayments) {
        ndflPersonDao.savePrepayments(prepayments);
    }

    @Override
    public void updateNdflPersons(List<NdflPerson> persons) {
        ndflPersonDao.updateNdflPersons(persons);
    }

    @Override
    public void updateIncomes(List<NdflPersonIncome> incomes) {
        ndflPersonDao.updateIncomes(incomes);
    }

    @Override
    public void updateDeductions(List<NdflPersonDeduction> deductions) {
        ndflPersonDao.updateDeductions(deductions);
    }

    @Override
    public void updatePrepayments(List<NdflPersonPrepayment> prepayments) {
        ndflPersonDao.updatePrepayments(prepayments);
    }

    @Override
    public void updateRowNum(List<NdflPerson> ndflPersons) {
        updateNdflPersonsRowNum(ndflPersons);
        List<NdflPersonIncome> incomes = new ArrayList<>();
        for (NdflPerson ndflPerson : ndflPersons) {
            incomes.addAll(ndflPerson.getIncomes());
        }
        updateIncomesRowNum(incomes);
        List<NdflPersonDeduction> deductions = new ArrayList<>();
        for (NdflPerson ndflPerson : ndflPersons) {
            deductions.addAll(ndflPerson.getDeductions());
        }
        updateDeductionsRowNum(deductions);
        List<NdflPersonPrepayment> prepayments = new ArrayList<>();
        for (NdflPerson ndflPerson : ndflPersons) {
            prepayments.addAll(ndflPerson.getPrepayments());
        }
        updatePrepaymentsRowNum(prepayments);
    }

    @Override
    public void updateNdflPersonsRowNum(List<NdflPerson> persons) {
        ndflPersonDao.updateNdflPersonsRowNum(persons);
    }

    @Override
    public void updateIncomesRowNum(List<NdflPersonIncome> incomes) {
        ndflPersonDao.updateIncomesRowNum(incomes);
    }

    @Override
    public void updateDeductionsRowNum(List<NdflPersonDeduction> deductions) {
        ndflPersonDao.updateDeductionsRowNum(deductions);
    }

    @Override
    public void updatePrepaymentsRowNum(List<NdflPersonPrepayment> prepayments) {
        ndflPersonDao.updatePrepaymentsRowNum(prepayments);
    }

    @Override
    public int findInpCountWithPositiveIncomeByPersonIdsAndAccruedIncomeDatePeriod(List<Long> ndflPersonIdList, Date periodStartDate, Date periodEndDate) {
        return ndflPersonDao.findInpCountWithPositiveIncomeByPersonIdsAndAccruedIncomeDatePeriod(ndflPersonIdList, periodStartDate, periodEndDate);
    }

    @Override
    public List<NdflPersonPrepayment> fetchPrepaymentByIncomesIdAndAccruedDate(List<Long> ndflPersonIncomeIdList, Date periodStartDate, Date periodEndDate) {
        return ndflPersonDao.fetchPrepaymentByIncomesIdAndAccruedDate(ndflPersonIncomeIdList, periodStartDate, periodEndDate);
    }

    @Override
    public List<NdflPerson> fetchRefBookPersonsAsNdflPerson(Long declarationDataId, Date actualDate) {
        return ndflPersonDao.fetchRefBookPersonsAsNdflPerson(declarationDataId, actualDate);
    }

    @Override
    public List<ConsolidationIncome> fetchIncomeSourcesConsolidation(ConsolidationSourceDataSearchFilter searchData) {
        return ndflPersonDao.fetchIncomeSourcesConsolidation(searchData);
    }

    @Override
    public List<NdflPersonDeduction> fetchDeductionsForConsolidation(List<Long> incomeIds) {
        return ndflPersonDao.fetchDeductionsForConsolidation(incomeIds);
    }

    @Override
    public List<NdflPersonPrepayment> fetchPrepaymentsForConsolidation(List<Long> incomeIds) {
        return ndflPersonDao.fetchPrepaymentsForConsolidation(incomeIds);
    }

    @Override
    public List<NdflPerson> fetchRefBookPersonsAsNdflPerson(List<Long> ndflPersonIdList, Date actualDate) {
        return ndflPersonDao.fetchRefBookPersonsAsNdflPerson(ndflPersonIdList, actualDate);
    }

    @Override
    public void fillNdflPersonIncomeSortFields(List<NdflPerson> ndflPersonList) {
        ndflPersonService.fillNdflPersonIncomeSortFields(ndflPersonList);
    }
}

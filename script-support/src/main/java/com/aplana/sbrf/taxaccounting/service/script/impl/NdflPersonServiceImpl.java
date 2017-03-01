package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ndfl.*;
import com.aplana.sbrf.taxaccounting.service.script.NdflPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Andrey Drunk
 */
@Service("ndflPersonService")
public class NdflPersonServiceImpl implements NdflPersonService {

    @Autowired
    NdflPersonDao ndflPersonDao;

    @Override
    public List<NdflPersonIncome> findIncomesByPeriodAndNdflPersonId(long ndflPersonId, Date startDate, Date endDate) {
        return ndflPersonDao.findIncomesByPeriodAndNdflPersonId(ndflPersonId, startDate, endDate);
    }

    @Override
    public List<NdflPersonDeduction> findDeductionsByPeriodAndNdflPersonId(long ndflPersonId, Date startDate, Date endDate) {
        return ndflPersonDao.findDeductionsByPeriodAndNdflPersonId(ndflPersonId, startDate, endDate);
    }

    @Override
    public PagingResult<NdflPerson> findNdflPersonByParameters(long declarationDataId, Map<String, Object> parameters, int startIndex, int pageSize) {
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }
        return ndflPersonDao.findNdflPersonByParameters(declarationDataId, parameters, new PagingParams(startIndex, pageSize));
    }

    @Override
    public int getCountNdflPerson(long declarationDataId) {
        String query = "select np.id from ndfl_person np where declaration_data_id = :declarationDataId";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("declarationDataId", declarationDataId);
        return ndflPersonDao.getCount(query, parameters);
    }

    @Override
    public List<NdflPerson> findNdflPersonByPairKppOktmo(long declarationDataId, String kpp, String oktmo) {
        return ndflPersonDao.findNdflPersonByPairKppOktmo(declarationDataId, kpp, oktmo);
    }

    @Override
    public List<NdflPersonPrepayment> findPrepaymentsByPeriodAndNdflPersonId(long ndflPersonId, Date startDate, Date endDate) {
        return ndflPersonDao.findPrepaymentsByPeriodAndNdflPersonId(ndflPersonId, startDate, endDate);
    }

    @Override
    public int[] updatePersonRefBookReferences(List<NdflPerson> ndflPersonList) {
        return ndflPersonDao.updatePersonRefBookReferences(ndflPersonList);
    }

    @Override
    public Long save(NdflPerson ndflPerson) {
        return ndflPersonDao.save(ndflPerson);
    }

    @Override
    public NdflPerson get(Long ndflPersonId) {
        return ndflPersonDao.get(ndflPersonId);
    }

    @Override
    public List<NdflPerson> findNdflPerson(long declarationDataId) {
        return ndflPersonDao.findPerson(declarationDataId);
    }

    @Override
    public List<NdflPersonIncome> findNdflPersonIncome(long declarationDataId) {
        return ndflPersonDao.findPersonIncome(declarationDataId);
    }

    @Override
    public List<NdflPersonIncome> findIncomesForPersonByKppOktmo(long ndflPersonId, String kpp, String oktmo) {
        return ndflPersonDao.findIncomesForPersonByKppOktmo(ndflPersonId, kpp, oktmo);
    }

    @Override
    public List<NdflPersonDeduction> findNdflPersonDeduction(long declarationDataId) {
        return ndflPersonDao.findNdflPersonDeduction(declarationDataId);
    }

    @Override
    public List<NdflPersonPrepayment> findNdflPersonPrepayment(long declarationDataId) {
        return ndflPersonDao.findNdflPersonPrepayment(declarationDataId);
    }

    @Override
    public List<NdflPersonDeduction> findDeductions(long ndflPersonId) {
        return ndflPersonDao.findDeductions(ndflPersonId);
    }

    @Override
    public List<NdflPersonPrepayment> findPrepayments(long ndflPersonId) {
        return ndflPersonDao.findPrepayments(ndflPersonId);
    }

    @Override
    public PagingResult<NdflPerson> findNdflPersonByParameters(long declarationDataId, Map<String, Object> subreportParameters) {
        return ndflPersonDao.findNdflPersonByParameters(declarationDataId, subreportParameters, new PagingParams());
    }

    @Override
    public List<NdflPersonPrepayment> findPrepaymentsByDeclarationDataId(long declarationDataId, String kpp, String oktmo) {
        return ndflPersonDao.findPrepaymentsByDeclarationDataId(declarationDataId, kpp, oktmo);
    }

    @Override
    public List<NdflPersonIncome> findIncomesByPeriodAndDeclarationDataId(long declarationDataId, Date startDate, Date endDate, String kpp, String oktmo) {
        return ndflPersonDao.findIncomesByPeriodAndDeclarationDataId(declarationDataId, startDate, endDate, kpp, oktmo);
    }

    @Override
    public void deleteAll(long declarationDataId) {
        List<NdflPerson> ndflPersonList = findNdflPerson(declarationDataId);
        for (NdflPerson ndflPerson : ndflPersonList) {
            ndflPersonDao.delete(ndflPerson.getId());
        }
    }

    @Override
    public List<NdflPersonIncome> findNdflPersonIncomeConsolidatedRNU2Ndfl(long declarationDataId, String kpp, String oktmo) {
        return ndflPersonDao.findNdflPersonIncomeConsolidatedRNU2Ndfl(declarationDataId, kpp, oktmo);
    }

    @Override
    public List<NdflPersonIncome> findNdflPersonIncomeConsolidatedRNU6Ndfl(long declarationDataId, String kpp, String oktmo) {
        return ndflPersonDao.findNdflPersonIncomeConsolidatedRNU2Ndfl(declarationDataId, kpp, oktmo);
    }

    @Override
    public List<NdflPersonDeduction> findDeductionsByNdflPersonAndOperation(long ndflPersonId, long operationId) {
        return ndflPersonDao.findDeductionsByNdflPersonAndOperation(ndflPersonId, operationId);
    }

    @Override
    public List<NdflPersonPrepayment> findPrepaymentsByNdflPersonAndOperation(long ndflPersonId, long operationId) {
        return ndflPersonDao.findPrepaymentsByNdflPersonAndOperation(ndflPersonId, operationId);
    }

    @Override
    public NdflPersonIncome getIncome(long id) {
        return ndflPersonDao.getIncome(id);
    }

    @Override
    public NdflPersonDeduction getDeduction(long id) {
        return ndflPersonDao.getDeduction(id);
    }

    @Override
    public NdflPersonPrepayment getPrepayment(long id) {
        return ndflPersonDao.getPrepayment(id);
    }
}

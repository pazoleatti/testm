package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.ndfl.*;
import com.aplana.sbrf.taxaccounting.service.script.NdflPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    public List<NdflSumByRate> findNdflSumByRate(long declarationDataId) {
        /*
        1. Суммируем Авансы в рамках ОПЕРАЦИИ
        2. Суммируем Доходы в рамках ОПЕРАЦИИ
        3. Связываем Аванс с Доходом на основании операции
        4. Суммируем Авансы между собой в рамках ставки и суммируем Доходы между собой в рамках ставки
         */

        // 1. Суммируем Авансы в рамках операции
        // Мапа <Номер_операции, Сумма_аванса>
        Map<Long, BigDecimal> mapSumPrepayment = new HashMap<Long, BigDecimal>();
        List<NdflPersonPrepayment> ndflPersonPrepaymentList = ndflPersonDao.findPrepaymentsByDeclarationDataId(declarationDataId);
        for (NdflPersonPrepayment ndflPersonPrepayment : ndflPersonPrepaymentList) {
            BigDecimal summPrepayment = mapSumPrepayment.get(ndflPersonPrepayment.getOperationId());
            if (summPrepayment == null) {
                summPrepayment = ndflPersonPrepayment.getSumm();
                mapSumPrepayment.put(ndflPersonPrepayment.getOperationId(), summPrepayment);
            } else {
                summPrepayment.add(ndflPersonPrepayment.getSumm());
            }
        }

        // Мапа <Номер_операции, Суммы>
        // Мапа <Ставка, Мапа>
        Map<Integer, Map> mapTaxRate = new HashMap<Integer, Map>();


        // 2. Суммируем Доходы в рамках ОПЕРАЦИИ
        List<NdflPersonIncome> ndflPersonIncomeList = ndflPersonDao.findIncomesByDeclarationDataId(declarationDataId);
        for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {
            Map<Long, NdflSumByRate> mapO = mapTaxRate.get(ndflPersonIncome.getTaxRate());
            if (mapO.isEmpty()) {
                // Данной ставки и операции еще нет в Мапе
                mapO = new HashMap<Long, NdflSumByRate>();
                NdflSumByRate ndflSumByRate = new NdflSumByRate();
                ndflSumByRate.setTaxRate(ndflPersonIncome.getTaxRate());
                ndflSumByRate.setIncomeAccruedSumm(ndflPersonIncome.getIncomeAccruedSumm());
                ndflSumByRate.setCalculatedTax(ndflPersonIncome.getCalculatedTax());
                if (ndflPersonIncome.getIncomeCode() == NdflSumByRate.INCOME_CODE_DIV) {
                    ndflSumByRate.setIncomeAccruedSummDiv(ndflPersonIncome.getIncomeAccruedSumm());
                    ndflSumByRate.setCalculatedTaxDiv(ndflPersonIncome.getCalculatedTax());
                }
                mapO.put(ndflPersonIncome.getOperationId(), ndflSumByRate);
                mapTaxRate.put(ndflPersonIncome.getTaxRate(), mapO);
            } else {
                NdflSumByRate ndflSumByRate = mapO.get(ndflPersonIncome.getOperationId());
                if (ndflSumByRate == null) {
                    // Данной операции еще нет в Мапе
                    ndflSumByRate = new NdflSumByRate();
                    mapO.put(ndflPersonIncome.getOperationId(), ndflSumByRate);
                }
                ndflSumByRate.addIncomeAccruedSumm(ndflPersonIncome.getIncomeAccruedSumm());
                ndflSumByRate.addCalculatedTax(ndflPersonIncome.getCalculatedTax());
                if (ndflPersonIncome.getIncomeCode() == NdflSumByRate.INCOME_CODE_DIV) {
                    ndflSumByRate.addIncomeAccruedSummDiv(ndflPersonIncome.getIncomeAccruedSumm());
                    ndflSumByRate.addCalculatedTaxDiv(ndflPersonIncome.getCalculatedTax());
                }
            }
        }

        List<NdflSumByRate> ndflSumByRateList = new ArrayList<NdflSumByRate>();

        for (Map.Entry<Integer, Map> iterTaxRate : mapTaxRate.entrySet()) {
            Map<Long, NdflSumByRate> mapO = iterTaxRate.getValue();
            NdflSumByRate ndflSumByRate = new NdflSumByRate();
            for (Map.Entry<Long, NdflSumByRate> iterOperation : mapO.entrySet()) {
                NdflSumByRate sbr = iterOperation.getValue();
                BigDecimal summPrepayment = mapSumPrepayment.get(iterOperation.getKey());

                // 3. Связываем Аванс с Доходом на основании операции
                sbr.setPrepaymentSum(summPrepayment);

                // 4. Суммируем Авансы между собой в рамках ставки и суммируем Доходы между собой в рамках ставки
                ndflSumByRate.addNdflSumByRate(sbr);
            }
            ndflSumByRateList.add(ndflSumByRate);
        }
        return ndflSumByRateList;
    }

    @Override
    public List<NdflSumByDate> findNdflSumByDate(long declarationDataId) {
        Map<String, NdflSumByDate> mapNdflPersonIncome = new HashMap<String, NdflSumByDate>();
        List<NdflPersonIncome> ndflPersonIncomeList = ndflPersonDao.findIncomesByDeclarationDataId(declarationDataId);

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {
            String key = df.format(ndflPersonIncome.getIncomeAccruedDate() + "_" + ndflPersonIncome.getTaxDate() +
                    "_" + ndflPersonIncome.getTaxTransferDate());
            NdflSumByDate ndflSumByDate = mapNdflPersonIncome.get(key);
            if (ndflSumByDate == null) {
                ndflSumByDate = new NdflSumByDate();
                ndflSumByDate.setIncomeAccruedDate(ndflPersonIncome.getIncomeAccruedDate());
                ndflSumByDate.setTaxDate(ndflPersonIncome.getTaxDate());
                ndflSumByDate.setTaxTransferDate(ndflPersonIncome.getTaxTransferDate());
                ndflSumByDate.setIncomePayoutSumm(ndflPersonIncome.getIncomePayoutSumm());
                ndflSumByDate.setWithholdingTax(ndflPersonIncome.getWithholdingTax());
                mapNdflPersonIncome.put(key, ndflSumByDate);
            } else {
                ndflSumByDate.addIncomePayoutSumm(ndflPersonIncome.getIncomePayoutSumm());
                ndflSumByDate.addWithholdingTax(ndflPersonIncome.getWithholdingTax());
            }
        }
        return new ArrayList<NdflSumByDate>(mapNdflPersonIncome.values());
    }

    @Override
    public void deleteAll(long declarationDataId) {
        List<NdflPerson> ndflPersonList = findNdflPerson(declarationDataId);
        for (NdflPerson ndflPerson : ndflPersonList) {
            ndflPersonDao.delete(ndflPerson.getId());
        }
    }

}

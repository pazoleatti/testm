package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.ndfl.*;
import com.aplana.sbrf.taxaccounting.service.script.NdflPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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
    public NdflPersonIncomeCommonValue findNdflPersonIncomeCommonValue(long declarationDataId, Date startDate, Date endDate) {
        /*
        Одним из полей Обобщенных показателей о доходах является перечень просуммированых доходов и авансов для каждой ставки.
        В рамках одной ставки может быть несколько операций.
        Доходы одной операции имеют одну и только одну ставку.
        Каждая операция имеет свой номер.
        В рамках одной операции может быть несколько доходов и авансов.
        Аванс и доход связыны между собой по номеру операции.

        1. Суммируем Авансы в рамках ОПЕРАЦИИ
        2. Суммируем Доходы в рамках ОПЕРАЦИИ
        3. Связываем Аванс с Доходом на основании операции
        4. Суммируем Авансы между собой в рамках ставки и суммируем Доходы между собой в рамках ставки
         */

        // Обобщенные показатели о доходах
        NdflPersonIncomeCommonValue ndflPersonIncomeCommonValue = new NdflPersonIncomeCommonValue();

        // Перечень уникальных идентификаторов ФЛ
        List<Long> personIds = new ArrayList<Long>();

        // 1. Суммируем Авансы в рамках операции
        // Мапа <Номер_операции, Сумма_аванса>
        Map<Long, Long> mapSumPrepayment = new HashMap<Long, Long>();
        List<NdflPersonPrepayment> ndflPersonPrepaymentList = ndflPersonDao.findPrepaymentsByDeclarationDataId(declarationDataId);
        for (NdflPersonPrepayment ndflPersonPrepayment : ndflPersonPrepaymentList) {
            Long summPrepayment = mapSumPrepayment.get(ndflPersonPrepayment.getOperationId());
            if (summPrepayment == null) {
                summPrepayment = ndflPersonPrepayment.getSumm();
            } else {
                summPrepayment += ndflPersonPrepayment.getSumm();
            }
            mapSumPrepayment.put(ndflPersonPrepayment.getOperationId(), summPrepayment);
        }

        // Мапа <Номер_операции, Суммы>
        // Мапа <Ставка, Мапа>
        Map<Integer, Map> mapTaxRate = new HashMap<Integer, Map>();

        List<NdflPersonIncome> ndflPersonIncomeList = ndflPersonDao.findIncomesByPeriodAndDeclarationDataId(declarationDataId, startDate, endDate);
        for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {

            // Обобщенные показатели о доходах
            if (!personIds.contains(ndflPersonIncome.getNdflPersonId())) {
                personIds.add(ndflPersonIncome.getNdflPersonId());
            }
            ndflPersonIncomeCommonValue.addWithholdingTax(ndflPersonIncome.getWithholdingTax());
            ndflPersonIncomeCommonValue.addNotHoldingTax(ndflPersonIncome.getNotHoldingTax());
            ndflPersonIncomeCommonValue.addRefoundTax(ndflPersonIncome.getRefoundTax());

            // 2. Суммируем Доходы в рамках ОПЕРАЦИИ
            Map<Long, NdflPersonIncomeByRate> mapO = mapTaxRate.get(ndflPersonIncome.getTaxRate());
            if (mapO.isEmpty()) {
                // Данной СТАВКИ еще нет в Мапе
                mapO = new HashMap<Long, NdflPersonIncomeByRate>();
                NdflPersonIncomeByRate ndflPersonIncomeByRate = new NdflPersonIncomeByRate();
                ndflPersonIncomeByRate.setTaxRate(ndflPersonIncome.getTaxRate());
                ndflPersonIncomeByRate.setIncomeAccruedSumm(ndflPersonIncome.getIncomeAccruedSumm());
                ndflPersonIncomeByRate.setTotalDeductionsSumm(ndflPersonIncome.getTotalDeductionsSumm());
                ndflPersonIncomeByRate.setCalculatedTax(ndflPersonIncome.getCalculatedTax());
                if (ndflPersonIncome.getIncomeCode() == NdflPersonIncomeByRate.INCOME_CODE_DIV) {
                    ndflPersonIncomeByRate.setIncomeAccruedSummDiv(ndflPersonIncome.getIncomeAccruedSumm());
                    ndflPersonIncomeByRate.setCalculatedTaxDiv(ndflPersonIncome.getCalculatedTax());
                }
                mapO.put(ndflPersonIncome.getOperationId(), ndflPersonIncomeByRate);
                mapTaxRate.put(ndflPersonIncome.getTaxRate(), mapO);
            } else {
                // Данная СТАВКА есть в Мапе
                NdflPersonIncomeByRate ndflPersonIncomeByRate = mapO.get(ndflPersonIncome.getOperationId());
                if (ndflPersonIncomeByRate == null) {
                    // Данной ОПЕРАЦИИ еще нет в Мапе
                    ndflPersonIncomeByRate = new NdflPersonIncomeByRate();
                    mapO.put(ndflPersonIncome.getOperationId(), ndflPersonIncomeByRate);
                }
                // Просуммируем доходы в рамках ОПЕРАЦИИ
                ndflPersonIncomeByRate.addIncomeAccruedSumm(ndflPersonIncome.getIncomeAccruedSumm());
                ndflPersonIncomeByRate.addTotalDeductionsSumm(ndflPersonIncome.getTotalDeductionsSumm());
                ndflPersonIncomeByRate.addCalculatedTax(ndflPersonIncome.getCalculatedTax());
                if (ndflPersonIncome.getIncomeCode() == NdflPersonIncomeByRate.INCOME_CODE_DIV) {
                    ndflPersonIncomeByRate.addIncomeAccruedSummDiv(ndflPersonIncome.getIncomeAccruedSumm());
                    ndflPersonIncomeByRate.addCalculatedTaxDiv(ndflPersonIncome.getCalculatedTax());
                }
                mapO.put(ndflPersonIncome.getOperationId(), ndflPersonIncomeByRate);
            }
        }

        // Перечень объектов с просуммироваными доходами и авансами в рамках СТАВКИ
        List<NdflPersonIncomeByRate> ndflPersonIncomeByRateList = new ArrayList<NdflPersonIncomeByRate>();

        for (Map.Entry<Integer, Map> iterTaxRate: mapTaxRate.entrySet()) {
            Map<Long, NdflPersonIncomeByRate> mapO = iterTaxRate.getValue();
            // Объект для хранения просуммированых доходов и авансов в рамках СТАВКИ
            NdflPersonIncomeByRate ndflPersonIncomeByRate = new NdflPersonIncomeByRate();
            for (Map.Entry<Long, NdflPersonIncomeByRate> iterOperation  : mapO.entrySet()) {
                NdflPersonIncomeByRate sbr = iterOperation.getValue();

                // Получим Аванс для конкретной операции
                Long summPrepayment = mapSumPrepayment.get(iterOperation.getKey());

                // 3. Связываем Аванс с Доходом для конкретной операции
                sbr.setPrepaymentSum(summPrepayment);

                // 4. Суммируем Авансы между собой в рамках ставки и суммируем Доходы между собой в рамках ставки
                ndflPersonIncomeByRate.addNdflSumByRate(sbr);
            }
            ndflPersonIncomeByRateList.add(ndflPersonIncomeByRate);
        }

        ndflPersonIncomeCommonValue.setNdflPersonIncomeByRateList(ndflPersonIncomeByRateList);
        ndflPersonIncomeCommonValue.setCountPerson(personIds.size());
        return ndflPersonIncomeCommonValue;
    }

    @Override
    public List<NdflPersonIncomeByDate> findNdflPersonIncomeByDate(long declarationDataId, Date startDate, Date endDate) {
        /*
        Метод возвращает просуммированные доходы и налоги, группируя их по трем датам:
        - Дата начисления дохода
        - Дата удержания налога
        - Срок (дата) перечисления налога
         */
        Map<String, NdflPersonIncomeByDate> mapNdflPersonIncome = new HashMap<String, NdflPersonIncomeByDate>();
        List<NdflPersonIncome> ndflPersonIncomeList = ndflPersonDao.findIncomesByPeriodAndDeclarationDataId(declarationDataId, startDate, endDate);

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {
            String key = df.format(ndflPersonIncome.getIncomeAccruedDate() + "_" + ndflPersonIncome.getTaxDate() +
                    "_" + ndflPersonIncome.getTaxTransferDate());
            NdflPersonIncomeByDate ndflPersonIncomeByDate = mapNdflPersonIncome.get(key);
            if (ndflPersonIncomeByDate == null) {
                ndflPersonIncomeByDate = new NdflPersonIncomeByDate();
                ndflPersonIncomeByDate.setIncomeAccruedDate(ndflPersonIncome.getIncomeAccruedDate());
                ndflPersonIncomeByDate.setTaxDate(ndflPersonIncome.getTaxDate());
                ndflPersonIncomeByDate.setTaxTransferDate(ndflPersonIncome.getTaxTransferDate());
                ndflPersonIncomeByDate.setIncomePayoutSumm(ndflPersonIncome.getIncomePayoutSumm());
                ndflPersonIncomeByDate.setWithholdingTax(ndflPersonIncome.getWithholdingTax());
                mapNdflPersonIncome.put(key, ndflPersonIncomeByDate);
            } else {
                ndflPersonIncomeByDate.addIncomePayoutSumm(ndflPersonIncome.getIncomePayoutSumm());
                ndflPersonIncomeByDate.addWithholdingTax(ndflPersonIncome.getWithholdingTax());
            }
        }
        return new ArrayList<NdflPersonIncomeByDate>(mapNdflPersonIncome.values());
    }

    @Override
    public void deleteAll(long declarationDataId) {
        List<NdflPerson> ndflPersonList = findNdflPerson(declarationDataId);
        for (NdflPerson ndflPerson : ndflPersonList) {
            ndflPersonDao.delete(ndflPerson.getId());
        }
    }

}

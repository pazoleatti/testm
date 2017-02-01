package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
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
    public List<NdflPersonIncome> findNdflPersonIncome(long declarationDataId) {
        return ndflPersonDao.findPersonIncome(declarationDataId);
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
    public PagingResult<NdflPerson> findNdflPersonByParameters(long declarationDataId, Map<String, Object> subreportParameters) {
        return ndflPersonDao.findNdflPersonByParameters(declarationDataId, subreportParameters, new PagingParams());
    }

    @Override
    public NdflPersonIncomeCommonValue findNdflPersonIncomeCommonValue(long declarationDataId, Date startDate, Date endDate) {
        /*
        Одним из полей Обобщенных показателей о доходах является перечень просуммированых доходов и авансов для каждой ставки.
        В рамках одной ставки может быть несколько операций.
        Доходы одной операции имеют одну и только одну ставку.
        Каждая операция имеет свой идентификатор.
        В рамках одной операции может быть несколько доходов и авансов.
        Аванс и доход связыны между собой по идентификатору операции.

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
        // Мапа <Идентификатор_операции, Сумма_аванса>
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

        // Мапа <Идентификатор_операции, Суммы>
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
            if (mapO == null) {
                // Данной СТАВКИ еще нет в Мапе
                mapO = new HashMap<Long, NdflPersonIncomeByRate>();
                NdflPersonIncomeByRate ndflPersonIncomeByRate = new NdflPersonIncomeByRate();
                ndflPersonIncomeByRate.setTaxRate(ndflPersonIncome.getTaxRate());
                ndflPersonIncomeByRate.setIncomeAccruedSumm(ndflPersonIncome.getIncomeAccruedSumm());
                ndflPersonIncomeByRate.setTotalDeductionsSumm(ndflPersonIncome.getTotalDeductionsSumm());
                ndflPersonIncomeByRate.setCalculatedTax(ndflPersonIncome.getCalculatedTax());
                if (ndflPersonIncome.getIncomeCode().equals(NdflPersonIncomeByRate.INCOME_CODE_DIV)) {
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
                if (ndflPersonIncome.getIncomeCode().equals(NdflPersonIncomeByRate.INCOME_CODE_DIV)) {
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
            ndflPersonIncomeByRate.setTaxRate(iterTaxRate.getKey());
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
    public List<NdflPersonIncomeByDate> findNdflPersonIncomeByDate(long declarationDataId, Date calendarStartDate, Date endDate) {
        /*
        Для заполнения СумДата будем учитывать только записи, в которых выполнены условия:
        "Дата удержания налога" или "Дата платежного поручения" должно быть заполнено и то что заполнено >= даты начала последнего квартала отчетного периода.
        "Дата удержания налога" или "Дата платежного поручения" должно быть заполнено и то что заполнено <= даты окончания последнего квартала отчетного периода.

        Из выбранных будем учитывать только те записи, в которых обязательно заполнено одно из полей: либо "Дата начисления дохода", либо "Сумма налога удержанная".
        Одновременно заполненных полей "Дата начисления дохода" и "Сумма налога удержанная" в одной и той же записи быть не может.
        Но могут быть записи, в которых не заполнены оба поля "Дата начисления дохода" и "Сумма налога удержанная" - такие записи мы учитывать не будем.

        Выбранные записи группируем по парам на основании ID операции.

        В каждой паре поля будут заполнятся следующим образом:
        "Дата начисления дохода" берем только из той записи пары, в которой оно заполнено.
        "Дата налога" берем из той записи пары, в которой заполнено поле "Сумма налога удержанная".
        "Срок перечисления налога в бюджет" берем только из той записи пары, в которой заполнено поле "Сумма налога удержанная".
        "Сумма выплаченного дохода" суммируем для всех записей пары.
        "Сумма налога удержанная" суммируем для всех записей пары.
         */
        // Мапа <Идентификатор_операции, Суммы_по_датам>
        Map<Long, NdflPersonIncomeByDate> mapNdflPersonIncome = new HashMap<Long, NdflPersonIncomeByDate>();
        List<NdflPersonIncome> ndflPersonIncomeList = ndflPersonDao.findIncomesByPeriodAndDeclarationDataId(declarationDataId, calendarStartDate, endDate);

        for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {
            // Учитываем только те записи, у которых заполнено либо "Дата начисления дохода", либо "Сумма налога удержанная"
            if (ndflPersonIncome.getIncomeAccruedDate() != null || ndflPersonIncome.getWithholdingTax() != null) {
                NdflPersonIncomeByDate ndflPersonIncomeByDate = mapNdflPersonIncome.get(ndflPersonIncome.getOperationId());
                if (ndflPersonIncomeByDate == null) {
                    ndflPersonIncomeByDate = new NdflPersonIncomeByDate();
                    ndflPersonIncomeByDate.setIncomePayoutSumm(ndflPersonIncome.getIncomePayoutSumm());
                    ndflPersonIncomeByDate.setWithholdingTax(ndflPersonIncome.getWithholdingTax());
                    mapNdflPersonIncome.put(ndflPersonIncome.getOperationId(), ndflPersonIncomeByDate);
                } else {
                    ndflPersonIncomeByDate.addIncomePayoutSumm(ndflPersonIncome.getIncomePayoutSumm());
                    ndflPersonIncomeByDate.addWithholdingTax(ndflPersonIncome.getWithholdingTax());
                }

                // Если заполнено поле "Дата начисления дохода", то будем учитывать это поле
                if (ndflPersonIncome.getIncomeAccruedDate() != null) {
                    if (ndflPersonIncomeByDate.getIncomeAccruedDate() == null) {
                        ndflPersonIncomeByDate.setIncomeAccruedDate(ndflPersonIncome.getIncomeAccruedDate());
                    }
                }

                // Если заполнено поле "Сумма налога удержанная", то учитываем поля "Дата удержания налога" и "Срок (дата) перечисления налога"
                if (ndflPersonIncome.getWithholdingTax() != null) {
                    if (ndflPersonIncomeByDate.getTaxDate() == null) {
                        ndflPersonIncomeByDate.setTaxDate(ndflPersonIncome.getTaxDate());
                    }
                    if (ndflPersonIncomeByDate.getTaxTransferDate() == null) {
                        ndflPersonIncomeByDate.setTaxTransferDate(ndflPersonIncome.getTaxTransferDate());
                    }
                }
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

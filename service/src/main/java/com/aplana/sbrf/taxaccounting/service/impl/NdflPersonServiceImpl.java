package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.KppSelect;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.filter.NdflFilter;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonDeductionDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonIncomeDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonPrepaymentDTO;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.NdflPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с ФЛ
 */
@Service
public class NdflPersonServiceImpl implements NdflPersonService {

    @Autowired
    private NdflPersonDao ndflPersonDao;
    @Autowired
    private RefBookFactory refBookFactory;

    @Override
    public NdflPerson findOne(long id) {
        return ndflPersonDao.findById(id);
    }

    @Override
    public PagingResult<NdflPerson> findPersonByFilter(long declarationDataId, Map<String, Object> parameters, PagingParams pagingParams) {
        return ndflPersonDao.fetchNdflPersonByParameters(declarationDataId, parameters, pagingParams);
    }

    @Override
    @PreAuthorize("hasPermission(#ndflFilter.declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).VIEW)")
    public PagingResult<NdflPerson> findPersonByFilter(NdflFilter ndflFilter, PagingParams pagingParams) {
        return ndflPersonDao.fetchNdflPersonByParameters(ndflFilter, pagingParams);
    }

    @Override
    public List<NdflPerson> findAllByDeclarationId(long declarationDataId) {
        return ndflPersonDao.findAllByDeclarationId(declarationDataId);
    }

    @Override
    public List<NdflPersonIncome> findNdflPersonIncome(long declarationDataId) {
        return ndflPersonDao.findAllIncomesByDeclarationId(declarationDataId);
    }

    @Override
    @PreAuthorize("hasPermission(#ndflFilter.declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).VIEW)")
    public PagingResult<NdflPersonIncomeDTO> findPersonIncomeByFilter(NdflFilter ndflFilter, PagingParams pagingParams) {
        return ndflPersonDao.fetchPersonIncomeByParameters(ndflFilter, pagingParams);
    }

    @Override
    @PreAuthorize("hasPermission(#ndflFilter.declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).VIEW)")
    public PagingResult<NdflPersonDeductionDTO> findPersonDeductionsByFilter(NdflFilter ndflFilter, PagingParams pagingParams) {
        return ndflPersonDao.fetchPersonDeductionByParameters(ndflFilter, pagingParams);
    }

    @Override
    @PreAuthorize("hasPermission(#ndflFilter.declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).VIEW)")
    public PagingResult<NdflPersonPrepaymentDTO> findPersonPrepaymentByFilter(NdflFilter ndflFilter, PagingParams pagingParams) {
        return ndflPersonDao.fetchPersonPrepaymentByParameters(ndflFilter, pagingParams);
    }

    @Override
    public String getPersonDocTypeName(long idDocType) {
        RefBookDataProvider provider = refBookFactory.getDataProvider(RefBook.Id.DOCUMENT_CODES.getId());
        PagingResult<Map<String, RefBookValue>> docCodes = provider.getRecords(new Date(), null,
                String.format("CODE = '%s'", idDocType), null);
        if (docCodes.size() > 1) {
            throw new ServiceException("Найдено несколько ДУЛ удовлетворяющих условиям!");
        }
        return docCodes.get(0).get("NAME").getStringValue();
    }

    @Override
    public int getNdflPersonCount(Long declarationDataId) {
        return ndflPersonDao.getNdflPersonCount(declarationDataId);
    }

    @Override
    public PagingResult<KppSelect> findAllKppByDeclarationDataId(long declarationDataId, String kpp, PagingParams pagingParams) {
        return ndflPersonDao.findAllKppByDeclarationDataId(declarationDataId, kpp, pagingParams);
    }

    @Override
    @Transactional
    public void fillNdflPersonIncomeSortFields(List<NdflPerson> ndflPersonList) {
        Map<Pair<String, String>, List<NdflPersonIncome>> incomesGroupedByOperationAndInp = new HashMap<>();
        for (NdflPerson person : ndflPersonList) {
            for (NdflPersonIncome income : person.getIncomes()) {
                Pair operationAndInpKey = new Pair(income.getOperationId(), person.getInp());
                List<NdflPersonIncome> operationAndInpGroup = incomesGroupedByOperationAndInp.get(operationAndInpKey);
                if (operationAndInpGroup == null) {
                    operationAndInpGroup = new ArrayList<>();
                }
                operationAndInpGroup.add(income);
                incomesGroupedByOperationAndInp.put(operationAndInpKey, operationAndInpGroup);

                if (income.getTaxDate() != null) {
                    income.setActionDate(income.getTaxDate());
                } else {
                    income.setActionDate(income.getPaymentDate());
                }

                if (income.getIncomeAccruedDate() != null) {
                    income.setRowType(NdflPersonIncome.ACCRUED_ROW_TYPE);
                } else if (income.getIncomePayoutDate() != null) {
                    income.setRowType(NdflPersonIncome.PAYOUT_ROW_TYPE);
                } else {
                    income.setRowType(NdflPersonIncome.OTHER_ROW_TYPE);
                }
            }
        }

        for (Map.Entry<Pair<String, String>, List<NdflPersonIncome>> entry : incomesGroupedByOperationAndInp.entrySet()) {
            List<NdflPersonIncome> group = entry.getValue();
            List<Date> incomeAccruedDates = new ArrayList<>();
            List<Date> incomePayoutDates = new ArrayList<>();
            List<Date> paymentDates = new ArrayList<>();

            for (NdflPersonIncome item : group) {
                if (item.getIncomeAccruedDate() != null) {
                    incomeAccruedDates.add(item.getIncomeAccruedDate());
                }
                if (item.getIncomePayoutDate() != null) {
                    incomePayoutDates.add(item.getIncomePayoutDate());
                }
                if (item.getPaymentDate() != null) {
                    paymentDates.add(item.getPaymentDate());
                }
            }

            for (NdflPersonIncome item : group) {
                if (!incomeAccruedDates.isEmpty()) {
                    Collections.sort(incomeAccruedDates);
                    item.setOperationDate(incomeAccruedDates.get(0));
                } else if (!incomePayoutDates.isEmpty()) {
                    Collections.sort(incomePayoutDates);
                    item.setOperationDate(incomePayoutDates.get(0));
                } else  if (!paymentDates.isEmpty()) {
                    Collections.sort(incomeAccruedDates);
                    item.setOperationDate(paymentDates.get(0));
                } else {
                    item.setOperationDate(null);
                }
            }
        }

    }
}

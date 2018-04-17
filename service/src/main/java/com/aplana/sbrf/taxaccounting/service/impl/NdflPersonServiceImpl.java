package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.filter.NdflFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonDeductionFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonIncomeFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonPrepaymentFilter;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonDeductionDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonIncomeDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonPrepaymentDTO;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.NdflPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Date;
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
        return ndflPersonDao.fetchOne(id);
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
}

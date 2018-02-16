package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.NdflFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonDeductionFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonIncomeFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonPrepaymentFilter;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
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
public class NdflPersonServiceImpl implements NdflPersonService {

    @Autowired
    private NdflPersonDao ndflPersonDao;

    @Override
    public NdflPerson findOne(long id) {
        return ndflPersonDao.fetchOne(id);
    }

    @Override
    public PagingResult<NdflPerson> findPersonByFilter(long declarationDataId, Map<String, Object> parameters, PagingParams pagingParams) {
        return ndflPersonDao.fetchNdflPersonByParameters(declarationDataId, parameters, pagingParams);
    }

    @Override
    public PagingResult<NdflPerson> findPersonByFilter(NdflFilter ndflFilter, PagingParams pagingParams) {
        return ndflPersonDao.fetchNdflPersonByParameters(ndflFilter, pagingParams);
    }

    @Override
    public PagingResult<NdflPersonIncomeDTO> findPersonIncomeByFilter(NdflFilter ndflFilter, PagingParams pagingParams) {
        return ndflPersonDao.fetchPersonIncomeByParameters(ndflFilter, pagingParams);
    }

    @Override
    public PagingResult<NdflPersonDeductionDTO> findPersonDeductionsByFilter(NdflFilter ndflFilter, PagingParams pagingParams) {
        return ndflPersonDao.fetchPersonDeductionByParameters(ndflFilter, pagingParams);
    }

    @Override
    public PagingResult<NdflPersonPrepaymentDTO> findPersonPrepaymentByFilter(NdflFilter ndflFilter, PagingParams pagingParams) {
        return ndflPersonDao.fetchPersonPrepaymentByParameters(ndflFilter, pagingParams);
    }
}

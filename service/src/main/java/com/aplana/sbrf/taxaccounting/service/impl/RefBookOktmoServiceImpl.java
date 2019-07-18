package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookOktmoDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookOktmo;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookOktmoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Реализация сервиса для работы со справочником ОКТМО
 */
@Service("refBookOktmoService")
public class RefBookOktmoServiceImpl implements RefBookOktmoService {

    @Autowired
    private RefBookOktmoDao refBookOktmoDao;

    @Override
    public PagingResult<RefBookOktmo> fetchAll(String filter, PagingParams pagingParams) {
        return refBookOktmoDao.fetchAll(filter, pagingParams);
    }

    @Override
    public RefBookOktmo fetchByCode(String code, Date version) {
        return refBookOktmoDao.fetchByCode(code, version);
    }
}
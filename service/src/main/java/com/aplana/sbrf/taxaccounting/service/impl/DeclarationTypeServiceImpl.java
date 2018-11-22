package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTypeService;
import com.aplana.sbrf.taxaccounting.service.TemplateChangesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User: avanteev
 */
@Service
@Transactional
public class DeclarationTypeServiceImpl implements DeclarationTypeService {

    @Autowired
    private DeclarationTypeDao declarationTypeDao;

    @Override
    @Transactional
    public int save(DeclarationType type) {
        return declarationTypeDao.save(type);
    }

    @Override
    public DeclarationType get(int typeId) {
        return declarationTypeDao.get(typeId);
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_ADMINISTRATION_SETTINGS)")
    public List<DeclarationType> fetchAll(TAUserInfo userInfo) {
        return declarationTypeDao.fetchAll();
    }
}
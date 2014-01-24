package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.service.DeclarationTypeService;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Transactional(readOnly = false)
    public int save(DeclarationType type) {
        return declarationTypeDao.save(type);
    }

    @Override
    public DeclarationType get(int typeId) {
        return declarationTypeDao.get(typeId);
    }

    @Override
    public void delete(int typeId) {
        declarationTypeDao.delete(typeId);
    }

    @Override
    public List<DeclarationType> listAll() {
        return declarationTypeDao.listAll();
    }
}

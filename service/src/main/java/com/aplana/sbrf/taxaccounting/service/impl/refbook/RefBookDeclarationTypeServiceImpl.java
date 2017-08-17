package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeclarationType;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDeclarationTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by aokunev on 10.08.2017.
 */
@Service
@Transactional
public class RefBookDeclarationTypeServiceImpl implements RefBookDeclarationTypeService {
    private RefBookDeclarationTypeDao refBookDeclarationTypeDao;

    public RefBookDeclarationTypeServiceImpl(RefBookDeclarationTypeDao refBookDeclarationTypeDao) {
        this.refBookDeclarationTypeDao = refBookDeclarationTypeDao;
    }

    @Override
    public List<RefBookDeclarationType> fetchAllDeclarationTypes() {
        return refBookDeclarationTypeDao.fetchAll();
    }
}

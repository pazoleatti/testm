package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.TemplateChangesDao;
import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import com.aplana.sbrf.taxaccounting.service.TemplateChangesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User: avanteev
 */
@Service
@Transactional
public class TemplateChangesServiceImpl implements TemplateChangesService {

    @Autowired
    private TemplateChangesDao templateChangesDao;

    @Override
    @Transactional(readOnly = false)
    public int save(TemplateChanges templateChanges) {
        return templateChangesDao.add(templateChanges);
    }

    @Override
    public List<TemplateChanges> getByFormTemplateId(int formTemplateId) {
        return templateChangesDao.getByFormTemplateId(formTemplateId);
    }

    @Override
    public List<TemplateChanges> getByDeclarationTemplateId(int declarationTemplateId) {
        return templateChangesDao.getByDeclarationTemplateId(declarationTemplateId);
    }
}

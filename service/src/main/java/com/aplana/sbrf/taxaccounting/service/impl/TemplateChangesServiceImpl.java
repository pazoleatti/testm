package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.TemplateChangesDao;
import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import com.aplana.sbrf.taxaccounting.model.VersionHistorySearchOrdering;
import com.aplana.sbrf.taxaccounting.service.TemplateChangesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
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
    public List<TemplateChanges> getByFormTemplateId(int formTemplateId, VersionHistorySearchOrdering ordering, boolean isAscSorting) {
        return templateChangesDao.getByFormTemplateId(formTemplateId, ordering, isAscSorting);
    }

    @Override
    public List<TemplateChanges> getByDeclarationTemplateId(int declarationTemplateId, VersionHistorySearchOrdering ordering, boolean isAscSorting) {
        return templateChangesDao.getByDeclarationTemplateId(declarationTemplateId, ordering, isAscSorting);
    }

    @Override
    public List<TemplateChanges> getByFormTypeIds(int ftTypeId, VersionHistorySearchOrdering ordering, boolean isAscSorting) {
        return templateChangesDao.getByFormTypeIds(ftTypeId, ordering, isAscSorting);
    }

    @Override
    public List<TemplateChanges> getByDeclarationTypeIds(int dtTypeId, VersionHistorySearchOrdering ordering, boolean isAscSorting) {
        return templateChangesDao.getByDeclarationTypeId(dtTypeId, ordering, isAscSorting);
    }

    @Override
    public List<TemplateChanges> getByRefBookIds(int refBookId, VersionHistorySearchOrdering ordering, boolean isAscSorting) {
        return templateChangesDao.getByRefBookId(refBookId, ordering, isAscSorting);
    }

    @Override
    public void delete(Collection<Integer> ids) {
        templateChangesDao.delete(ids);
    }

    @Override
    public void deleteByTemplateIds(Collection<Integer> ftIds, Collection<Integer> dtIds) {
        List<Integer> changeses = templateChangesDao.getIdsByTemplateIds(ftIds, dtIds, VersionHistorySearchOrdering.DATE, true);
        if (changeses.isEmpty())
            return;
        templateChangesDao.delete(changeses);
    }
}

package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.TemplateChangesDao;
import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * User: avanteev
 */
@Repository
public class TemplateChangesDaoImpl implements TemplateChangesDao {
    @Override
    public void add(TemplateChanges templateChanges) {

    }

    @Override
    public List<TemplateChanges> getByFormTemplateId(int ftId) {
        return null;
    }

    @Override
    public List<TemplateChanges> getByDeclarationTemplateId(int dtId) {
        return null;
    }
}

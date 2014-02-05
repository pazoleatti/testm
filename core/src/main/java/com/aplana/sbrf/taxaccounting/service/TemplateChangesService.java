package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TemplateChanges;

import java.util.List;

/**
 * User: avanteev
 * Севвис для получения истории изменений версии
 */
public interface TemplateChangesService {
    int save(TemplateChanges templateChanges);
    List<TemplateChanges> getByFormTemplateId(int formTemplateId);
    List<TemplateChanges> getByDeclarationTemplateId(int declarationTemplateId);
}

package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import com.aplana.sbrf.taxaccounting.model.VersionHistorySearchOrdering;

import java.util.Collection;
import java.util.List;

/**
 * User: avanteev
 * Сервис для получения истории изменений версии
 */
public interface TemplateChangesService {
    int save(TemplateChanges templateChanges);
    List<TemplateChanges> getByFormTemplateId(int formTemplateId, VersionHistorySearchOrdering ordering, boolean isAscSorting);
    List<TemplateChanges> getByDeclarationTemplateId(int declarationTemplateId, VersionHistorySearchOrdering ordering, boolean isAscSorting);
    List<TemplateChanges> getByFormTypeIds(int ftTypeId, VersionHistorySearchOrdering searchOrdering, boolean isAscSorting);
    List<TemplateChanges> getByDeclarationTypeIds(int dtTypeId, VersionHistorySearchOrdering ordering, boolean isAscSorting);
    void delete(Collection<Integer> ids);
}

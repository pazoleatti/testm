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

    /**
     * Не использовать для получения изменений для удаления, т.к. все версии шаблонов могут быть на тот момент уже все удалены.
     * @param ftTypeId
     * @param searchOrdering
     * @param isAscSorting
     * @return
     */
    List<TemplateChanges> getByFormTypeIds(int ftTypeId, VersionHistorySearchOrdering searchOrdering, boolean isAscSorting);
    List<TemplateChanges> getByDeclarationTypeIds(int dtTypeId, VersionHistorySearchOrdering ordering, boolean isAscSorting);
    List<TemplateChanges> getByRefBookIds(int refBookId, VersionHistorySearchOrdering searchOrdering, boolean isAscSorting);
    void delete(Collection<Integer> ids);

    /**
     *
     * @param ftIds список версий макетов НФ
     * @param dtIds список версий деклараций
     */
    void deleteByTemplateIds(Collection<Integer> ftIds, Collection<Integer> dtIds);
}

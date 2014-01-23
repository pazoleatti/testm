package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.TemplateChanges;

import java.util.List;

/**
 * User: avanteev
 * Получение событий изменений версий макета.
 */
public interface TemplateChangesDao {
    /**
     *
     * @param templateChanges изменение версии макета
     */
    int add(TemplateChanges templateChanges);

    /**
     * Получает список изменений версии НФ макета
     * @param ftId идентификатор версии НФ
     * @return списрк изменений версии макета НФ
     */
    List<TemplateChanges> getByFormTemplateId(int ftId);

    /**
     * Получает список изменений версии декларации макета
     * @param dtId идентификатор версии декларации
     * @return списрк изменений версии макета декаларации
     */
    List<TemplateChanges> getByDeclarationTemplateId(int dtId);
}

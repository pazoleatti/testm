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

    /**
     * Получает список изменений макета НФ.
     * @param ftTypeId идентификатор макета НФ
     * @return список изменений
     */
    List<TemplateChanges> getByFormTypeIds(int ftTypeId);

    /**
     * Получает список изменений макета деклараций.
     * @param dtTypeId дентификатор макета деклараций
     * @return список изменений
     */
    List<TemplateChanges> getByDeclarationTypeId(int dtTypeId);
}

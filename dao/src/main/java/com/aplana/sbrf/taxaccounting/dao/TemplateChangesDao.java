package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import com.aplana.sbrf.taxaccounting.model.VersionHistorySearchOrdering;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;

/**
 * User: avanteev
 * Получение событий изменений версий макета.
 */
public interface TemplateChangesDao {
    /**
     * @param templateChanges изменение версии макета
     */
    int add(TemplateChanges templateChanges);

    /**
     * Получает список изменений версии НФ макета
     *
     * @param ftId         идентификатор версии НФ
     * @param ordering     столбец, по которому сортировать
     * @param isAscSorting сорировать по возрастанию или убыванию
     * @return списрк изменений версии макета НФ
     */
    List<TemplateChanges> getByFormTemplateId(int ftId, VersionHistorySearchOrdering ordering, boolean isAscSorting);

    /**
     * Получает список изменений версии декларации макета
     *
     * @param dtId         идентификатор версии декларации
     * @param ordering     столбец, по которому сортировать
     * @param isAscSorting сорировать по возрастанию или убыванию
     * @return списрк изменений версии макета декаларации
     */
    List<TemplateChanges> getByDeclarationTemplateId(int dtId, VersionHistorySearchOrdering ordering, boolean isAscSorting);

    /**
     * Получает список изменений макета НФ.
     *
     * @param ftTypeId     идентификатор макета НФ
     * @param ordering     столбец, по которому сортировать
     * @param isAscSorting сорировать по возрастанию или убыванию
     * @return список изменений
     */
    List<TemplateChanges> getByFormTypeIds(int ftTypeId, VersionHistorySearchOrdering ordering, boolean isAscSorting);

    /**
     * Получает список изменений макета деклараций.
     *
     * @param dtTypeId     дентификатор макета деклараций
     * @param ordering     столбец, по которому сортировать
     * @param isAscSorting сорировать по возрастанию или убыванию
     * @return список изменений
     */
    List<TemplateChanges> getByDeclarationTypeId(int dtTypeId, VersionHistorySearchOrdering ordering, boolean isAscSorting);

    /**
     * Удаляет все записи журнала изменений макета. Только в случае удаления всего макета.
     *
     * @param ids идентификаторы
     */
    void delete(@NotNull Collection<Integer> ids);
}

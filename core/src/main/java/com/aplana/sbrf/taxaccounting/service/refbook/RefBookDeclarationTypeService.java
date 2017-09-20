package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeclarationType;

import java.util.List;

/**
 * Сервис для работы со справочником Виды форм
 */
public interface RefBookDeclarationTypeService {
    /**
     * Получение всех значений справочника
     *
     * @return Список значений справочника
     */
    List<RefBookDeclarationType> fetchAllDeclarationTypes();

    /**
     * Получение значений справочника для создания налоговой формы
     *
     * @param declarationKind     Вид налоговой формы
     * @param departmentId Подразделение
     * @param periodId ID отчетного периода
     * @return Список значений справочника
     */
    List<RefBookDeclarationType> fetchDeclarationTypesForCreate(Long declarationKind, Integer departmentId, Integer periodId);
}

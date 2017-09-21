package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeclarationType;

import java.util.Date;
import java.util.List;

/**
 * Дао для работы со справочником Виды форм
 */
public interface RefBookDeclarationTypeDao {
    /**
     * Получение всех значений справочника
     *
     * @return Список значений справочника
     */
    List<RefBookDeclarationType> fetchAll();

    /**
     * Получение значений справочника для создания налоговой формы
     *
     * @param declarationKind Вид налоговой формы
     * @param departmentId    ID подразделения
     * @param periodStartDate Начало отчетного периода
     * @return Список значений справочника
     */
    List<RefBookDeclarationType> fetchDeclarationTypesForCreate(Long declarationKind, Integer departmentId, Date periodStartDate);
}

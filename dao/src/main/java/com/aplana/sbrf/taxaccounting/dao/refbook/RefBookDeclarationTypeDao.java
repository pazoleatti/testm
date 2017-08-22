package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeclarationType;

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
}

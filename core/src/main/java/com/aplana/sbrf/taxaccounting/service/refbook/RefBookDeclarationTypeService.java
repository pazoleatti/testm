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
}

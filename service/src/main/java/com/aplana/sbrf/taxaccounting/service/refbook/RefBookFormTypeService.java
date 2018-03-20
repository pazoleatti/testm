package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookFormType;

import java.util.List;

/**
 * Сервис для работы со справочником Виды налоговых форм (ид=931)
 */
public interface RefBookFormTypeService {

    /**
     * Возвращяет все записи справочника
     *
     * @return список записей справочника RefBookFormType
     */
    List<RefBookFormType> fetchAll();
}

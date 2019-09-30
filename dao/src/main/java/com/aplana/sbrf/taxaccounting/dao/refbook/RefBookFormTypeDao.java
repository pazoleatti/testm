package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookFormType;

import java.util.List;

/**
 * Dao для работы со справочником Виды налоговых форм (ид=931)
 */
public interface RefBookFormTypeDao {

    /**
     * Возвращяет все записи справочника
     *
     * @return список записей справочника RefBookFormType
     */
    List<RefBookFormType> fetchAll();
}

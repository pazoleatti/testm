package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeclarationType;

import java.util.List;

/**
 * Дао для работы со справочником АСНУ
 */
public interface RefBookDeclarationTypeDao {

    List<RefBookDeclarationType> fetchAll();
}

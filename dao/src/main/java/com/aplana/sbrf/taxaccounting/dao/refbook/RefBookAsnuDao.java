package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;

import java.util.List;

/**
 * Дао для работы со справочником АСНУ
 */
public interface RefBookAsnuDao {

    List<RefBookAsnu> fetchAll();
}

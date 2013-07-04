package com.aplana.sbrf.taxaccounting.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

/**
 * Дао для версионных справочников
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 04.07.13 12:25
 */

public interface RefBookDao {

	List<Map<String, RefBookValue>> getData(Long refBookId, Date version);

}

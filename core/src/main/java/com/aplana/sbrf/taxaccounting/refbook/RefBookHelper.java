package com.aplana.sbrf.taxaccounting.refbook;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * User: avanteev
 */
public interface RefBookHelper {

	void dataRowsDereference(Collection<DataRow<Cell>> dataRows,
			List<Column> columns);

	Map<String, String> singleRecordDereference(RefBook refBook, RefBookDataProvider provider,
			List<RefBookAttribute> attributes, Map<String, RefBookValue> record);
}

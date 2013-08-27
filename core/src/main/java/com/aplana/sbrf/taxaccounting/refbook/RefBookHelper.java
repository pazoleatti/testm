package com.aplana.sbrf.taxaccounting.refbook;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.Collection;
import java.util.List;

/**
 * User: avanteev
 */
public interface RefBookHelper {

    public void refBookDereference(Collection<DataRow<Cell>> dataRows,
                                   List<Column> columns);
}

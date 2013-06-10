package com.aplana.sbrf.taxaccounting.script.support.api.datarow;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowFilter;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;

public interface DataRowScriptService {
	
	List<DataRow<Cell>> getList();
	
	List<DataRow<Cell>> getList(DataRowRange range);
	
	List<DataRow<Cell>> getList(DataRowFilter filter);
	
	List<DataRow<Cell>> getList(DataRowFilter filter, DataRowRange range);
	
	void updateRow(DataRow<Cell> row);

}

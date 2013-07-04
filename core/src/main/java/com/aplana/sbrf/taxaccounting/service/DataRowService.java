package com.aplana.sbrf.taxaccounting.service;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;

public interface DataRowService {
	
	PaginatedSearchResult<DataRow<Cell>> getDataRows(TAUserInfo userInfo, long formDataId, DataRowRange range, boolean saved);
	
	int getRowCount(TAUserInfo userInfo, long formDataId, boolean saved); 
	
	void update(TAUserInfo userInfo, long formDataId, List<DataRow<Cell>> dataRows);

}

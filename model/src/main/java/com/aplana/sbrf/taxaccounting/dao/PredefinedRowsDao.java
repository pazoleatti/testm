package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.Form;

public interface PredefinedRowsDao {
	List<DataRow> getPredefinedRows(Form form);
	void savePredefinedRows(Form form, List<DataRow> predefinedRows);
}

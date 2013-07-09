package com.aplana.sbrf.taxaccounting.service.script.api;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

public interface DataRowService {
	
	List<DataRow<Cell>> getAllSaved();
	int getSavedCount();
	
	List<DataRow<Cell>> getAll();
	int getCount();
	
	void insert(DataRow<Cell> dataRow, int index);
	
	void insert(List<DataRow<Cell>> dataRows, int index);
	
	void update(DataRow<Cell> dataRow);
	
	void update(List<DataRow<Cell>> dataRows);
	
	void delete(DataRow<Cell> dataRow);
	
	void delete(List<DataRow<Cell>> dataRows);
	
	void save();
	
	void cancel();

}

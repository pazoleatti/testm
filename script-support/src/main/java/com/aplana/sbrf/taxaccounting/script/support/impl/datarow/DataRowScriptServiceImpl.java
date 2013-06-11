package com.aplana.sbrf.taxaccounting.script.support.impl.datarow;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowFilter;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.script.support.api.datarow.DataRowScriptService;
import com.aplana.sbrf.taxaccounting.service.script.ScriptComponentContext;
import com.aplana.sbrf.taxaccounting.service.script.ScriptComponentContextHolder;

public class DataRowScriptServiceImpl implements DataRowScriptService, ScriptComponentContextHolder{
	
	private ScriptComponentContext context;
	
	private FormData formData;
	
	@Autowired
	private DataRowDao dao;

	@Override
	public List<DataRow<Cell>> getList() {
		return getList(null, null);
	}

	@Override
	public List<DataRow<Cell>> getList(DataRowRange range) {
		return getList(null, range);
	}

	@Override
	public List<DataRow<Cell>> getList(DataRowFilter filter) {
		// TODO Auto-generated method stub
		return getList(filter, null);
	}

	@Override
	public List<DataRow<Cell>> getList(DataRowFilter filter, DataRowRange range) {
		return new DataRowList(dao, formData, range, filter, context.getLogger());
	}

	@Override
	public void setScriptComponentContext(ScriptComponentContext context) {
		this.context = context;
	}

	@Override
	public void updateRow(DataRow<Cell> row) {
		dao.updateRow(formData, row);
	}

}

package com.aplana.sbrf.taxaccounting.service.script.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.service.script.ScriptComponentContext;
import com.aplana.sbrf.taxaccounting.service.script.ScriptComponentContextHolder;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowService;


@Component
@Scope(value="prototype")
public class DataRowServiceImpl implements DataRowService, ScriptComponentContextHolder{
	
	private FormData fd;
	
	private ScriptComponentContext context;
	
	@Autowired
	private DataRowDao dataRowDao;

	public FormData getFormData() {
		return fd;
	}

	public void setFormData(FormData formData) {
		this.fd = formData;
	}

	@Override
	public void setScriptComponentContext(ScriptComponentContext context) {
		this.setScriptComponentContext(context);
	}

	@Override
	public List<DataRow<Cell>> getAllSaved() {
		return dataRowDao.getSavedRows(fd, null, null);
	}

	@Override
	public int getSavedCount() {
		return dataRowDao.getSavedSize(fd, null);
	}

	@Override
	public List<DataRow<Cell>> getAll() {
		return dataRowDao.getRows(fd, null, null);
	}

	@Override
	public int getCount() {
		return dataRowDao.getSize(fd, null);
	}

	@Override
	public void insert(DataRow<Cell> dataRow, int index) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insert(List<DataRow<Cell>> dataRows, int index) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(DataRow<Cell> dataRow) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(List<DataRow<Cell>> dataRows) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(DataRow<Cell> dataRow) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(List<DataRow<Cell>> dataRows) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void save() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		
	}

}

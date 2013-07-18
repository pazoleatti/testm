package com.aplana.sbrf.taxaccounting.service.script.impl;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import com.aplana.sbrf.taxaccounting.service.script.ScriptComponentContext;
import com.aplana.sbrf.taxaccounting.service.script.ScriptComponentContextHolder;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;


/**
 * Компонент позволяет из скриптов работать с данными НФ
 * Компонент не защищен.
 * 
 * @author sgoryachkin
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DataRowHelperImpl implements DataRowHelper, ScriptComponentContextHolder{
	
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
		this.context = context;
	}

	@Override
	public List<DataRow<Cell>> getAllSaved() {
		List<DataRow<Cell>> rows = dataRowDao.getSavedRows(fd, null, null);
		FormDataUtils.setValueOners(rows);
		return  rows;
	}

	@Override
	public int getSavedCount() {
		return dataRowDao.getSavedSize(fd, null);
	}

	@Override
	public List<DataRow<Cell>> getAll() {
		List<DataRow<Cell>> rows = dataRowDao.getRows(fd, null, null);
		FormDataUtils.setValueOners(rows);
		return rows;
	}

	@Override
	public int getCount() {
		return dataRowDao.getSize(fd, null);
	}

	@Override
	public void insert(DataRow<Cell> dataRow, int index) {
		@SuppressWarnings("unchecked")
		List<DataRow<Cell>> asList = Arrays.asList(dataRow);
		dataRowDao.insertRows(fd, index, asList);
		
	}

	@Override
	public void insert(List<DataRow<Cell>> dataRows, int index) {
		dataRowDao.insertRows(fd, index, dataRows);
	}

	@Override
	public void update(DataRow<Cell> dataRow) {
		@SuppressWarnings("unchecked")
		List<DataRow<Cell>> asList = Arrays.asList(dataRow);
		dataRowDao.updateRows(fd, asList);
	}

	@Override
	public void update(List<DataRow<Cell>> dataRows) {
		dataRowDao.updateRows(fd, dataRows);
	}

	@Override
	public void delete(DataRow<Cell> dataRow) {
		@SuppressWarnings("unchecked")
		List<DataRow<Cell>> asList = Arrays.asList(dataRow);
		dataRowDao.removeRows(fd, asList);
	}

	@Override
	public void delete(List<DataRow<Cell>> dataRows) {
		dataRowDao.removeRows(fd, dataRows);
	}

	@Override
	public void commit() {
		dataRowDao.commit(fd.getId());
	}

	@Override
	public void rollback() {
		dataRowDao.rollback(fd.getId());
	}

	@Override
	public void save(List<DataRow<Cell>> dataRows) {
		dataRowDao.saveRows(fd, dataRows);
		
	}

}

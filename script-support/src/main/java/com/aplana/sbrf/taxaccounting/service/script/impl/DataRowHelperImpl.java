package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContext;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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

    private List<DataRow<Cell>> dataRows;
	
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
        getAllCached().remove(dataRow);
    }

	@Override
	public void delete(List<DataRow<Cell>> dataRows) {
		dataRowDao.removeRows(fd, dataRows);
        getAllCached().remove(dataRows);
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

    @Override
    public DataRow getDataRow(List<DataRow<Cell>> dataRows, String rowAlias) {
        if (rowAlias == null) {
            throw new NullPointerException("Row alias cannot be null");
        }
        for (DataRow<Cell> row : dataRows) {
            if (rowAlias.equals(row.getAlias())) {
                return row;
            }
        }
        throw new IllegalArgumentException("Wrong row alias requested: "
                + rowAlias);
    }

    @Override
    public List<DataRow<Cell>> getAllCached() {
        if (dataRows == null){
            dataRows = getAll();
        }

        return dataRows;
    }

   @Override
    public int getDataRowIndex(List<DataRow<Cell>> dataRows, String rowAlias) {
        if (rowAlias == null) {
            throw new NullPointerException("Row alias cannot be null");
        }

        for (int index = 0; index < dataRows.size(); ++index) {
            DataRow<Cell> row = dataRows.get(index);
            if (rowAlias.equals(row.getAlias())) {
                return index;
            }
        }
        throw new IllegalArgumentException("Wrong row alias requested: "
                + rowAlias);
    }

    @Override
    public void clear(){
        save(new ArrayList<DataRow<Cell>>());
    }
}

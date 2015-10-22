package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
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
public class DataRowHelperImpl implements DataRowHelper, ScriptComponentContextHolder {
	
	private FormData fd;

    private List<DataRow<Cell>> dataRows;

	@Autowired
	private DataRowDao dataRowDao;

    @Autowired
    private FormDataDao formDataDao;

	public FormData getFormData() {
		return fd;
	}

	public void setFormData(FormData formData) {
		this.fd = formData;
	}

	@Override
	public void setScriptComponentContext(ScriptComponentContext context) {}

	@Override
	public List<DataRow<Cell>> getAllSaved() {
		List<DataRow<Cell>> rows = dataRowDao.getRows(fd, null);
		FormDataUtils.setValueOwners(rows);
		return rows;
	}

	@Override
	public int getSavedCount() {
		return dataRowDao.getRowCount(fd);
	}

	@Override
	public List<DataRow<Cell>> getAll() {
        List<DataRow<Cell>> rows;
        rows = dataRowDao.getRows(fd, null);
		FormDataUtils.setValueOwners(rows);
		return rows;
	}

	@Override
	public int getCount() {
		return dataRowDao.getRowCount(fd);
	}

    /**
     *
     * @param dataRow
     * @param index от единицы
     */
    @Override
	public void insert(DataRow<Cell> dataRow, int index) {
		@SuppressWarnings("unchecked")
		List<DataRow<Cell>> asList = Arrays.asList(dataRow);
        getAllCached().add(index-1, dataRow);
		dataRowDao.insertRows(fd, index, asList);
	}

    /**
     *
     * @param dataRows
     * @param index от единицы
     */
    @Override
	public void insert(List<DataRow<Cell>> dataRows, int index) {
        getAllCached().addAll(index-1, dataRows);
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
        FormDataUtils.cleanValueOwners(dataRows);
        dataRowDao.updateRows(fd, dataRows);
        FormDataUtils.setValueOwners(dataRows);
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
        getAllCached().removeAll(dataRows);
	}

	@Override
	public void commit() {
		dataRowDao.removeCheckPoint(fd);
	}

	@Override
	public void rollback() {
		dataRowDao.restoreCheckPoint(fd);
	}

	@Override
	public void save(List<DataRow<Cell>> dataRows) {
        dataRowDao.removeRows(fd);
        this.dataRows = new ArrayList<DataRow<Cell>>();

        updateIndexes(dataRows);
        FormDataUtils.cleanValueOwners(dataRows);

		// сохранение строк порциями
		List<DataRow<Cell>> buffer = new ArrayList<DataRow<Cell>>();
		for(int i=0; i<dataRows.size(); i++) {
			buffer.add(dataRows.get(i));
			if (buffer.size() == DataRowHelper.INSERT_LIMIT) {
				insert(buffer, getAllCached().size() + 1);
				buffer.clear();
			}
			// обрабатываем окончание строк
			if (i == dataRows.size() - 1 && !buffer.isEmpty()) {
				insert(buffer, getAllCached().size() + 1);
			}
		}

        FormDataUtils.setValueOwners(dataRows);
        this.dataRows = dataRows;
    }

    void updateIndexes(List<DataRow<Cell>> dataRows) {
        Integer index = 1;
        for(DataRow<Cell> row: dataRows) {
            row.setIndex(index++);
        }
    }

    @Override
    public DataRow getDataRow(List<DataRow<Cell>> dataRows, String rowAlias) {
        if (rowAlias == null) {
            throw new IllegalArgumentException("Row alias cannot be null");
        }
        for (DataRow<Cell> row : dataRows) {
            if (rowAlias.equals(row.getAlias())) {
                return row;
            }
        }
        throw new IllegalArgumentException("Wrong row alias requested: " + rowAlias);
    }

    @Override
    public List<DataRow<Cell>> getAllCached() {
        if (dataRows == null){
            dataRows = getAll();
        }
        return dataRows;
    }

    @Override
    public void setAllCached(List<DataRow<Cell>> dataRows) {
        this.dataRows = new ArrayList<DataRow<Cell>>();
        getAllCached().addAll(dataRows);
    }

    @Override
    public int getDataRowIndex(List<DataRow<Cell>> dataRows, String rowAlias) {
        if (rowAlias == null) {
            throw new IllegalArgumentException("Row alias cannot be null");
        }

        for (int index = 0; index < dataRows.size(); ++index) {
            DataRow<Cell> row = dataRows.get(index);
            if (rowAlias.equals(row.getAlias())) {
                return index;
            }
        }
        throw new IllegalArgumentException("Wrong row alias requested: " + rowAlias);
    }

    /**
     * Сброс кэша
     */
    @Override
    public void dropCache(){
        dataRows = null;
    }

    @Override
    public void clear(){
        dataRows = new ArrayList<DataRow<Cell>>();
        save(dataRows);
    }

    @Override
    public void saveSort() {
        dataRowDao.reorderRows(fd, dataRows);
        formDataDao.updateSorted(fd.getId(), true);
    }
}

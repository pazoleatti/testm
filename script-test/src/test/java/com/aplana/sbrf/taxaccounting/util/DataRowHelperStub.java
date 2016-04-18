package com.aplana.sbrf.taxaccounting.util;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;

import java.util.LinkedList;
import java.util.List;

/**
 * Реализация хэлпера с сохранением в списке
 *
 * @author Levykin
 */
public class DataRowHelperStub implements DataRowHelper {
    private List<DataRow<Cell>> rowList = new LinkedList<DataRow<Cell>>();

    @Override
    public List<DataRow<Cell>> getAllSaved() {
        return rowList;
    }

    @Override
    public int getSavedCount() {
        return rowList.size();
    }

    @Override
    public List<DataRow<Cell>> getAll() {
        return rowList;
    }

    @Override
    public int getCount() {
        return rowList.size();
    }

    @Override
    public void save(List<DataRow<Cell>> dataRows) {
        rowList = new LinkedList(dataRows);
        updateIndexes();
    }

    @Override
    public void insert(DataRow<Cell> dataRow, int index) {
        rowList.add(index - 1, dataRow);
        updateIndexes();
    }

    @Override
    public void insert(List<DataRow<Cell>> dataRows, int index) {
        rowList.addAll(index - 1, dataRows);
        updateIndexes();
    }

    @Override
    public void update(DataRow<Cell> dataRow) {
        // Не требуется
    }

    @Override
    public void update(List<DataRow<Cell>> dataRows) {
        // Не требуется
    }

    @Override
    public void delete(DataRow<Cell> dataRow) {
        if (dataRow != null) {
            rowList.remove(dataRow);
            updateIndexes();
        }
    }

    @Override
    public void delete(List<DataRow<Cell>> dataRows) {
        if (dataRows != null) {
            rowList.removeAll(dataRows);
            updateIndexes();
        }
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
        throw new IllegalArgumentException("Wrong row alias requested: " + rowAlias);
    }

    @Override
    public List<DataRow<Cell>> getAllCached() {
        return rowList;
    }

    @Override
    public void setAllCached(List<DataRow<Cell>> dataRows) {
        rowList = dataRows;
    }

    @Override
    public void dropCache() {
        // Не требуется
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
        throw new IllegalArgumentException("Wrong row alias requested: " + rowAlias);
    }

    @Override
    public void clear() {
        rowList.clear();
    }

    @Override
    public void saveSort() {
        // Не требуется
    }

    /**
     * Пересчет индексов строк
     */
    private void updateIndexes() {
        int counter = 0;
        for (DataRow<Cell> row : rowList) {
            row.setIndex(++counter);
        }
    }
}

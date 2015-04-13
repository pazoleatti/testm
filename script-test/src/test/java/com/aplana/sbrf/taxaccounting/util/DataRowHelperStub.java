package com.aplana.sbrf.taxaccounting.util;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;

import java.util.ArrayList;
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
        if (!dataRows.isEmpty()) {
            rowList = getCloneRows(dataRows);
        } else {
            rowList = new ArrayList<DataRow<Cell>>();
        }
        updateIndexes();
    }

    /** Получить копию строки. */
    private List<DataRow<Cell>> getCloneRows(List<DataRow<Cell>> dataRows) {
        // клонировать список
        List<DataRow<Cell>> clone = new ArrayList<DataRow<Cell>>(dataRows.size());
        // получить список ячеек по первой строке
        List<Cell> cells = new ArrayList<Cell>();
        List<FormStyle> formStyleList = new ArrayList<FormStyle>();
        for (DataRow<Cell> row : dataRows) {
            for (String key : row.keySet()) {
                formStyleList.add(row.getCell(key).getStyle());
            }
        }
        // сделать копии строк
        for (DataRow<Cell> row : dataRows) {
            cells.clear();
            for (String key : row.keySet()) {
                Cell cell = new Cell(row.getCell(key).getColumn(),  formStyleList);
                cells.add(cell);
            }
            DataRow<Cell> newRow = new DataRow<Cell>(row.getAlias(), cells);

            newRow.setAlias(row.getAlias());
            newRow.setIndex(row.getIndex());
            newRow.setId(row.getId());
            newRow.setImportIndex(row.getImportIndex());
            for (String alias : row.keySet()) {
                newRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
                newRow.getCell(alias).setEditable(row.getCell(alias).isEditable());
                newRow.getCell(alias).setColSpan(row.getCell(alias).getColSpan());
                newRow.getCell(alias).setRowSpan(row.getCell(alias).getRowSpan());
            }
            clone.add(newRow);
        }
        return clone;
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
    public void commit() {
        // Не требуется
    }

    @Override
    public void rollback() {
        // Не требуется
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

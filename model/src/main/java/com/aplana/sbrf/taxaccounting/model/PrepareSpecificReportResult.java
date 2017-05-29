package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.List;

/**
 * Объект-хранилище данных для вывода предварительных результатов формировая спец. отчета
 *
 * @author lhaziev
 */
public class PrepareSpecificReportResult implements Serializable{

    /**
     * Столбцы результатов
     */
    List<Column> tableColumns;

    /**
     * Список результатов
     */
    List<DataRow<Cell>> dataRows;

    private int countAvailableDataRows;

    public List<Column> getTableColumns() {
        return tableColumns;
    }

    public void setTableColumns(List<Column> tableColumns) {
        this.tableColumns = tableColumns;
    }

    public List<DataRow<Cell>> getDataRows() {
        return dataRows;
    }

    public void setDataRows(List<DataRow<Cell>> dataRows) {
        this.dataRows = dataRows;
    }

    public int getCountAvailableDataRows() {
        return countAvailableDataRows;
    }

    public void setCountAvailableDataRows(int countAvailableDataRows) {
        this.countAvailableDataRows = countAvailableDataRows;
    }
}

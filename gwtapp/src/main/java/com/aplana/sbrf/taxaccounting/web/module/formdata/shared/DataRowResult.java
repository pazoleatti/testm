package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.gwtplatform.dispatch.shared.Result;
/**
 * Результат выполнения действий, которые моифицируют форму каким-либо образом.
 *
 * @author Eugene Stetsenko
 */
public class DataRowResult implements Result {
	private static final long serialVersionUID = -4686362790466910194L;

    private String uuid;
    private DataRow<Cell> currentRow;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public DataRow<Cell> getCurrentRow() {
        return currentRow;
    }

    public void setCurrentRow(DataRow<Cell> currentRow) {
        this.currentRow = currentRow;
    }
}
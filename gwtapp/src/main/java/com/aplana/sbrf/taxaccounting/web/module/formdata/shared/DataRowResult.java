package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.gwtplatform.dispatch.shared.Result;
/**
 * Результат выполнения действий, которые моифицируют форму каким-либо образом.
 *
 * @author Eugene Stetsenko
 */
public class DataRowResult implements Result {
	private static final long serialVersionUID = -4686362790466910194L;
	
	private List<LogEntry> logEntries;
	private DataRow<Cell> currentRow;

	public List<LogEntry> getLogEntries() {
		return logEntries;
	}

	public void setLogEntries(List<LogEntry> logEntries) {
		this.logEntries = logEntries;
	}

	public DataRow<Cell> getCurrentRow() {
		return currentRow;
	}

	public void setCurrentRow(DataRow<Cell> currentRow) {
		this.currentRow = currentRow;
	}
	
}
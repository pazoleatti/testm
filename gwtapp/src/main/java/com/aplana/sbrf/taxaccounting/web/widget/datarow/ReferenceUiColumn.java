package com.aplana.sbrf.taxaccounting.web.widget.datarow;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.ReferenceColumn;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ColumnContext;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ReferenceCell;

/**
 * @author Dmitriy Levykin
 */
public class ReferenceUiColumn extends DataRowColumn<Object> {

	public ReferenceUiColumn(ReferenceColumn column, ColumnContext columnContext) {
		super(new ReferenceCell(columnContext), column);
		this.setHorizontalAlignment(ALIGN_RIGHT);
	}

	@Override
	public Object getValue(DataRow<Cell> dataRow) {
		return dataRow.get(alias);
	}
}

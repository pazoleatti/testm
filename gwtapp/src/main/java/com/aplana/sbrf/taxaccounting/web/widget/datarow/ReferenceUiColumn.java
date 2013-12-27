package com.aplana.sbrf.taxaccounting.web.widget.datarow;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.ReferenceColumn;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ColumnContext;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ReferenceCell;
import com.google.gwt.cell.client.FieldUpdater;

/**
 * @author Dmitriy Levykin
 */
public class ReferenceUiColumn extends DataRowColumn<Object> {

	public ReferenceUiColumn(ReferenceColumn column, ColumnContext columnContext) {
		super(new ReferenceCell(columnContext), column);
		this.setHorizontalAlignment(ALIGN_RIGHT);
		this.setFieldUpdater(new FieldUpdater<DataRow<Cell>, Object>() {
			@Override
			public void update(int index, DataRow<Cell> dataRow, Object value) {
				dataRow.put(getAlias(), value);
			}
		});
	}

	@Override
	public Long getValue(DataRow<Cell> dataRow) {
		return (Long) dataRow.get(alias);
	}
}

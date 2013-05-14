package com.aplana.sbrf.taxaccounting.web.widget.datarow;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ColumnContext;
import com.aplana.sbrf.taxaccounting.web.widget.cell.NumericDictionaryCell;
import com.google.gwt.cell.client.FieldUpdater;

import java.math.BigDecimal;

/**
 * @author Vitalii Samolovskikh
 */
public class EditNumericDictionaryColumn extends DataRowColumn<BigDecimal> {
	public EditNumericDictionaryColumn(NumericColumn column, ColumnContext columnContext) {
		super(new NumericDictionaryCell(column.getDictionaryCode(), columnContext), column);
		this.setHorizontalAlignment(ALIGN_RIGHT);
		this.setFieldUpdater(new FieldUpdater<DataRow<Cell>, BigDecimal>() {
			@Override
			public void update(int index, DataRow<Cell> dataRow, BigDecimal value) {
				dataRow.put(getAlias(), value);
			}
		});
	}

	@Override
	public BigDecimal getValue(DataRow dataRow) {
		return (BigDecimal) dataRow.get(alias);
	}
}

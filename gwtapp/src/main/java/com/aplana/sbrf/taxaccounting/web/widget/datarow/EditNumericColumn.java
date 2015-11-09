package com.aplana.sbrf.taxaccounting.web.widget.datarow;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ColumnContext;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ValidatedInputCell;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEvent;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.i18n.client.NumberFormat;

import java.math.BigDecimal;

/**
 * @author Vitalii Samolovskikh
 * Числовая колонка С возможностью редактирования
 **/
public class EditNumericColumn extends DataRowColumn<String> {


	private NumberFormat numberFormat;

	public EditNumericColumn(final NumericColumn column, ColumnContext columnContext) {
		super(new ValidatedInputCell(columnContext, true), column);
		this.setHorizontalAlignment(ALIGN_RIGHT);

		StringBuilder mask = new StringBuilder("#");
		int precision = column.getPrecision();
		if (precision > 0) {
			mask.append('.');
			for(int i = 0; i < precision; ++i) {
				mask.append('#');
			}
		}
		this.numberFormat = NumberFormat.getFormat(mask.toString());
		this.setFieldUpdater(new FieldUpdater<DataRow<Cell>, String>() {
			@Override
			public void update(int i, DataRow<Cell> dataRow, String s) {
				try {
                    dataRow.put(getAlias(), new BigDecimal(s));
				} catch (NumberFormatException e) {
					dataRow.put(getAlias(), null);
				}
				CellModifiedEvent event = new CellModifiedEvent(dataRow);
				fireEvent(event);
			}
		});
	}

	@Override
	public String getValue(DataRow dataRow) {
		BigDecimal val = (BigDecimal)dataRow.get(getAlias());
        return val == null ? "" : numberFormat.format(val);
	}
}

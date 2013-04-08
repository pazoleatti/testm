package com.aplana.sbrf.taxaccounting.web.widget.datarow;

import java.math.BigDecimal;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ColumnContext;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ValidatedInputCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.AbstractCellTable;

/** 
 * @author Vitalii Samolovskikh 
 * Числовая колонка С возможностью редактирования 
 **/
public class EditNumericColumn extends DataRowColumn<String> {


	private NumberFormat numberFormat;

	public EditNumericColumn(final NumericColumn column, ColumnContext columnContext) {
		super(new ValidatedInputCell(columnContext), column);
		this.setHorizontalAlignment(ALIGN_RIGHT);

		StringBuffer mask = new StringBuffer("#");
		int precision = column.getPrecision();
		if (precision > 0) {
			mask.append('.');
			for(int i = 0; i < precision; ++i) {
				mask.append('#');
			}
		}
		this.numberFormat = NumberFormat.getFormat(mask.toString());
		this.setFieldUpdater(new FieldUpdater<DataRow, String>() {
			@Override
			public void update(int i, DataRow dataRow, String s) {
				try {
					dataRow.put(getAlias(), numberFormat.parse(s));
				} catch (NumberFormatException e) {
					dataRow.put(getAlias(), null);
				}
			}
		});
	}

	@Override
	public String getValue(DataRow dataRow) {
		BigDecimal val = (BigDecimal)dataRow.get(getAlias());
		return val == null ? "" : numberFormat.format(val);
	}
}

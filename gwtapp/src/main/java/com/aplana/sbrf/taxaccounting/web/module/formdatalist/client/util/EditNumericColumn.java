package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.util;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.AbstractCellTable;

/** 
 * @author Vitalii Samolovskikh 
 **/
public class EditNumericColumn extends DataRowColumn<String> {
	private static Logger logger = Logger.getLogger(EditNumericColumn.class.getName());
	
	private NumberFormat numberFormat;

	public EditNumericColumn(NumericColumn column, final AbstractCellTable<DataRow> cellTable) {
		super(new EditTextCell(), column);
		final EditTextCell cell = (EditTextCell)getCell();

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
					logger.log(Level.WARNING, "Failed to convert inputed value '" + s + "' to number. Reseting value of cell to null");
					dataRow.put(getAlias(), null);
				}
				cell.clearViewData(dataRow);
				cellTable.redraw();
			}
		});
	}

	@Override
	public String getValue(DataRow dataRow) {
		BigDecimal val = (BigDecimal)dataRow.get(getAlias());
		return val == null ? "" : numberFormat.format(val);
	}
}

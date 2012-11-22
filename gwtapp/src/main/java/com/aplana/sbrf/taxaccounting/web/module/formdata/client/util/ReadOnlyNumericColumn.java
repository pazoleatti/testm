package com.aplana.sbrf.taxaccounting.web.module.formdata.client.util;

import java.math.BigDecimal;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ReadOnlyStringCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.AbstractCellTable;

/** 
 * @author Vitalii Samolovskikh
 * Числовая колонка БЕЗ возможности редактирования 
 **/
public class ReadOnlyNumericColumn extends DataRowColumn<String> {
	
	private NumberFormat numberFormat;

	public ReadOnlyNumericColumn(NumericColumn column, final AbstractCellTable<DataRow> cellTable) {
		super(new ReadOnlyStringCell(), column);
//		final Cell cell = getCell();

		StringBuffer mask = new StringBuffer("#");
		int precision = column.getPrecision();
		if (precision > 0) {
			mask.append('.');
			for(int i = 0; i < precision; ++i) {
				mask.append('#');
			}
		}
		this.numberFormat = NumberFormat.getFormat(mask.toString());
	}

	@Override
	public String getValue(DataRow dataRow) {
		BigDecimal val = (BigDecimal)dataRow.get(getAlias());
		return val == null ? "" : numberFormat.format(val);
	}
}

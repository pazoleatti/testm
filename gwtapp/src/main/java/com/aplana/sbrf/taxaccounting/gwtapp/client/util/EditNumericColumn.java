package com.aplana.sbrf.taxaccounting.gwtapp.client.util;

import java.math.BigDecimal;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.i18n.client.NumberFormat;

/** @author Vitalii Samolovskikh */
public class EditNumericColumn extends DataRowColumn<String> {
	private NumberFormat numberFormat;
	
    public EditNumericColumn(NumericColumn column) {
    	super(new EditTextCell(), column);
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
                dataRow.put(getAlias(), numberFormat.parse(s));
            }
        });
    }

	@Override
	public String getValue(DataRow object) {
		BigDecimal val = (BigDecimal)object.get(getAlias());
		return numberFormat.format(val);
	}
}

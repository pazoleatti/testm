package com.aplana.sbrf.taxaccounting.web.module.formdata.client.util;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ReadOnlyStringCell;

/**
 * 
 * @author Eugene Stetsenko
 * Текстовая колонка С возможностью редактирования
 *
 */
public class ReadOnlyTextColumn extends DataRowColumn<String> {

    public ReadOnlyTextColumn(StringColumn col) {
        super(new ReadOnlyStringCell(), col);
    }

    @Override
    public String getValue(DataRow dataRow) {
    	String value = (String)dataRow.get(alias); 
        return value == null ? "" : value;
    }
}

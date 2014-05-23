package com.aplana.sbrf.taxaccounting.web.widget.datarow;

import com.aplana.sbrf.taxaccounting.model.AutoNumerationColumn;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.web.widget.cell.AutoNumerationCell;

/**
 * @author Fail Mukhametdinov
 */
public class AutoNumerationUiColumn extends DataRowColumn<Object> {

    public AutoNumerationUiColumn(AutoNumerationColumn column) {
        super(new AutoNumerationCell(), column);
    }

    @Override
    public Object getValue(DataRow<Cell> dataRow) {
        return (Object) dataRow.get(alias);
    }
}

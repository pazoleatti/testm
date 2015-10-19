package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookItem;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;

public class RefBookItemTextColumn extends Column<RefBookItem, String> {

    private final int valueIndex;

    RefBookItemTextColumn(int valueIndex, Cell<String> cell) {
        this(valueIndex, true, cell);
    }

    RefBookItemTextColumn(int valueIndex, Boolean sortable, Cell<String> cell) {
        super(cell);
        this.valueIndex = valueIndex;
        super.setSortable(sortable);
    }

    @Override
    public String getValue(RefBookItem object) {
        return object.getRefBookRecordDereferenceValues().get(valueIndex).getDereferenceValue();
    }

    @Override
    public int hashCode() {
        return valueIndex;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}

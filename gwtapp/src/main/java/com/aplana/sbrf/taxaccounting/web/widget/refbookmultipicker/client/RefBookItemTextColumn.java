package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.RefBookItem;
import com.google.gwt.user.cellview.client.TextColumn;

public class RefBookItemTextColumn extends TextColumn<RefBookItem> {

    private final int valueIndex;

    RefBookItemTextColumn(int valueIndex) {
        this.valueIndex = valueIndex;
    }

    RefBookItemTextColumn(int valueIndex, Boolean sortable) {
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
}

package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client;

import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.RefBookItem;
import com.google.gwt.user.cellview.client.TextColumn;

public class RefBookItemTextColumn extends TextColumn<RefBookItem> {
	
	private final int valueIndex;
	
	RefBookItemTextColumn(int valueIndex){
		this.valueIndex = valueIndex;
	}

	@Override
	public String getValue(RefBookItem object) {
		return object.getValues().get(valueIndex);
	}

}

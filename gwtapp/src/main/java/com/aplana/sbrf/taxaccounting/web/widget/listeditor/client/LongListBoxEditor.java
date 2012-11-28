package com.aplana.sbrf.taxaccounting.web.widget.listeditor.client;

import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.user.client.ui.ListBox;

public class LongListBoxEditor extends ListBox implements LeafValueEditor<Long> {
	@Override
	public void setValue(Long value) {
		if (value == null){
			return;
		}

		for (int i = 0; i < getItemCount(); i++) {
			String optValue = getValue(i);
			if (value.equals(Long.valueOf(optValue))) {
				setItemSelected(i, true);
			} else {
				setItemSelected(i, false);
			}
		}
	}

	@Override
	public Long getValue() {
		if(getSelectedIndex() < 0){
			return null;
		}
		return Long.valueOf(getValue(getSelectedIndex()));
	}
}

package com.aplana.sbrf.taxaccounting.web.widget.listeditor.client;

import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.user.client.ui.ListBox;

import java.util.ArrayList;
import java.util.List;

public class ListBoxEditor extends ListBox implements LeafValueEditor<List<Long>> {

    @Override
	public void setValue(List<Long> value) {
		if (value == null)
			return;

		for (int i = 0; i < getItemCount(); i++) {
			String optValue = getValue(i);
			if (value.contains(Long.valueOf(optValue))) {
				setItemSelected(i, true);
				if (!isMultipleSelect()) {
					break;
				}
			} else {
				setItemSelected(i, false);
			}
		}

	}

	@Override
	public List<Long> getValue() {
		List<Long> value = new ArrayList<Long>();
		if (isMultipleSelect()) {
			for (int i = 0; i < getItemCount(); i++) {
				if (isItemSelected(i)) {
					String optValue = getValue(i);
					value.add(Long.valueOf(optValue));
				}
			}
		} else {
			int i = getSelectedIndex();
			String optValue = getValue(i);
			value.add(Long.valueOf(optValue));
		}
		return value;
	}

}

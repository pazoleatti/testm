package com.aplana.sbrf.taxaccounting.web.widget.treepicker;

import com.google.gwt.user.client.ui.CheckBox;

public class ExtendedCheckBox extends CheckBox implements NodeSelectedEvent.NodeSelectedEventHandler {
	//Идентификатор родительской ноды
	private Integer parentId;

	public ExtendedCheckBox(String label) {
		super(label);
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	@Override
	public void onNodeSelected(NodeSelectedEvent event) {
		if (event.getNodeId().equals(this.parentId)){
			if (event.isChildSelect()){
				setValue(true);
			} else {
				setValue(false);
			}
		}

	}

}

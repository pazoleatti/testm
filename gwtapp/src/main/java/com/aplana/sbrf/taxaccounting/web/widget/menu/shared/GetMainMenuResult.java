package com.aplana.sbrf.taxaccounting.web.widget.menu.shared;

import java.util.List;

import com.gwtplatform.dispatch.shared.Result;

public class GetMainMenuResult implements Result{
	
	private List<MenuItem> menuItems;

	public List<MenuItem> getMenuItems() {
		return menuItems;
	}

	public void setMenuItems(List<MenuItem> menuItems) {
		this.menuItems = menuItems;
	}
	
}

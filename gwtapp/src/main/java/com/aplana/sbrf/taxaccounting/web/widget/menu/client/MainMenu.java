package com.aplana.sbrf.taxaccounting.web.widget.menu.client;

import java.util.List;

import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.MenuItem;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class MainMenu extends ViewImpl implements MainMenuPresenter.MyView {
	interface Binder extends UiBinder<Widget, MainMenu> {
	}

	private Widget widget;

	@UiField
	Panel panel;

	@Inject
	public MainMenu(final Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setMenuItems(List<MenuItem> links) {
		panel.clear();
		for (MenuItem menuItem : links) {
			InlineHyperlink link = new InlineHyperlink();
			link.setTargetHistoryToken(menuItem.getLink());
			link.setText(menuItem.getName());
			panel.add(link);
		}
	}

}
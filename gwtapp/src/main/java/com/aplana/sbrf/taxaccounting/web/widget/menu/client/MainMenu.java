package com.aplana.sbrf.taxaccounting.web.widget.menu.client;

import java.util.List;

import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.MenuItem;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.MenuBar;
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
	public void setMenuItems(final List<MenuItem> menuItems) {
		panel.clear();
		MenuBar menu = new MenuBar();
		menu.setAnimationEnabled(true);

		for (final MenuItem menuItem : menuItems) {
			if (!menuItem.getSubMenu().isEmpty()) {
				MenuBar subMenuBar = new MenuBar(true);
				for (MenuItem subMenu : menuItem.getSubMenu()) {
					SafeHtmlBuilder sb = new SafeHtmlBuilder();
					sb.appendHtmlConstant("<a href=\"#"
							+ subMenu.getLink() + ";"
							+ "nType="
							+ menuItem.getLink()
							+ "\" style=\"color:#000000; font-family: Tahoma; text-decoration:none;\"><div>"
							+ subMenu.getName() + "</div></a>");
					subMenuBar.addItem(new com.google.gwt.user.client.ui.MenuItem(sb.toSafeHtml()));
				}
				menu.addItem(menuItem.getName(), subMenuBar);
				menu.addSeparator();
				panel.add(menu);
			}
			else {
				InlineHyperlink link = new InlineHyperlink();
				link.setTargetHistoryToken(menuItem.getLink());
				link.setText(menuItem.getName());
				panel.add(link);
			}
		}
	}

}
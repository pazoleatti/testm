package com.aplana.sbrf.taxaccounting.web.widget.menu.client;

import java.util.List;

import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.MenuItem;
import com.google.gwt.dom.client.Style;
import com.google.gwt.resources.client.CssResource;
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
	interface MainMenuStyle extends CssResource {
		String grayMenuItem();
	}

	private Widget widget;

	@UiField
	Panel panel;

	@UiField MainMenuStyle style;

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
		menu.setAnimationEnabled(false);

		for (final MenuItem menuItem : menuItems) {
			if (!menuItem.getSubMenu().isEmpty()) {
				MenuBar subMenuBar = new MenuBar(true);
				for (MenuItem subMenu : menuItem.getSubMenu()) {
					SafeHtmlBuilder sb = new SafeHtmlBuilder();
					sb.appendHtmlConstant("<a href=\"#"
							+ subMenu.getLink()
							+ (menuItem.getLink().isEmpty() ? "" :";nType=" + menuItem.getLink())
							+ "\" style=\"color:#000000; font-family: Tahoma; text-decoration:none;\"><div>"
							+ subMenu.getName() + "</div></a>");
					com.google.gwt.user.client.ui.MenuItem subMenuItem =
							new com.google.gwt.user.client.ui.MenuItem(sb.toSafeHtml());
					subMenuItem.getElement().addClassName(style.grayMenuItem());
					subMenuBar.addItem(subMenuItem);
				}
				menu.addItem(menuItem.getName() + " " + getArrowSymbol(), subMenuBar);
				menu.addSeparator().getElement().getStyle().setBorderStyle(Style.BorderStyle.NONE);
			}
			else {
				SafeHtmlBuilder sb = new SafeHtmlBuilder();
				sb.appendHtmlConstant("<style>a:hover {color: #000000 !important}</style>");
				sb.appendHtmlConstant("<a href=\"#"
						+ menuItem.getLink() + ";"
						+ "nType="
						+ menuItem.getLink()
						+ "\" style=\"color:white; font-family: Tahoma; text-decoration:none;\">"
						+ menuItem.getName() + "</a>");
				menu.addItem(new com.google.gwt.user.client.ui.MenuItem(sb.toSafeHtml()));
				menu.addSeparator().getElement().getStyle().setBorderStyle(Style.BorderStyle.NONE);
			}
		}
		panel.add(menu);
	}

	private String getArrowSymbol() {
		return "\u25BC";
	}

}
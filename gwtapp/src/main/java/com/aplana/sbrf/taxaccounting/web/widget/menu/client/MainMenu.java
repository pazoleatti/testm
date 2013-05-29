package com.aplana.sbrf.taxaccounting.web.widget.menu.client;

import java.util.List;

import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.MenuItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class MainMenu extends ViewImpl implements MainMenuPresenter.MyView {

	interface LocalHtmlTemplates extends SafeHtmlTemplates {
		@Template("<a style=\"color:#000000; text-decoration:none;\" href=\"{0}\"><div>{1}</div></a>")
		SafeHtml link(String url, String name);
	}

	interface Binder extends UiBinder<Widget, MainMenu> {
	}

	interface MainMenuStyle extends CssResource {
		String grayMenuItem();
	}

	private static final LocalHtmlTemplates template = GWT.create(LocalHtmlTemplates.class);

	@UiField
	Panel panel;

	@UiField MainMenuStyle style;

	@Inject
	public MainMenu(final Binder binder) {
		initWidget(binder.createAndBindUi(this));
	}

	@Override
	public void setMenuItems(final List<MenuItem> menuItems) {
		panel.clear();
		MenuBar menu = new MenuBar();
		menu.setAnimationEnabled(false);

		for (MenuItem item : menuItems) {
			MenuBar subMenuBar = new MenuBar(true);
			addSubMenu(item, subMenuBar);
			menu.addItem(item.getName() + " " + getArrowSymbol(), subMenuBar);
			menu.addSeparator().getElement().getStyle().setBorderStyle(Style.BorderStyle.NONE);
		}
		panel.add(menu);
	}

	private void addSubMenu(MenuItem menuItem, MenuBar menu) {
		for (MenuItem item : menuItem.getSubMenu()) {
			if (!item.getSubMenu().isEmpty()) {
				MenuBar subMenuBar = new MenuBar(true);
				addSubMenu(item, subMenuBar);
				menu.addItem(item.getName(), subMenuBar);
			} else {
				com.google.gwt.user.client.ui.MenuItem subMenuItem =
						new com.google.gwt.user.client.ui.MenuItem(template.link(item.getLink(), item.getName()));
				subMenuItem.getElement().addClassName(style.grayMenuItem());
				menu.addItem(subMenuItem);
			}
		}

	}

	private String getArrowSymbol() {
		return "\u25BC";
	}

}
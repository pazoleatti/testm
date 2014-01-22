package com.aplana.sbrf.taxaccounting.web.widget.menu.client;

import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.MenuItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.http.client.*;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

import java.util.List;

public class MainMenu extends ViewImpl implements MainMenuPresenter.MyView {

	interface LocalHtmlTemplates extends SafeHtmlTemplates {
		@Template("<a style=\"color:#000000; text-decoration:none;\" href=\"{0}\">{1}</a>")
		SafeHtml link(String url, SafeHtml divName);

        @Template("<div>{0}</div>")
        SafeHtml div(String name);
	}

	interface Binder extends UiBinder<Widget, MainMenu> {
	}

	private static final LocalHtmlTemplates template = GWT.create(LocalHtmlTemplates.class);

    @UiField
    MenuBar menu;

    private Timer timer;

	@Inject
	public MainMenu(final Binder binder) {
		initWidget(binder.createAndBindUi(this));
        final RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, "download/timer/ping/");
        final RequestCallback requestCallback = new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
            }

            @Override
            public void onError(Request request, Throwable throwable) {

            }
        };
        timer = new Timer() {
            @Override
            public void run() {
                try {
                    requestBuilder.sendRequest(null, requestCallback);
                } catch (RequestException e) {
                }
            }
        };
        timer.scheduleRepeating(300000);
	}

	@Override
	public void setMenuItems(final List<MenuItem> menuItems) {
        menu.clearItems();
		for (MenuItem item : menuItems) {
			MenuBar subMenuBar = new MenuBar(true);
			addSubMenu(item, subMenuBar);
			menu.addItem(item.getName() + " " + getArrowSymbol(), subMenuBar);
			menu.addSeparator().getElement().getStyle().setBorderStyle(Style.BorderStyle.NONE);
		}
	}

	private void addSubMenu(MenuItem menuItem, MenuBar menu) {
		for (MenuItem item : menuItem.getSubMenu()) {
			if (!item.getSubMenu().isEmpty()) {
				MenuBar subMenuBar = new MenuBar(true);
				addSubMenu(item, subMenuBar);
				menu.addItem(template.div(item.getName()).asString(), true, subMenuBar);
			} else {
				com.google.gwt.user.client.ui.MenuItem subMenuItem =
						new com.google.gwt.user.client.ui.MenuItem(template.link(item.getLink(), template.div(item.getName())));
				subMenuItem.setScheduledCommand(new Scheduler.ScheduledCommand() {
					@Override
					public void execute() {
                        getMenu().selectItem(null);
					}
				});
				menu.addItem(subMenuItem);
			}
		}
	}

	private String getArrowSymbol() {
		return "\u25BC";
	}

    private MenuBar getMenu() {
        return menu;
    }
}
package com.aplana.sbrf.taxaccounting.web.widget.menu.client;

import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.MenuItem;
import com.google.gwt.dom.client.Style;
import com.google.gwt.http.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import java.util.List;

/**
 * Представление для главного меню
 */
public class MainMenuView extends AbstractMenuView implements MainMenuPresenter.MyView {

    private NotificationMenuItem notificationMenuItem;

    @Inject
    public MainMenuView(final Binder binder) {
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
        Timer timer = new Timer() {
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
    public void setNotificationMenuItem(NotificationMenuItem item) {
        notificationMenuItem = item;
        menu.addItem(item);
    }

    @Override
    public void updateNotificationCount(int count) {
        notificationMenuItem.setCount(count);
    }

    @Override
    public void selectNotificationMenuItem() {
        menu.selectItem(notificationMenuItem);
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        menu.clearItems();
        for (MenuItem item : menuItems) {
            MenuBar subMenuBar = new MenuBar(true);
            addSubMenu(item, subMenuBar);
            menu.addItem(item.getName() + " " + getArrowSymbol(), subMenuBar);
            menu.addSeparator().getElement().getStyle().setBorderStyle(Style.BorderStyle.NONE);
        }
    }

    protected String getArrowSymbol() {
        return "\u25BC";
    }

    @UiTemplate("MenuView.ui.xml")
    interface Binder extends UiBinder<Widget, MainMenuView> {
    }
}
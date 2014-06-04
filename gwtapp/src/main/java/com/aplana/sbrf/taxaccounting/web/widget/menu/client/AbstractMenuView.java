package com.aplana.sbrf.taxaccounting.web.widget.menu.client;

import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.MenuItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.MenuBar;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 * Абстрактный класс представления меню
 *
 * @author Fail Mukhametdinov
 */
public abstract class AbstractMenuView extends ViewImpl implements AbstractMenuPresenter.MyView {

    protected static final LocalHtmlTemplates template = GWT.create(LocalHtmlTemplates.class);

    @UiField
    MenuBar menu;

    protected void addSubMenu(MenuItem menuItem, MenuBar menu) {
        for (MenuItem item : menuItem.getSubMenu()) {
            if (!item.getSubMenu().isEmpty()) {
                MenuBar subMenuBar = new MenuBar(true);
                addSubMenu(item, subMenuBar);
                menu.addItem(template.div(item.getName()).asString(), true, subMenuBar);
            } else {
                com.google.gwt.user.client.ui.MenuItem subMenuItem =
                        new com.google.gwt.user.client.ui.MenuItem(template.link(item.getLink(), template.div(item.getName())));
                // Спрятать меню после выбора элемента
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

    protected MenuBar getMenu() {
        return menu;
    }

    interface LocalHtmlTemplates extends SafeHtmlTemplates {
        @Template("<a style=\"color:#000000; text-decoration:none;\" href=\"{0}\">{1}</a>")
        SafeHtml link(String url, SafeHtml divName);

        @Template("<div>{0}</div>")
        SafeHtml div(String name);
    }
}

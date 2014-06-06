package com.aplana.sbrf.taxaccounting.web.widget.menu.client;

import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.MenuItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import java.util.List;

/**
 * Представление для меню "Руководство пользователя"
 *
 * @author Fail Mukhametdinov
 */
public class ManualMenuView extends AbstractMenuView implements ManualMenuPresenter.MyView {

    private static final LocalHtmlTemplates template = GWT.create(LocalHtmlTemplates.class);

    @Inject
    public ManualMenuView(final Binder binder) {
        initWidget(binder.createAndBindUi(this));
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        menu.clearItems();
        for (MenuItem item : menuItems) {
            MenuBar subMenuBar = new MenuBar(true);
            addSubMenu(item, subMenuBar);
            Image image = new Image("resources/img/question_mark.png");
            image.setTitle("Руководство пользователя");
            menu.addItem(item.getName(), subMenuBar)
                    .getElement().appendChild(image.getElement())
                    .getParentElement().setAttribute("style", "border:0");
        }
    }

    @Override
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

    @UiTemplate("MenuView.ui.xml")
    interface Binder extends UiBinder<Widget, ManualMenuView> {
    }

    interface LocalHtmlTemplates extends SafeHtmlTemplates {
        @Template("<a style=\"color:#000000; text-decoration:none;\" href=\"{0}\" target=\"_blank\">{1}</a>")
        SafeHtml link(String url, SafeHtml divName);

        @Template("<div>{0}</div>")
        SafeHtml div(String name);
    }
}

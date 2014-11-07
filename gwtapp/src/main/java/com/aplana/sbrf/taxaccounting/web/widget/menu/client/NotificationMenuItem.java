package com.aplana.sbrf.taxaccounting.web.widget.menu.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuBar;

public class NotificationMenuItem extends MenuItem {

    public static final MyTemplate TEMPLATES = GWT.create(MyTemplate.class);

    public interface MyTemplate extends SafeHtmlTemplates {
        @Template("<img src=\"{0}\" /><span style=\"vertical-align: top; margin-left: 5px;\">{1}</span>")
        SafeHtml notifyMsg(String iconUrl, String msg);
        @Template("<span>{0}</span>")
        SafeHtml noMsg(String msg);
    }

    private static final String NEW_MSG_ICON = "resources/img/attention_on_20.png";

    public NotificationMenuItem() {
        super(TEMPLATES.noMsg("Нет оповещений"));
    }

    public void setCount(int count) {
        if (count > 0) {
            setHTML(TEMPLATES.notifyMsg(NEW_MSG_ICON,
                    decline(count, "оповещение", "оповещения", "оповещений")));
        } else {
            setHTML(TEMPLATES.noMsg("Нет оповещений"));
        }
    }

    /**
     * Склоняет слово в зависимости от идущего до него числительного
     * @param num числительное
     * @param nominative именительный падеж слова
     * @param singular родительный падеж слова, ед. число
     * @param plural множественное число
     * @return правильное склонение слова
     */
    private String decline(int num, String nominative, String singular, String plural) {
        String text;
        if (num > 10 && ((num % 100) / 10) == 1) return num + " " + plural;

        switch (num % 10)
        {
            case 1:
                text = nominative;
                break;
            case 2:
            case 3:
            case 4:
                text = singular;
                break;
            default: // case 0, 5-9
                text = plural;
        }
        return num + " " + text;
    }
}

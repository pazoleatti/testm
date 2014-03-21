package com.aplana.sbrf.taxaccounting.web.widget.menu.client;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.MenuItem;

public class NotificationMenuItem extends MenuItem {

    int count = 0;
    String name;

    public NotificationMenuItem(String text) {
        super(SafeHtmlUtils.fromString(text));
        name = text;
    }

    public void setCount(int count) {
        this.count = count;
        setText(String.valueOf(count) + " " + name);
    }

    public int getCount() {
        return count;
    }
}

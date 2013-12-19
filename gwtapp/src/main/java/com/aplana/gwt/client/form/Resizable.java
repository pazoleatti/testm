package com.aplana.gwt.client.form;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Контейнер для элементов заголовка формы по середине.
 * Имеет возможность скрывать длинный текст за многоточием
 *
 * @author aivanov
 */
public class Resizable extends SimplePanel {

    GlobalResources.Style style;

    public Resizable() {
        super();
        style = GlobalResources.INSTANCE.style();
        style.ensureInjected();
    }

    @Override
    public void add(Widget widget) {
        widget.getElement().addClassName(style.resizeOverflow());
        super.add(widget);
    }
}

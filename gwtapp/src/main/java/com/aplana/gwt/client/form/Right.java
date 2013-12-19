package com.aplana.gwt.client.form;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Контейнер для элементов заголовка формы слева
 *
 * @author aivanov
 */
public class Right extends ComplexPanel {

    GlobalResources.Style style;

    public Right() {
        setElement(DOM.createDiv());
        style = GlobalResources.INSTANCE.style();
        style.ensureInjected();
        getElement().addClassName(style.right());
    }

    @Override
    public void add(Widget widget) {
        widget.getElement().addClassName(style.childInherit());
        insert(widget, getElement(), 0, true);  // так как элементы опмещаются по правому рядому стеком, то всегда помещает перед первым
    }
}

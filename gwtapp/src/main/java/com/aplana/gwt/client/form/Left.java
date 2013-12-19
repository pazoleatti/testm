package com.aplana.gwt.client.form;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Контейнер для элементов заголовка формы слева
 *
 * @author aivanov
 */
public class Left extends ComplexPanel {

    GlobalResources.Style style;

    public Left() {
        setElement(DOM.createDiv());
        style = GlobalResources.INSTANCE.style();
        style.ensureInjected();
        getElement().addClassName(style.left());
    }

    @Override
    public void add(Widget widget) {
        widget.getElement().addClassName(style.childInherit());
        add(widget, getElement());
    }
}

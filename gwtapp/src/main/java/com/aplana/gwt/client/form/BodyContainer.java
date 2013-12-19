package com.aplana.gwt.client.form;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Контейнер для основной части типовой формы
 *
 * @author aivanov
 */
public class BodyContainer extends ComplexPanel {

    private GlobalResources.Style style;

    public BodyContainer() {
        setElement(DOM.createDiv());
        setupStyle();
    }

    public void setupStyle() {
        style = GlobalResources.INSTANCE.style();
        style.ensureInjected();

        getElement().addClassName(style.typicalFormBody());
    }

    public void add(Widget widget) {
        add(widget, getElement());
    }

}

package com.aplana.gwt.client.form;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Контейнер типовой формы
 *
 * @author aivanov
 */
public class MainContainer extends ComplexPanel {

    public MainContainer() {
        setElement(DOM.createDiv());

        //setup MainContainer style
        getElement().getStyle().setWidth(100, Style.Unit.PCT);
        getElement().getStyle().setHeight(100, Style.Unit.PCT);
    }

    @UiChild(tagname = "header", limit = 1)
    public void addHeader(HeaderContainer header) {
        add(header, getElement());
    }

    @UiChild(tagname = "body", limit = 1)
    public void addBody(BodyContainer body) {
        add(body, getElement());
    }

    @Override
    public void add(Widget widget) {
        GWT.log("Support only body and header elements.");
        throw new IllegalStateException(
                "Support only body and header elements.");
    }

}

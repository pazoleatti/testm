package com.aplana.gwt.client.form;

import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Контейнер для основной части типовой формы
 *
 * @author aivanov
 */
public class BodyContainer extends HTMLPanel {

    private FormResources.Style style;

    public BodyContainer() {
        super("");
        setupStyle();
    }

    public BodyContainer(String html) {
        super(html);
        setupStyle();
    }

    public void setupStyle() {
        style = FormResources.INSTANCE.style();
        style.ensureInjected();

        getElement().addClassName(style.typicalFormBody());
    }

}

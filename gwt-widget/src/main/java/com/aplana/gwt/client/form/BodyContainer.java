package com.aplana.gwt.client.form;

import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Контейнер для основной части типовой формы
 *
 * @author aivanov
 */
public class BodyContainer extends HTMLPanel {

    public BodyContainer() {
        super("");
        setupStyle();
    }

    public BodyContainer(String html) {
        super(html);
        setupStyle();
    }

    public final void setupStyle() {
        FormResources.Style style = FormResources.INSTANCE.style();
        style.ensureInjected();

        getElement().addClassName(style.typicalFormBody());
    }

}

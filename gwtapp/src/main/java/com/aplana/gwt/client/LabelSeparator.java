package com.aplana.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Label-разделитель
 * @author vpetrov
 */
public class LabelSeparator extends Composite {
    private static Binder uiBinder = GWT.create(Binder.class);

    interface Binder extends UiBinder<Widget, LabelSeparator> {
    }

    @UiField
    Label separator;

    public LabelSeparator() {
        setStyleName("AplanaLabelSeparator");
    }

    @Inject
    @UiConstructor
    public LabelSeparator(String text) {
        initWidget(uiBinder.createAndBindUi(this));
        separator.setText(text);
    }
}

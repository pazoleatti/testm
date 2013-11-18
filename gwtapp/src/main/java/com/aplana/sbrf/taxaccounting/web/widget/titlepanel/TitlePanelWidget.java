package com.aplana.sbrf.taxaccounting.web.widget.titlepanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

public class TitlePanelWidget extends Composite implements HasValue<String> {

    interface Binder extends UiBinder<HorizontalPanel, TitlePanelWidget>{
    }

    private static Binder uiBinder = GWT.create(Binder.class);

    @UiField
    Label text;

    @UiField
    Button closeButton;

    private String value;

    @UiConstructor
    public TitlePanelWidget() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setClosedPanelAction(final PanelClosingAction action) {
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
               action.onClose();
            }
        });
    }

    @Override
    public String getValue() {
        return text.getText();
    }

    @Override
    public void setValue(String value) {
        this.value = value;
        text.setText(value);
    }

    @Override
    public void setValue(String value, boolean fireEvents) {
        this.value = value;
        text.setText(value);
        if (fireEvents){
            ValueChangeEvent.fire(this, this.value);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}

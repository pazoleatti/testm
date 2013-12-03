package com.aplana.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Composite;
import com.google.inject.Inject;

import java.util.List;


public class RefBookTextBox extends Composite {
    private static Binder uiBinder = GWT.create(Binder.class);

    interface Binder extends UiBinder<Widget, RefBookTextBox> {
    }

    @UiField
    HorizontalPanel buttonPanel;

    @UiField
    TextBox text;


    @Inject
    public RefBookTextBox() {
        initWidget(uiBinder.createAndBindUi(this));
        addStyleName("AplanaRefbookTextBox");
    }


    /**
     * Добавляет кнопки и из обработчики в виджет
     *
     * @param buttons обработчики кнопок
     */
    public void addButtons(List<RefBookButtonData> buttons) {
        buttonPanel.clear();
        for (RefBookButtonData data : buttons) {
            PushButton btn = new PushButton(new Image(data.getImageUrl()));
            btn.addClickHandler(data.getClickHandler());
            btn.addStyleName("AplanaRefbookButton");
            btn.addStyleName("rbbtn");
            buttonPanel.add(btn);
       /*     setEnabled(false);
            setEnabled(true);*/
        }

    }

    public void setEnabled(boolean enabled){
        text.setEnabled(enabled);
        buttonPanel.setVisible(enabled);
    }


}

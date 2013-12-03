package com.aplana.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;

/**
 * Компонент выбора года
 * User: vpetrov
 * Date: 26.11.13
 */
public class SpinEdit extends Composite {

    private static Binder uiBinder = GWT.create(Binder.class);

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public int getMinValue() {
        return minValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }


    interface Binder extends UiBinder<Widget, SpinEdit> {
    }

    private int maxValue = Integer.MAX_VALUE;

    private int minValue = Integer.MIN_VALUE;

    @UiField
    Label lbl;

    @UiField
    TextBox txt;

    @UiField
    PushButton btnUp;

    @UiField
    PushButton btnDown;

    @Inject
    public SpinEdit(){
        initWidget(uiBinder.createAndBindUi(this));
        addStyleName("AplanaSpinEdit");
    }

    @Inject
    @UiConstructor
    public SpinEdit(String labelText){
        initWidget(uiBinder.createAndBindUi(this));
        addStyleName("AplanaSpinEdit");
        lbl.setText(labelText);

        txt.addKeyPressHandler(new KeyPressHandler(){
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (!Character.isDigit(event.getCharCode())) {
                        ((TextBox)event.getSource()).cancelKey();
                    return;
                }

                long num;
                try {
                    if (txt.getText().isEmpty())
                        num = 0;
                    else
                        num = Long.valueOf(txt.getText());
                }catch (Exception e){
                    ((TextBox)event.getSource()).cancelKey();
                    return;
                }

                if ((num * 10 + Integer.valueOf(Character.toString((char)event.getCharCode())) < minValue)||( num * 10 + Integer.valueOf(Character.toString((char)event.getCharCode())) > maxValue)){
                    ((TextBox)event.getSource()).cancelKey();
                    return;
                }
            }
        });

        btnUp.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                int num = 0;

                if (!txt.getText().isEmpty())
                    num = Integer.valueOf(txt.getText());
                if (num < maxValue)
                    num ++;
                txt.setText(Integer.toString(num));
            }
        });

        btnDown.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                int num = 0;
                if (!txt.getText().isEmpty())
                    num = Integer.valueOf(txt.getText());
                if (num > minValue)
                    num --;
                txt.setText(Integer.toString(num));
            }
        });


    }

}

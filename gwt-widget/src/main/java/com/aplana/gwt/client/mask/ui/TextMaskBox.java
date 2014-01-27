package com.aplana.gwt.client.mask.ui;

import com.aplana.gwt.client.mask.MaskBox;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.text.shared.testing.PassthroughParser;
import com.google.gwt.text.shared.testing.PassthroughRenderer;

/**
 * Виджет для ввода текста с маской
 *
 * beta-версия
 *
 * @author aivanov
 */
public class TextMaskBox extends MaskBox<String> {

    private String deferredValue;
    private boolean mayBeNull = true;

    public TextMaskBox() {
        super(PassthroughRenderer.instance(), PassthroughParser.instance());
    }

    @Override
    public String getText() {
        return isEqualsTextPicture(super.getText()) ? "" : super.getText();
    }

    public void setText(String text){
        if (getMask() != null) {
            if(getMask().length() < text.length()){
                text = text.substring(0, getMask().length());
            }
            super.setText(text);
        } else {
            super.setText(text);
        }
    }

    @Override
    public String getValue() {
        if (!checkValue(super.getValue())) {
            return deferredValue;
        }
        return super.getValue();
    }


    @Override
    public void setValue(String value) {
        this.setValue(value, false);
    }

    @Override
    public void setValue(String value, boolean fireEvents) {
        if (checkValue(value)) {
            super.setValue(value, false);
            setText(value == null ? getTextPicture() : value);
            removeExceptionStyle();
        }
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    /**
     * Проверить что дата не пуста и после запомнить удачное значение
     *
     * @param value дата
     * @return если null то вернет false
     */
    private boolean checkValue(String value) {
        if (value == null && !mayBeNull) {
            return false;
        } else {
            deferredValue = value;
            return true;
        }
    }

    public boolean isMayBeNull() {
        return mayBeNull;
    }

    public void setMayBeNull(boolean mayBeNull) {
        this.mayBeNull = mayBeNull;
    }

}

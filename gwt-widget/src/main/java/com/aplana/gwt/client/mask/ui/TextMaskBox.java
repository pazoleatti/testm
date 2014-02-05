package com.aplana.gwt.client.mask.ui;

import com.aplana.gwt.client.mask.MaskBox;
import com.aplana.gwt.client.mask.parser.TextMaskParser;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.text.shared.testing.PassthroughRenderer;
import com.google.gwt.uibinder.client.UiConstructor;

/**
 * Виджет для ввода текста с маской
 *
 * @author aivanov
 */
public class TextMaskBox extends MaskBox<String> {

    private String deferredValue;
    private boolean mayBeNull = true;

    @UiConstructor
    public TextMaskBox(String mask) {
        super(PassthroughRenderer.instance(), TextMaskParser.instance(mask));
        super.setMask(mask);
    }

    @Override
    public String getText() {
        return isEqualsTextPicture(super.getText()) ? "" : super.getText();
    }

    public void setText(String text){
        if (getMask() != null) {
            if (text != null) {
                if (getMask().length() < text.length()) {
                    text = text.substring(0, getMask().length());
                }
            } else {
                text = getTextPicture();
            }
        }
        super.setText(text);
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
            setText(getTextIfNullOrEmpty(value));
            removeExceptionStyle();
        }
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    /**
     * Проверить что текст не пуста и после запомнить удачное значение
     *
     * @param value текст
     * @return если null то вернет false
     */
    private boolean checkValue(String value) {
        if (value == null) {
            if (!mayBeNull) {
                return false;
            }
            deferredValue = null;
            return true;
        }
        deferredValue = value;
        return true;
    }

    private String getTextIfNullOrEmpty(String text){
        return (text == null || text.isEmpty()) ? getTextPicture() : text;
    }

    public boolean isMayBeNull() {
        return mayBeNull;
    }

    public void setMayBeNull(boolean mayBeNull) {
        this.mayBeNull = mayBeNull;
    }

}

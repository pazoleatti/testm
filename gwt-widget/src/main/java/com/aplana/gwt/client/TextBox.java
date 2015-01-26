package com.aplana.gwt.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;

/**
 * TextBox, который в задимсабленном состоянии показывает Label.
 * @author Vitaliy Samolovskikh
 */
public class TextBox extends DoubleStateWrapper<com.google.gwt.user.client.ui.TextBox, String> implements HasText, HasValue<String> {

    /**
     * Признак валидности длины введенной строки
     */
    private boolean isValid = true;

    public TextBox() {
		super(new com.google.gwt.user.client.ui.TextBox());
	}

    /**
     * Устанавливает максимальную длину вводимой строки
     */
    public void setMaxLength(int length){
        widget.setMaxLength(length);
    }

    /**
     * Возвращает максимальную длину вводимой строки
     */
    public int getMaxLength() {
        return widget.getMaxLength();
    }

    /**
     * Устанавливает фиксированную длину вводимой строки
     *
     * @param length количество символов
     */
    public void setLength(final int length) {
        this.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                String value = event.getValue();
                isValid = value.isEmpty() || value.length() == length;

                if (isValid) {
                    widget.removeStyleName("gwt-TextBox-invalid");
                } else {
                    widget.addStyleName("gwt-TextBox-invalid");
                }
            }
        });
    }

    public boolean isValid() {
        return isValid;
    }

    @Override
    public String getText() {
        return super.getValue();
    }

    @Override
    public void setText(String text) {
        super.setValue(text);
    }
}

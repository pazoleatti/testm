package com.aplana.gwt.client;

import com.google.gwt.user.client.ui.HasText;

/**
 * TextBox, который в задимсабленном состоянии показывает Label.
 * @author Vitaliy Samolovskikh
 */
public class TextBox extends DoubleStateWrapper<com.google.gwt.user.client.ui.TextBox, String> implements HasText {
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

    @Override
    public String getText() {
        return super.getValue();
    }

    @Override
    public void setText(String text) {
        super.setValue(text);
    }
}

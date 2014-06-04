package com.aplana.gwt.client;

/**
 * TextArea, который в задимсабленном состоянии показывает Label.
 *
 * @author Vitaliy Samolovskikh
 */
public class TextArea extends DoubleStateWrapper<com.google.gwt.user.client.ui.TextArea, String> {
    public TextArea() {
        super(new com.google.gwt.user.client.ui.TextArea());
    }

    /**
     * Устанавливает максимальную длину вводимой строки
     */
    public void setMaxLength(String length){
        widget.getElement().setAttribute("maxLength", length);
    }

    public int getVisibleLines() {
        return widget.getVisibleLines();
    }

    public void setVisibleLines(int lines) {
        widget.setVisibleLines(lines);
        label.setHeight(20 * lines + "px");
    }
}
package com.aplana.gwt.client.mask;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ValueBox;
import com.google.gwt.user.client.ui.ValueBoxBase;

/**
 * Виджет для ввода текста с маской
 *
 * @author aivanov
 */
public class MaskBox<T> extends ValueBox<T> {

    private int length = 255;
    private boolean enabled;

    private String mask;
    private MaskListener maskListener;

    private MaskBox source = this;

    public MaskBox(Element element, Renderer<T> renderer, final Parser<T> parser) {
        super(element, renderer, parser);
        setStyleName("gwt-TextBox");
    }

    @Override
    public String getText() {
        return isEqualsTextPicture(super.getText()) ? "" : super.getText();
    }

    public void setupMaskListener(String mask) {
        if (maskListener == null) {
            maskListener = new MaskListener(this, mask);
            addBlurHandler(new BlurHandler() {
                public void onBlur(BlurEvent event) {
                    System.out.println("-ValueChangeEvent");
                    ValueChangeEvent.fire(source, getText());
                }
            });
        } else
            maskListener.setMask(mask);
    }

    public void setMask(String mask) {
        this.mask = mask;

        setupMaskListener(mask);
        setText(getTextPicture());
        setLength(mask.length());
    }

    public String getMask() {
        return mask;
    }

    public String getTextPicture() {
        return maskListener != null ? maskListener.getMaskPicture() : null;
    }

    public void setLength(int length) {
        this.length = length;
        setMaxLength(length);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public int getLength() {
        return length;
    }

    public void enable(boolean enabled) {
        this.enabled = enabled;
        setReadOnly(!enabled);
        if (!enabled) {
            unsinkEvents(Event.KEYEVENTS);
        } else
            sinkEvents(Event.KEYEVENTS);
    }

    protected boolean isEqualsTextPicture(String value) {

        return getTextPicture() != null && getTextPicture().equals(value);
    }

    public void addExceptionStyle() {
        getElement().getStyle().setBackgroundColor("yellow");
    }

    public void removeExceptionStyle() {
        getElement().getStyle().setBackgroundColor("");
    }

    protected ValueBoxBase getSource() {
        return source;
    }
}

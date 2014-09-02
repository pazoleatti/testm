package com.aplana.gwt.client.mask;

import com.google.gwt.dom.client.Document;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ValueBox;
import com.google.gwt.user.client.ui.ValueBoxBase;

import java.text.ParseException;

/**
 * Виджет для ввода текста с маской
 * НЕ следует использовать не посредственно.
 *
 * Рабочие символы маски это 9 (только цифры) и Х(любой символ, даже цифра)
 * Например, 99.99.9999 - для дат вида 12.12.1234 или ХХХХХХХХ - абвгдеёж
 *
 * @author aivanov
 */
public class MaskBox<T> extends ValueBox<T> implements LeafValueEditor<T> {

    private int length = 255;
    private boolean enabled;

    private String mask;
    private MaskListener maskListener;
    private Parser<T> parser;

    // то что показывается в элементе ввода
    private String textPicture;

    private MaskBox source = this;

    protected MaskBox(Renderer<T> renderer, final Parser<T> parser) {
        super(Document.get().createTextInputElement(), renderer, parser);
        setStyleName("gwt-TextBox");
        this.parser = parser;
        addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                selectAll();
            }
        });
    }

    /**
     * При установки маски просиходит регистрация специальных обработчиков
     * @param mask маска вида 99.9999 и пр.
     */
    public void setupMaskListener(String mask) {
        if (maskListener == null) {
            maskListener = new MaskListener(this, mask);
            addBlurHandler(new BlurHandler() {
                public void onBlur(BlurEvent event) {
                    try {
                        setValue(getText().equals(textPicture) ? null : parser.parse(getText()), true);
                    } catch (ParseException e) {
                        if (textPicture == null || !textPicture.equals(getText())) {
                            addExceptionStyle();
                        }
                    }
                }
            });
        } else
            maskListener.setMask(mask);

        textPicture = getTextPicture();
    }

    /**
     * Ручной сброс фокуса
     */
    public void trySetValue(){
        BlurEvent.fireNativeEvent(Document.get().createBlurEvent(), this);
    }

    public void setMask(String mask) {
        if (mask != null && !mask.isEmpty()) {
            this.mask = mask;

            setupMaskListener(mask);
            setText(getTextPicture());
            setLength(mask.length());
        }
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

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        setReadOnly(!enabled);
        if (!enabled) {
            unsinkEvents(Event.KEYEVENTS);
            unsinkEvents(Event.MOUSEEVENTS);
            unsinkEvents(Event.BUTTON_LEFT);
            unsinkEvents(Event.FOCUSEVENTS);
        } else {
            sinkEvents(Event.KEYEVENTS);
            sinkEvents(Event.MOUSEEVENTS);
            sinkEvents(Event.BUTTON_LEFT);
            sinkEvents(Event.FOCUSEVENTS);
        }
    }

    protected boolean isEqualsTextPicture(String value) {
        return getTextPicture() != null && getTextPicture().equals(value);
    }

    public void addExceptionStyle() {
        getElement().getStyle().setBackgroundColor("#ffccd2");
    }

    public void removeExceptionStyle() {
        getElement().getStyle().setBackgroundColor("");
    }

    /**
     * Включение/выключение вывода отладочной информации в консоль
     * @param isDebug значение
     */
    public void setDebug(boolean isDebug) {
        if (maskListener != null) {
            maskListener.setDebug(isDebug);
        }
    }

    public boolean isDebug() {
        if (maskListener != null) {
            maskListener.isDebug();
        }
        return false;
    }

    protected ValueBoxBase getSource() {
        return source;
    }
}

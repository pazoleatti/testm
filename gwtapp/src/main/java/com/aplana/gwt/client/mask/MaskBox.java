package com.aplana.gwt.client.mask;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
public class MaskBox<T> extends ValueBox<T> {

    /**
     * Формирование строки которая показывается в элементе ввода
     * Например для маски 9999 (или XXXX) будет ____
     * для маски 999.999.999.999 (или XXX.XXX.XXX.XXX) ___.___.___.___
     * @param mask маска вида 99.9999 и пр.
     * @return строку вида ___.___
     */
    public static String createMaskPicture(String mask) {
        StringBuffer pic = new StringBuffer();
        for (char mc : mask.toCharArray()) {
            switch (mc) {
                case '9':
                case 'X':
                    pic.append("_");
                    break;
                default:
                    pic.append(mc);
            }
        }
        return pic.toString();
    }

    private int length = 255;
    private boolean enabled;

    private String mask;
    private MaskListener maskListener;

    // то что показывается в элементе ввода
    // yfghbth
    private String textPicture;

    private MaskBox source = this;

    protected MaskBox(Renderer<T> renderer, final Parser<T> parser) {
        super(Document.get().createTextInputElement(), renderer, parser);
        setStyleName("gwt-TextBox");

        getSource().addValueChangeHandler(new ValueChangeHandler() {
            @Override
            public void onValueChange(ValueChangeEvent event) {
                Object eventValue = event.getValue();
                try {
                    setValue(parser.parse((CharSequence) eventValue));
                } catch (ParseException e) {
                    if (textPicture == null || !textPicture.equals(eventValue)) {
                        addExceptionStyle();
                    }
                }
            }
        });
    }

    @Override
    public String getText() {
        return isEqualsTextPicture(super.getText()) ? "" : super.getText();
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
                    ValueChangeEvent.fire(source, getText());
                }
            });
        } else
            maskListener.setMask(mask);

        textPicture = getTextPicture();
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

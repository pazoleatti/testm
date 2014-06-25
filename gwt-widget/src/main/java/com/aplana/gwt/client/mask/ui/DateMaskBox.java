package com.aplana.gwt.client.mask.ui;

import com.aplana.gwt.client.mask.MaskBox;
import com.aplana.gwt.client.mask.parser.DMYDateParser;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.client.DateTimeFormatRenderer;
import com.google.gwt.text.shared.Parser;

import java.util.Date;

/**
 * Основной класс для виджета для ввода даты по маске
 * Класс с доступный только наследникам конструктором
 *
 * @author aivanov
 */
public class DateMaskBox extends MaskBox<Date> {

    private DateTimeFormat format;
    private boolean mayBeNull = false;

    public DateMaskBox() {
        this(DMYDateParser.formatDMY, DMYDateParser.instanceDMY(), "99.99.9999");
    }

    protected DateMaskBox(DateTimeFormat dateFormat, Parser<Date> dateParser, String mask) {
        super(new DateTimeFormatRenderer(dateFormat), dateParser);
        this.format = dateFormat;
        setMask(mask);
    }

    @Override
    public Date getValue() {
        return super.getValue();
    }

    /**
     * В отличии от метода getValue() может вернуть null при некорректной дате.
     * Использовать можно там, где требуется проверка заполненности обязательных полей.
     * Метод getValue() возвращает последнее корректное значение.
     * @return Date - корректную дату, либо null
     */
    public Date getRawValue() {
        return super.getValue();
    }

    @Override
    public void setValue(Date value) {
        this.setValue(value, false);
    }

    @Override
    public void setValue(Date value, boolean fireEvents) {
        super.setValue(value, false);
        setText(value == null ? getTextPicture() : format.format(value));
        if (value == null && !mayBeNull) {
            addExceptionStyle();
        } else {
            removeExceptionStyle();
        }
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }

    }

    public boolean isMayBeNull() {
        return mayBeNull;
    }

    public void setMayBeNull(boolean mayBeNull) {
        this.mayBeNull = mayBeNull;
    }
}

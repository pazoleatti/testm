package com.aplana.gwt.client.mask;

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
public abstract class DateMaskBoxAbstract extends MaskBox<Date> {

    private Date deferredValue;
    private DateTimeFormat format;
    private boolean mayBeNull = false;

    protected DateMaskBoxAbstract(DateTimeFormat dateFormat, Parser<Date> dateParser, String mask) {
        super(new DateTimeFormatRenderer(dateFormat), dateParser);
        this.format = dateFormat;
        setMask(mask);
    }

    @Override
    public Date getValue() {
        if (!checkValue(super.getValue())) {
            return deferredValue;
        }
        return super.getValue();
    }


    @Override
    public void setValue(Date value) {
        this.setValue(value, false);
    }

    @Override
    public void setValue(Date value, boolean fireEvents) {
        if (checkValue(value)) {
            super.setValue(value, false);
            setText(value == null ? getTextPicture() : format.format(value));
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
    private boolean checkValue(Date value) {
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

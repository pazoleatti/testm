package com.aplana.gwt.client.mask;

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

    private Date value;

    private Date deferredValue;

    private DateTimeFormat format;

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
        return value;
    }


    @Override
    public void setValue(Date value) {
        this.value = value;
        if (checkValue(value)) {
            setText(format.format(value));
        }
    }

    /**
     * Проверить что дата не пуста и после заполнить удачное значение
     *
     * @param value дата
     * @return если null то вернет false
     */
    private boolean checkValue(Date value) {
        if (value == null) {
            return false;
        } else {
            deferredValue = value;
            return true;
        }
    }

}

package com.aplana.gwt.client.mask;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.client.DateTimeFormatRenderer;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ValueBox;

import java.text.ParseException;
import java.util.Date;

/**
 * Виджет для ввода даты по маске
 *
 * @author aivanov
 */
public class MaskDateBox extends MaskBox<Date> {

    private Date value;

    private Date deferredValue;

    public MaskDateBox() {
        super(Document.get().createTextInputElement(), new DateTimeFormatRenderer(DateParser.format),
                DateParser.instance());
        setMask("99.99.9999");
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
            setText(DateParser.format.format(value));
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

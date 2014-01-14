package com.aplana.gwt.client.mask;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.shared.Parser;

import java.text.ParseException;
import java.util.Date;

public class DateParser implements Parser<Date> {

    private static DateParser INSTANCE;

    public static final DateTimeFormat format = DateTimeFormat.getFormat("dd.MM.yyyy");

    public static Parser<Date> instance() {
        if (INSTANCE == null) {
            INSTANCE = new DateParser();
        }
        return INSTANCE;
    }

    protected DateParser() {
    }

    public Date parse(CharSequence object) throws ParseException {
        if ("".equals(object.toString())) {
            return null;
        }

        try {
            return format.parse(object.toString());
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }
}


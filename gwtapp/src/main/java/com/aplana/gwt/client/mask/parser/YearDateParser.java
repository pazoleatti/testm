package com.aplana.gwt.client.mask.parser;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.shared.Parser;

import java.text.ParseException;
import java.util.Date;

/**
 * Кастомный парсер даты в виде года для DateMaskBox
 *
 * @author aivanov
 */
public class YearDateParser implements Parser<Date> {

    private static YearDateParser INSTANCE;

    public static final DateTimeFormat formatY = DateTimeFormat.getFormat("yyyy");

    public static Parser<Date> instanceY() {
        if (INSTANCE == null) {
            INSTANCE = new YearDateParser();
        }
        return INSTANCE;
    }

    protected YearDateParser() {
    }

    public Date parse(CharSequence object) throws ParseException {
        if ("".equals(object.toString())) {
            return null;
        }

        try {
            return formatY.parseStrict(object.toString());
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }
}


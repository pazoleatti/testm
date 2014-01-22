package com.aplana.gwt.client.mask.parser;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.shared.Parser;

import java.text.ParseException;
import java.util.Date;

/**
 * Кастомный парсер даты в виде месяца и года для DateMaskBox
 *
 * @author aivanov
 */
public class MonthYearDateParser implements Parser<Date> {

    private static MonthYearDateParser INSTANCE;

    public static final DateTimeFormat formatMY = DateTimeFormat.getFormat("MM.yyyy");

    public static Parser<Date> instanceMY() {
        if (INSTANCE == null) {
            INSTANCE = new MonthYearDateParser();
        }
        return INSTANCE;
    }

    protected MonthYearDateParser() {
    }

    public Date parse(CharSequence object) throws ParseException {
        if ("".equals(object.toString())) {
            return null;
        }

        try {
            return formatMY.parseStrict(object.toString());
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }
}


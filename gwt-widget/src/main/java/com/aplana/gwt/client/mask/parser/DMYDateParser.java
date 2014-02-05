package com.aplana.gwt.client.mask.parser;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.shared.Parser;

import java.text.ParseException;
import java.util.Date;

/**
 * Кастомный парсер даты в виде дня месяца и года для DateMaskBox
 *
 * @author aivanov
 */
public class DMYDateParser implements Parser<Date> {

    private static DMYDateParser instance;

    public static final DateTimeFormat formatDMY = DateTimeFormat.getFormat("dd.MM.yyyy");

    public static Parser<Date> instanceDMY() {
        if (instance == null) {
            instance = new DMYDateParser();
        }
        return instance;
    }

    protected DMYDateParser() {
    }

    public Date parse(CharSequence object) throws ParseException {
        if ("".equals(object.toString())) {
            return null;
        }

        try {
            return formatDMY.parseStrict(object.toString());
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }
}


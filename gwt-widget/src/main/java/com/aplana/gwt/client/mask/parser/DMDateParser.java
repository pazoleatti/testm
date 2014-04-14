package com.aplana.gwt.client.mask.parser;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.shared.Parser;

import java.text.ParseException;
import java.util.Date;

/**
 * Кастомный парсер даты в виде дня месяца для DayMonthMaskBox
 *
 * @author vpetrov
 */
public class DMDateParser implements Parser<Date> {

    private static DMDateParser instance;

    public static final DateTimeFormat formatDM = DateTimeFormat.getFormat("dd.MM");
    public static final DateTimeFormat formatDMY = DateTimeFormat.getFormat("dd.MM.yyyy");


    public static Parser<Date> instanceDM() {
        if (instance == null) {
            instance = new DMDateParser();
        }
        return instance;
    }

    protected DMDateParser() {
    }

    public Date parse(CharSequence object) throws ParseException {
        if ("".equals(object.toString())) {
            return null;
        }

        try {
            return formatDMY.parseStrict(object.toString()+".1970");
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }
}
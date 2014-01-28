package com.aplana.gwt.client.mask.parser;

import com.aplana.gwt.client.mask.MaskUtils;
import com.google.gwt.text.shared.Parser;

import java.text.ParseException;

/**
 * Кастомный парсер даты в виде дня месяца и года для DateMaskBox
 *
 * @author aivanov
 */
public class TextMaskParser implements Parser<String> {

    private static TextMaskParser INSTANCE;
    private String mask;
    private String maskPicture;

    /**
     * Returns the instance of the no-op renderer.
     */
    public static Parser<String> instance(String mask) {
        if (INSTANCE == null || (INSTANCE.mask != null && !INSTANCE.mask.equals(mask))) {
            INSTANCE = new TextMaskParser(mask);
        }
        return INSTANCE;
    }

    protected TextMaskParser(String mask) {
        this.mask = mask;
        this.maskPicture = MaskUtils.createMaskPicture(mask);
    }

    public String parse(CharSequence object) throws ParseException {
        String obj = object.toString();
        try {
            if (obj.length() == mask.length()) {
                char[] objCharArray = obj.toCharArray();
                char[] picCharArray = maskPicture.toCharArray();
                if (!obj.equals(maskPicture)) {
                    for (int i = 0; i < objCharArray.length; i++) {
                        if (objCharArray[i] == picCharArray[i]) {
                            throw new ParseException("Значение имеет недопустимые символы!", i);
                        }

                    }
                }
            }
        } catch (NullPointerException e) {
            throw new ParseException(e.getMessage(), 0);
        }
        return obj;
    }
}


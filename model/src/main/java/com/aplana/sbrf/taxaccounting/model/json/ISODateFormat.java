package com.aplana.sbrf.taxaccounting.model.json;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.FieldPosition;
import java.util.Date;

/**
 * Переопределние StdDateFormat (используется по-умолчанию)
 * parse остаётся, т.к. он гибкий (парсит и "2000-01-01" и "2000-01-01T00:00:00.000+0000 и др"),
 * а format переопределен, чтобы не передавались таймзоны на клиент
 */
public class ISODateFormat extends StdDateFormat {

    private FastDateFormat formatter = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss");

    public ISODateFormat() {
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        return formatter.format(date, toAppendTo, fieldPosition);
    }

    // для GWT
    @Override
    public int hashCode()
    {
        return formatter.hashCode();
    }
}

package com.aplana.sbrf.taxaccounting.utils;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * @author Fail Mukhametdinov
 */
public class SimpleDateUtilsTest {

    /**
     * Проверка разницы дней между календарными датами, учитывая границы суток
     *
     * @throws Exception
     */
    @Test
    public void testDaysBetween() throws Exception {
        Calendar c = Calendar.getInstance();
        c.set(2016, Calendar.JANUARY, 1, 23, 59, 59);
        Date before = c.getTime();
        c.set(2016, Calendar.JANUARY, 2, 0, 0, 0);
        Date after = c.getTime();
        int daysBetween = SimpleDateUtils.daysBetween(before, after);
        assertEquals(1, daysBetween);
    }
}
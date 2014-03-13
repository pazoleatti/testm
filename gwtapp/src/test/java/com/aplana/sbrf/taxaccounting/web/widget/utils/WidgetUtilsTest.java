package com.aplana.sbrf.taxaccounting.web.widget.utils;

import org.junit.Assert;

import java.util.Date;

/**
 * @author aivanov
 */
public class WidgetUtilsTest {

    //@Test
    public void testIsNotCorrectDate() throws Exception {
        Date start = new Date(1111, 11, 11);
        Date end = new Date(1111, 11, 21);
        Date current = new Date(1111, 11, 15);
        Date current2 = new Date(1111, 11, 22);
        Date current3 = new Date(1111, 11, 10);

        // current в периоде
        Assert.assertTrue("not true: start, end, current", WidgetUtils.isInLimitPeriod(start, end, current));
        Assert.assertTrue("not true: null, null, current", WidgetUtils.isInLimitPeriod(null, null, current));
        Assert.assertTrue("not true: null, end, current", WidgetUtils.isInLimitPeriod(null, end, current));
        Assert.assertTrue("not true: start, null, current", WidgetUtils.isInLimitPeriod(start, null, current));
        Assert.assertTrue("not true: start, end, start", WidgetUtils.isInLimitPeriod(start, end, start));
        Assert.assertTrue("not true: start, end, end", WidgetUtils.isInLimitPeriod(start, end, end));

        // не в периоде
        Assert.assertFalse("not false: start, end, current2", WidgetUtils.isInLimitPeriod(start, end, current2));
        Assert.assertFalse("not false: start, end, current3", WidgetUtils.isInLimitPeriod(start, end, current3));
        Assert.assertFalse("not false: start, end, null", WidgetUtils.isInLimitPeriod(start, end, null));
        Assert.assertFalse("not false: start, end, null", WidgetUtils.isInLimitPeriod(null, end, null));
        Assert.assertFalse("not false: start, end, null", WidgetUtils.isInLimitPeriod(start, null, null));
        Assert.assertFalse("not false: null, null, null", WidgetUtils.isInLimitPeriod(null, null, null));

    }

    //@Test
    public void testItWasChange() throws Exception {
        Object o1 = new Object();
        Object o2 = new Object();
        Date date1 = new Date(1111, 11, 11);
        Date date2 = new Date(1111, 11, 12);

        Assert.assertFalse(WidgetUtils.isWasChange(o1, o1));
        Assert.assertFalse(WidgetUtils.isWasChange(null, null));
        Assert.assertTrue(WidgetUtils.isWasChange(o1, o2));
        Assert.assertTrue(WidgetUtils.isWasChange(null, o1));
        Assert.assertTrue(WidgetUtils.isWasChange(o1, null));
        Assert.assertTrue(WidgetUtils.isWasChange(o2, o1));

        Assert.assertFalse(WidgetUtils.isWasChange(date1, date1));
        Assert.assertTrue(WidgetUtils.isWasChange(date1, date2));
        Assert.assertTrue(WidgetUtils.isWasChange(date2, date1));
        Assert.assertTrue(WidgetUtils.isWasChange(null, date1));
        Assert.assertTrue(WidgetUtils.isWasChange(date1, null));

    }

}

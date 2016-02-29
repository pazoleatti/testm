package com.aplana.sbrf.taxaccounting.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Dmitriy Levykin
 */
public class TransportDataParamTest {

    private static String INVALID_NAME_1 = "__1111-25______99_11702_03212014_.rnu";
    private static String INVALID_NAME_2 = "____852-4______________147212014";

    private static String VALID_NAME_1 = "____852-4___99_11702_02_02212014.rnu";
    private static String VALID_NAME_2 = "1290-40.1_______49_0000_00222015_6.rNu";

    // Несоответствие формата
    @Test(expected = IllegalArgumentException.class)
    public void valueOf1Test() {
        TransportDataParam.valueOf(INVALID_NAME_1);
    }

    // Несоответствие расширения
    @Test(expected = IllegalArgumentException.class)
    public void valueOf2Test() {
        TransportDataParam.valueOf(INVALID_NAME_2);
    }

    @Test
    public void valueOf3Test() {
        TransportDataParam transportDataParam = TransportDataParam.valueOf(VALID_NAME_1);
        Assert.assertEquals("852-4", transportDataParam.getFormCode());
        Assert.assertEquals("99_11702_02_02", transportDataParam.getDepartmentCode());
        Assert.assertEquals("21", transportDataParam.getReportPeriodCode());
        Assert.assertEquals(Integer.valueOf(2014), transportDataParam.getYear());
        Assert.assertNull(transportDataParam.getMonth());
    }

    @Test
    public void valueOf4Test() {
        TransportDataParam transportDataParam = TransportDataParam.valueOf(VALID_NAME_2);
        Assert.assertEquals("1290-40.1", transportDataParam.getFormCode());
        Assert.assertEquals("49_0000_00", transportDataParam.getDepartmentCode());
        Assert.assertEquals("22", transportDataParam.getReportPeriodCode());
        Assert.assertEquals(Integer.valueOf(2015), transportDataParam.getYear());
        Assert.assertEquals(Integer.valueOf(6), transportDataParam.getMonth());
    }
}


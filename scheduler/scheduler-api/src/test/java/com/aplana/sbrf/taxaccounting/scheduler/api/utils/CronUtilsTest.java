package com.aplana.sbrf.taxaccounting.scheduler.api.utils;

import junit.framework.Assert;
import org.junit.Test;

public class CronUtilsTest {
    @Test
    public void assembleIbmCronExpressionTest() {
        String cron = "* * * * *";
        String newCron = CronUtils.assembleIbmCronExpression(cron);
        Assert.assertEquals(newCron, "0 * * * * ?");

        cron = "15 1 1 1 *";
        newCron = CronUtils.assembleIbmCronExpression(cron);
        Assert.assertEquals(newCron, "0 15 1 1 JAN ?");

        cron = "15 1 * 1 1";
        newCron = CronUtils.assembleIbmCronExpression(cron);
        Assert.assertEquals(newCron, "0 15 1 ? JAN TUE");
    }

    @Test
    public void getBaseCronTest() {
        String ibmCron = "0 25 * * * ?";
        Assert.assertEquals(CronUtils.getBaseCron(ibmCron), "25 * * * ?");
    }
}

package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.GregorianCalendar;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DepartmentReportPeriodServiceTest {
    DepartmentReportPeriodService departmentReportPeriodService = new DepartmentReportPeriodServiceImpl();

    @Test
    public void getPrevDepartmentReportPeriodTest() {
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setId(1);

        DepartmentReportPeriod departmentReportPeriod1 = new DepartmentReportPeriod();
        departmentReportPeriod1.setId(1);
        departmentReportPeriod1.setReportPeriod(reportPeriod);
        departmentReportPeriod1.setDepartmentId(1);
        departmentReportPeriod1.setCorrectionDate(new GregorianCalendar(2014, 3, 3).getTime());

        DepartmentReportPeriod departmentReportPeriod2 = new DepartmentReportPeriod();
        departmentReportPeriod2.setId(2);
        departmentReportPeriod2.setReportPeriod(reportPeriod);
        departmentReportPeriod2.setDepartmentId(1);
        departmentReportPeriod2.setCorrectionDate(new GregorianCalendar(2014, 2, 2).getTime());

        DepartmentReportPeriod departmentReportPeriod3 = new DepartmentReportPeriod();
        departmentReportPeriod3.setId(3);
        departmentReportPeriod3.setReportPeriod(reportPeriod);
        departmentReportPeriod3.setDepartmentId(1);
        departmentReportPeriod2.setCorrectionDate(new GregorianCalendar(2014, 1, 1).getTime());

        DepartmentReportPeriod departmentReportPeriod4 = new DepartmentReportPeriod();
        departmentReportPeriod4.setId(4);
        departmentReportPeriod4.setReportPeriod(reportPeriod);
        departmentReportPeriod4.setDepartmentId(1);

        DepartmentReportPeriodDao departmentReportPeriodDao = mock(DepartmentReportPeriodDao.class);
        ReflectionTestUtils.setField(departmentReportPeriodService, "departmentReportPeriodDao",
                departmentReportPeriodDao);
        when(departmentReportPeriodDao.getListByFilter(any(DepartmentReportPeriodFilter.class))).thenReturn(
                Arrays.asList(departmentReportPeriod1, departmentReportPeriod2, departmentReportPeriod3,
                        departmentReportPeriod4));

        DepartmentReportPeriod prevDepartmentReportPeriod =
                departmentReportPeriodService.getPrevDepartmentReportPeriod(departmentReportPeriod1);

        Assert.assertEquals(departmentReportPeriod2.getId(), prevDepartmentReportPeriod.getId());
    }
}

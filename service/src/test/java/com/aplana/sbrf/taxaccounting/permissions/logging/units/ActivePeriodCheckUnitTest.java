package com.aplana.sbrf.taxaccounting.permissions.logging.units;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("CheckUnitTest.xml")
public class ActivePeriodCheckUnitTest {

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private CheckUnit activePeriodCheckUnit;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private Logger logger;

    @Autowired
    private DeclarationData declarationData;

    @Autowired
    private DepartmentReportPeriod departmentReportPeriod;

    @Autowired
    private TAUserInfo userInfo;

    @Autowired
    private ReportPeriod reportPeriod;

    @Autowired
    private TaxPeriod taxPeriod;

    @Autowired
    private Department department;

    @Test
    public void test_check_period_is_active() {

        when(declarationData.getDepartmentReportPeriodId()).thenReturn(1);
        when(departmentReportPeriod.isActive()).thenReturn(true);
        when(departmentReportPeriodService.fetchOne(any(Integer.class))).thenReturn(departmentReportPeriod);

        assertTrue(activePeriodCheckUnit.check(logger, userInfo, declarationData, null));
    }

    @Test
    public void test_check_period_is_not_active() {

        when(declarationData.getDepartmentReportPeriodId()).thenReturn(1);
        when(departmentReportPeriod.isActive()).thenReturn(false);
        when(departmentReportPeriodService.fetchOne(any(Integer.class))).thenReturn(departmentReportPeriod);
        when(departmentReportPeriod.getReportPeriod()).thenReturn(reportPeriod);
        when(reportPeriod.getTaxPeriod()).thenReturn(taxPeriod);
        when(departmentService.getDepartment(anyInt())).thenReturn(department);

        assertFalse(activePeriodCheckUnit.check(logger, userInfo, declarationData, null));
    }

}
package com.aplana.sbrf.taxaccounting.permissions.logging.units;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("CheckUnitTest.xml")
public class IdentifyCredentialsCheckUnitTest {

    @Autowired
    private CheckUnit identifyCredentialsCheckUnit;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private DeclarationData declarationData;

    @Autowired
    private Department department;

    @Autowired
    private DepartmentReportPeriod departmentReportPeriod;

    @Autowired
    private Logger logger;

    @Autowired
    private ReportPeriod reportPeriod;

    @Autowired
    private TAUser taUser;

    @Autowired
    private TAUserInfo userInfo;

    @Autowired
    private TaxPeriod taxPeriod;

    @Test
    public void test_check_success() {
        when(userInfo.getUser()).thenReturn(taUser);
        when(taUser.hasRole(TARole.N_ROLE_CONTROL_UNP)).thenReturn(true);
        when(taUser.hasRoles(Matchers.<String>anyVararg())).thenReturn(true);

        assertTrue(identifyCredentialsCheckUnit.check(logger, userInfo, declarationData, null));
    }

    @Test
    public void test_check_fail() {
        when(userInfo.getUser()).thenReturn(taUser);
        when(taUser.hasRole(anyString())).thenReturn(false);
        when(taUser.hasRoles(Matchers.<String>anyVararg())).thenReturn(false);

        when(departmentReportPeriodService.fetchOne(anyInt())).thenReturn(departmentReportPeriod);
        when(departmentService.getDepartment(anyInt())).thenReturn(department);
        when(departmentReportPeriod.getReportPeriod()).thenReturn(reportPeriod);
        when(reportPeriod.getTaxPeriod()).thenReturn(taxPeriod);

        assertFalse(identifyCredentialsCheckUnit.check(logger, userInfo, declarationData, null));
    }

}
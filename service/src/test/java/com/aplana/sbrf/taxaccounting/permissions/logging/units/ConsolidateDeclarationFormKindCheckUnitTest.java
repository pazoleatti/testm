package com.aplana.sbrf.taxaccounting.permissions.logging.units;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("CheckUnitTest.xml")
public class ConsolidateDeclarationFormKindCheckUnitTest {

    @Autowired
    private CheckUnit consolidateDeclarationFormKindCheckUnit;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private DeclarationData declarationData;

    @Autowired
    private DeclarationTemplate declarationTemplate;

    @Autowired
    private Department department;

    @Autowired
    private DepartmentReportPeriod departmentReportPeriod;

    @Autowired
    private Logger logger;

    @Autowired
    private ReportPeriod reportPeriod;

    @Autowired
    private TAUserInfo taUserInfo;

    @Autowired
    private TaxPeriod taxPeriod;

    @Test
    public void test_check_success() {
        when(declarationTemplate.getDeclarationFormKind()).thenReturn(DeclarationFormKind.CONSOLIDATED);
        when(declarationTemplateService.get(anyInt())).thenReturn(declarationTemplate);

        assertTrue(consolidateDeclarationFormKindCheckUnit.check(logger, taUserInfo, declarationData, null));
    }

    @Test
    public void test_check_fail() {
        when(declarationTemplate.getDeclarationFormKind()).thenReturn(DeclarationFormKind.PRIMARY);
        when(declarationTemplateService.get(anyInt())).thenReturn(declarationTemplate);
        when(departmentReportPeriodService.fetchOne(anyInt())).thenReturn(departmentReportPeriod);
        when(departmentService.getDepartment(anyInt())).thenReturn(department);
        when(departmentReportPeriod.getReportPeriod()).thenReturn(reportPeriod);
        when(reportPeriod.getTaxPeriod()).thenReturn(taxPeriod);

        assertFalse(consolidateDeclarationFormKindCheckUnit.check(logger, taUserInfo, declarationData, null));
    }
}
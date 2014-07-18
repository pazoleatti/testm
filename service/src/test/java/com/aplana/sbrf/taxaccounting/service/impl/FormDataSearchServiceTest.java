package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.test.DepartmentFormTypeMockUtils.mockDepartmentFormType;
import static com.aplana.sbrf.taxaccounting.test.UserMockUtils.mockUser;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("FormDataSearchServiceTest.xml")
public class FormDataSearchServiceTest {

    @Autowired
	private FormDataSearchService formDataSearchService;

    @Autowired
    private FormTypeService formTypeService;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    private DepartmentFormTypeDao departmentFormTypeDao;

    @Autowired
    private FormDataAccessService formDataAccessService;
    @Autowired
    private FormTemplateService formTemplateService;
    @Autowired
    private PeriodService periodService;

	private final List<FormType> FORM_TYPES_BY_TAX_TYPE = new ArrayList<FormType>();
	
	private static final int CONTROL_UNP_USER_ID = 1;
	private static final int CONTROL_USER_ID = 2;
	private static final int OPERATOR_USER_ID = 3;

    private int FORM_TEMPLATE_ID_1 = 1;
    private int FORM_TEMPLATE_ID_2 = 2;
    private int FORM_TEMPLATE_ID_3 = 3;

	
	@Before
	public void tearUp() {

        TemplateFilter filter = new TemplateFilter();
        filter.setTaxType(TaxType.TRANSPORT);
        when(formTypeService.getByFilter(filter)).thenReturn(FORM_TYPES_BY_TAX_TYPE);
        filter.setTaxType(TaxType.INCOME);

        ArrayList<FormType> formTypes = new ArrayList<FormType>();
        FormType type1 = new FormType();
        type1.setId(1);
        FormType type2 = new FormType();
        type2.setId(2);
        FormType type3 = new FormType();
        type3.setId(3);
        formTypes.add(type1);
        formTypes.add(type2);
        formTypes.add(type3);
        when(formTypeService.getByFilter(filter)).thenReturn(formTypes);
        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, Calendar.JANUARY, 1);
        FormTemplate formTemplate1 = new FormTemplate();
        formTemplate1.setId(FORM_TEMPLATE_ID_1);
        formTemplate1.setVersion(calendar.getTime());
        calendar.set(2013, Calendar.JANUARY, 1);
        FormTemplate formTemplate2 = new FormTemplate();
        formTemplate2.setId(FORM_TEMPLATE_ID_2);
        formTemplate2.setVersion(calendar.getTime());
        calendar.set(2014, Calendar.JANUARY, 1);
        FormTemplate formTemplate3 = new FormTemplate();
        formTemplate3.setId(FORM_TEMPLATE_ID_3);
        formTemplate3.setVersion(calendar.getTime());
        when(formTemplateService.getFormTemplateVersionsByStatus(formTemplate1.getId(), VersionedObjectStatus.NORMAL)).thenReturn(asList(formTemplate1));
        when(formTemplateService.getFormTemplateVersionsByStatus(formTemplate2.getId(), VersionedObjectStatus.NORMAL)).thenReturn(asList(formTemplate2));
        when(formTemplateService.getFormTemplateVersionsByStatus(formTemplate3.getId(), VersionedObjectStatus.NORMAL)).thenReturn(asList(formTemplate3));
        calendar.set(2012, Calendar.DECEMBER, 31);
        when(formTemplateService.getFTEndDate(formTemplate1.getId())).thenReturn(calendar.getTime());
        calendar.set(2013, Calendar.DECEMBER, 31);
        when(formTemplateService.getFTEndDate(formTemplate2.getId())).thenReturn(calendar.getTime());
        calendar.set(2014, Calendar.DECEMBER, 31);
        when(formTemplateService.getFTEndDate(formTemplate3.getId())).thenReturn(calendar.getTime());
        ReportPeriod reportPeriod = new ReportPeriod();
        calendar.set(2037, Calendar.JULY, 1);
        reportPeriod.setStartDate(calendar.getTime());
        calendar.set(2037, Calendar.NOVEMBER, 1);
        reportPeriod.setEndDate(calendar.getTime());
        when(periodService.getReportPeriod(1)).thenReturn(reportPeriod);

        when(departmentService.getTaxFormDepartments(any(TAUser.class), anyListOf(TaxType.class), any(Date.class), any(Date.class))).thenReturn(asList(1, 2, 3));
        when(formDataAccessService.getAvailableFormDataKind(any(TAUserInfo.class), anyListOf(TaxType.class))).thenReturn(asList(FormDataKind.values()));
		
		List<DepartmentFormType> dfts = new ArrayList<DepartmentFormType>();
		dfts.add(mockDepartmentFormType(1, 1, FormDataKind.SUMMARY));
		dfts.add(mockDepartmentFormType(1, 2, FormDataKind.SUMMARY));
		when(departmentFormTypeDao.getByTaxType(1, TaxType.TRANSPORT, null, null)).thenReturn(dfts);
		
		dfts = new ArrayList<DepartmentFormType>();
		dfts.add(mockDepartmentFormType(2, 3, FormDataKind.SUMMARY));
		dfts.add(mockDepartmentFormType(3, 2, FormDataKind.SUMMARY));
        dfts.add(mockDepartmentFormType(1, 2, FormDataKind.PRIMARY));
		when(departmentFormTypeDao.getDepartmentSources(1, TaxType.TRANSPORT, null, null)).thenReturn(dfts);
	}

	@Test
	public void testGetAvailableFilterValuesForControlUnp() {

		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setUser(mockUser(CONTROL_UNP_USER_ID, 1, TARole.ROLE_CONTROL_UNP));
		FormDataFilterAvailableValues values = formDataSearchService.getAvailableFilterValues(userInfo, TaxType.TRANSPORT);
        assertEquals(values.getFormTypes().size(), FORM_TYPES_BY_TAX_TYPE.size());
		assertEquals(6, values.getKinds().size());
        assertTrue(values.getKinds().containsAll(asList(FormDataKind.PRIMARY, FormDataKind.CONSOLIDATED,
                FormDataKind.SUMMARY, null)));
        assertEquals(3, values.getDepartmentIds().size());
        assertTrue(values.getDepartmentIds().containsAll(asList(1, 2, 3)));
	}

	@Test
	public void testGetAvailableFilterValuesForOperator() {
        try {
            TAUserInfo userInfo = new TAUserInfo();
            userInfo.setUser(mockUser(OPERATOR_USER_ID, 1, TARole.ROLE_OPER));
            FormDataFilterAvailableValues values = formDataSearchService.getAvailableFilterValues(userInfo, TaxType.TRANSPORT);
            assertEquals(0, values.getFormTypes().size());
            assertEquals(6, values.getKinds().size());
            assertTrue(values.getKinds().containsAll(asList(FormDataKind.PRIMARY, null)));
            /*assertFalse(values.getKinds().contains(FormDataKind.SUMMARY));
            assertFalse(values.getKinds().contains(FormDataKind.CONSOLIDATED));*/
            assertEquals(3, values.getDepartmentIds().size());
            assertTrue(values.getDepartmentIds().containsAll(asList(1, 2, 3)));
            assertTrue(values.getDepartmentIds().contains(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	@Test
	public void testGetAvailableFilterValuesForControl() {
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setUser(mockUser(CONTROL_USER_ID, 1, TARole.ROLE_CONTROL));
		FormDataFilterAvailableValues values = formDataSearchService.getAvailableFilterValues(userInfo, TaxType.TRANSPORT);
		assertEquals(0, values.getFormTypes().size());
		assertEquals(6, values.getKinds().size());
        assertTrue(values.getKinds().containsAll(asList(FormDataKind.PRIMARY, FormDataKind.CONSOLIDATED,
                FormDataKind.SUMMARY, null)));
		assertEquals(3, values.getDepartmentIds().size());
        assertTrue(values.getDepartmentIds().containsAll(asList(1, 2, 3)));
	}

    @Test
    public void testGetActiveFormTypeInReportPeriod() {
        //List<FormType> formTypes = formDataSearchService.getActiveFormTypeInReportPeriod(1, 1);
        //assertEquals(0, formTypes.size());
    }
}

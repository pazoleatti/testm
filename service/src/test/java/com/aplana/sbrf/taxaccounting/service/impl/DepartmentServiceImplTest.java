package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;


public class DepartmentServiceImplTest {

    private static DepartmentService departmentService;
    private static DepartmentDao departmentDao;
    private static DepartmentReportPeriodDao departmentReportPeriodDao;

    private static ArrayList<TARole> taRoles;
    private static Department root;
    private static Department departmentTB2;
    private static Department departmentTB3;
    private static Department departmentGOSB31;
    private static Department departmentOSB311;
    private static int[] departmentID = new int[]{0, 2, 3, 31, 311};

    private final static int DEPARTMENT_TB1_ID = 1;

    @Before
    public void init() {
        departmentService = new DepartmentServiceImpl();

        departmentDao = mock(DepartmentDao.class);
        ReflectionTestUtils.setField(departmentService, "departmentDao", departmentDao);

        root = new Department();
        root.setName("Bank");
        root.setId(departmentID[0]);
        root.setType(DepartmentType.ROOT_BANK);

        departmentTB2 = new Department();
        departmentTB2.setName("TB2");
        departmentTB2.setId(departmentID[1]);
        departmentTB2.setType(DepartmentType.TERBANK);

        departmentTB3 = new Department();
        departmentTB3.setName("TB3");
        departmentTB3.setId(departmentID[2]);
        departmentTB3.setType(DepartmentType.TERBANK);

        departmentGOSB31 = new Department();
        departmentGOSB31.setName("GOSB31");
        departmentGOSB31.setId(departmentID[3]);
        departmentGOSB31.setType(DepartmentType.GOSB);

        departmentOSB311 = new Department();
        departmentOSB311.setName("OSB311");
        departmentOSB311.setId(departmentID[4]);
        departmentOSB311.setType(DepartmentType.OSB);

        // TODO data for исполнители

        when(departmentDao.getDepartment(departmentID[0])).thenReturn(root);
        when(departmentDao.getDepartment(departmentID[1])).thenReturn(departmentTB2);
        when(departmentDao.getDepartment(departmentID[2])).thenReturn(departmentTB3);
        when(departmentDao.getDepartment(departmentID[3])).thenReturn(departmentGOSB31);
        when(departmentDao.getDepartment(departmentID[4])).thenReturn(departmentOSB311);

        when(departmentDao.getParent(departmentID[1])).thenReturn(root);
        when(departmentDao.getParent(departmentID[2])).thenReturn(root);
        when(departmentDao.getParent(departmentID[3])).thenReturn(departmentTB3);
        when(departmentDao.getParent(departmentID[4])).thenReturn(departmentGOSB31);

        ArrayList<Department> departments = new ArrayList<Department>();

        departments.add(departmentTB3);
        departments.add(departmentGOSB31);
        departments.add(departmentOSB311);
        when(departmentDao.getAllChildren(departmentID[2])).thenReturn(departments);
        departments = new ArrayList<Department>();
        departments.add(departmentTB2);
        when(departmentDao.getAllChildren(departmentID[1])).thenReturn(departments);
        departments = new ArrayList<Department>();
        departments.add(root);
        departments.add(departmentTB2);
        departments.add(departmentTB3);
        departments.add(departmentGOSB31);
        departments.add(departmentOSB311);
        when(departmentDao.getAllChildren(departmentID[0])).thenReturn(departments);

        when(departmentDao.listDepartments()).thenReturn(departments);

        departments = new ArrayList<Department>();
        departments.add(root);
        when(departmentDao.getDepartmentsByType(DepartmentType.ROOT_BANK.getCode())).thenReturn(departments);
        departments = new ArrayList<Department>();
        departments.add(departmentTB2);
        departments.add(departmentTB3);
        when(departmentDao.getDepartmentsByType(DepartmentType.TERBANK.getCode())).thenReturn(departments);
        departments = new ArrayList<Department>();
        departments.add(departmentGOSB31);
        when(departmentDao.getDepartmentsByType(DepartmentType.GOSB.getCode())).thenReturn(departments);
        departments = new ArrayList<Department>();
        departments.add(departmentOSB311);
        when(departmentDao.getDepartmentsByType(DepartmentType.OSB.getCode())).thenReturn(departments);

        taRoles = new ArrayList<TARole>();
        TARole taRole = new TARole();
        taRole.setAlias(TARole.ROLE_CONTROL_UNP);
        taRoles.add(taRole);
        taRole = new TARole();
        taRole.setAlias(TARole.ROLE_CONTROL_NS);
        taRoles.add(taRole);
        taRole = new TARole();
        taRole.setAlias(TARole.ROLE_CONTROL);
        taRoles.add(taRole);
        taRole = new TARole();
        taRole.setAlias(TARole.ROLE_OPER);
        taRoles.add(taRole);

        departmentReportPeriodDao = mock(DepartmentReportPeriodDao.class);
        ReflectionTestUtils.setField(departmentService, "departmentReportPeriodDao", departmentReportPeriodDao);
        DepartmentReportPeriod drpOpen = new DepartmentReportPeriod();
        drpOpen.setActive(true);
        DepartmentReportPeriod drpClose = new DepartmentReportPeriod();
        drpClose.setActive(false);
        when(departmentReportPeriodDao.get(0, Long.valueOf(departmentID[0]))).thenReturn(drpOpen);
        when(departmentReportPeriodDao.get(0, Long.valueOf(departmentID[1]))).thenReturn(drpOpen);
        when(departmentReportPeriodDao.get(0, Long.valueOf(departmentID[2]))).thenReturn(drpClose);
        when(departmentReportPeriodDao.get(0, Long.valueOf(departmentID[3]))).thenReturn(drpClose);
        when(departmentReportPeriodDao.get(0, Long.valueOf(departmentID[4]))).thenReturn(drpOpen);
        when(departmentReportPeriodDao.get(1, Long.valueOf(departmentID[4]))).thenReturn(drpClose);
    }

    @Test
    public void testDepDao() {
        Department department = new Department();
        DepartmentDao departmentDao = mock(DepartmentDao.class);
        List<Department> listDep = new ArrayList<Department>();
        listDep.add(new Department());
        listDep.add(new Department());
        listDep.add(new Department());
        ReflectionTestUtils.setField(departmentService, "departmentDao", departmentDao);
        when(departmentDao.getChildren(DEPARTMENT_TB1_ID)).thenReturn(listDep);
        when(departmentDao.getDepartment(DEPARTMENT_TB1_ID)).thenReturn(department);
        Assert.assertEquals(3, departmentService.getChildren(DEPARTMENT_TB1_ID).size());
        Assert.assertEquals(department, departmentService.getDepartment(DEPARTMENT_TB1_ID));
    }

    @Test
    public void listAllTest() {
        departmentService.listAll();
        verify(departmentDao, times(1)).listDepartments();
    }

    @Test
    public void getParentTest() {
        DepartmentDao departmentDao = mock(DepartmentDao.class);
        ReflectionTestUtils.setField(departmentService, "departmentDao", departmentDao);
        Integer departmentId = anyInt();
        when(departmentDao.getParent(departmentId)).thenReturn(new Department());

        departmentService.getParent(departmentId);
        verify(departmentDao, times(1)).getParent(departmentId);
    }

    @Test
    public void getRequiredForTreeDepartmentsTest() {
        Set<Integer> available = new HashSet<Integer>(Arrays.asList(2, 3));

        Collection<Department> result = departmentService.getRequiredForTreeDepartments(available).values();
        verify(departmentDao, times(1)).getDepartment(2);
        verify(departmentDao, times(1)).getParent(2);
        verify(departmentDao, times(1)).getDepartment(3);
        verify(departmentDao, times(1)).getParent(3);
        Assert.assertEquals(3, result.size());
        Assert.assertEquals(true, result.contains(root));
        Assert.assertEquals(true, result.contains(departmentTB2));
        Assert.assertEquals(true, result.contains(departmentTB3));
    }

    @Test
    public void testGetBADepartments() {
        TAUser taUser = new TAUser();
        taUser.setRoles(taRoles);

        List<Department> result = departmentService.getBADepartments(taUser);
        Assert.assertEquals(5, result.size());
        Assert.assertEquals(true, result.contains(root) && result.contains(departmentTB2)
                && result.contains(departmentTB3) && result.contains(departmentGOSB31) && result.contains(departmentOSB311));

        taUser.getRoles().remove(0);

        for (int i = 0; i < departmentID.length; i++) {
            taUser.setDepartmentId(departmentID[i]);
            result = departmentService.getBADepartments(taUser);

            switch (departmentID[i]) {
                case 0:
                case 2:
                case 3:
                    Assert.assertEquals(0, result.size());
                    break;
                case 31:
                case 311:
                    Assert.assertEquals(3, result.size());
                    Assert.assertEquals(true, result.contains(departmentTB3) && result.contains(departmentGOSB31)
                            && result.contains(departmentOSB311));
                    break;
            }
        }
    }

    @Test
    public void testGetTBDepartments() {
        TAUser taUser = new TAUser();
        taUser.setRoles(taRoles);

        List<Department> result = departmentService.getTBDepartments(taUser);
        Assert.assertEquals(3, result.size());
        Assert.assertEquals(true, result.contains(root) && result.contains(departmentTB2)
                && result.contains(departmentTB3));

        taUser.getRoles().remove(0);

        for (int i = 0; i < departmentID.length; i++) {
            taUser.setDepartmentId(departmentID[i]);
            result = departmentService.getTBDepartments(taUser);

            switch (departmentID[i]) {
                case 0:
                    Assert.assertEquals(0, result.size());
                    break;
                case 2:
                case 3:
                    Assert.assertEquals(1, result.size());
                    Assert.assertEquals(true, result.contains(departmentTB2) || result.contains(departmentTB3));
                    break;
                case 31:
                case 311:
                    Assert.assertEquals(1, result.size());
                    Assert.assertEquals(true, result.contains(departmentTB3));
                    break;
            }
        }
    }

    @Test
    public void testGetBankDepartment() {
        Department result = departmentService.getBankDepartment();
        Assert.assertEquals(result, root);
    }

    @Test
    public void testGetTaxFormDepartments() {
        TAUser taUser = new TAUser();
        taUser.setRoles(taRoles);

        List<Department> result = departmentService.getTaxFormDepartments(taUser);
        Assert.assertEquals(5, result.size());
        Assert.assertEquals(true, result.contains(root) && result.contains(departmentTB2)
                && result.contains(departmentTB3) && result.contains(departmentGOSB31) && result.contains(departmentOSB311));

        // TODO
        taUser.getRoles().remove(0);
        //test for ROLE_CONTROL_NS
        taUser.getRoles().remove(0);
        // test for ROLE_CONTROL
        taUser.getRoles().remove(0);
        // test for ROLE_OPER
    }

    @Test
    public void testGetDestinationDepartments() {
        TAUser taUser = new TAUser();
        taUser.setRoles(taRoles);

        List<Department> result = departmentService.getDestinationDepartments(taUser);
        Assert.assertEquals(5, result.size());
        Assert.assertEquals(true, result.contains(root) && result.contains(departmentTB2)
                && result.contains(departmentTB3) && result.contains(departmentGOSB31) && result.contains(departmentOSB311));


        taUser.getRoles().remove(0);

        for (int i = 0; i < departmentID.length; i++) {
            taUser.setDepartmentId(departmentID[i]);
            result = departmentService.getDestinationDepartments(taUser);

            switch (departmentID[i]) {
                case 0:
                case 2:
                case 3:
                    Assert.assertEquals(1, result.size());
                    Assert.assertEquals(true, result.contains(departmentGOSB31));
                    break;
                case 31:
                case 311:
                    Assert.assertEquals(3, result.size());
                    Assert.assertEquals(true, result.contains(departmentTB3)
                            && result.contains(departmentGOSB31) && result.contains(departmentOSB311));
                    break;
            }
        }
    }

    @Test
    public void testGetPrintFormDepartments() {
        // TODO
    }

    @Test
    public void testGetOpenPeriodDepartments() {
        TAUser taUser = new TAUser();
        taUser.setRoles(taRoles);
        ReportPeriod reportPeriod = new ReportPeriod();

        reportPeriod.setId(0);
        List<Department> result = departmentService.getOpenPeriodDepartments(taUser, reportPeriod);
        Assert.assertEquals(3, result.size());
        Assert.assertEquals(true, result.contains(root) && result.contains(departmentTB2)
                && result.contains(departmentOSB311));

        reportPeriod.setId(1);
        result = departmentService.getOpenPeriodDepartments(taUser, reportPeriod);
        Assert.assertEquals(0, result.size());

        // TODO
        taUser.getRoles().remove(0);
        //test for ROLE_CONTROL_NS
        taUser.getRoles().remove(0);
        // test for ROLE_CONTROL
        taUser.getRoles().remove(0);
        // test for ROLE_OPER
    }
}

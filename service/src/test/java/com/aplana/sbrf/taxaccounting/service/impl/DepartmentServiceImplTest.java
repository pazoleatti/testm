package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static com.aplana.sbrf.taxaccounting.test.ReportPeriodMockUtils.mockReportPeriod;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class DepartmentServiceImplTest {

    private static DepartmentService departmentService;
    private static DepartmentDao departmentDao;
    private static DepartmentReportPeriodDao departmentReportPeriodDao;
    private static DepartmentDeclarationTypeDao departmentDeclarationTypeDao;
    private static DepartmentFormTypeDao departmentFormTypeDao;

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
        departmentDeclarationTypeDao = mock(DepartmentDeclarationTypeDao.class);
        departmentFormTypeDao = mock(DepartmentFormTypeDao.class);
        departmentReportPeriodDao = mock(DepartmentReportPeriodDao.class);
        PeriodService periodService = mock(PeriodService.class);

        ReflectionTestUtils.setField(departmentService, "departmentDao", departmentDao);
        ReflectionTestUtils.setField(departmentService, "departmentReportPeriodDao", departmentReportPeriodDao);
        ReflectionTestUtils.setField(departmentService, "departmentDeclarationTypeDao", departmentDeclarationTypeDao);
        ReflectionTestUtils.setField(departmentService, "departmentFormTypeDao", departmentFormTypeDao);
        ReflectionTestUtils.setField(departmentService, "periodService", periodService);

        root = new Department();
        root.setName("Bank");
        root.setId(departmentID[0]);
        root.setType(DepartmentType.ROOT_BANK);

        departmentTB2 = new Department();
        departmentTB2.setName("TB2");
        departmentTB2.setId(departmentID[1]);
        departmentTB2.setType(DepartmentType.TERR_BANK);

        departmentTB3 = new Department();
        departmentTB3.setName("TB3");
        departmentTB3.setId(departmentID[2]);
        departmentTB3.setType(DepartmentType.TERR_BANK);

        departmentGOSB31 = new Department();
        departmentGOSB31.setName("GOSB31");
        departmentGOSB31.setId(departmentID[3]);
        departmentGOSB31.setType(DepartmentType.CSKO_PCP);

        departmentOSB311 = new Department();
        departmentOSB311.setName("OSB311");
        departmentOSB311.setId(departmentID[4]);
        departmentOSB311.setType(DepartmentType.CSKO_PCP);

        // TODO data for исполнители

        // Подразделения
        when(departmentDao.getDepartment(root.getId())).thenReturn(root);
        when(departmentDao.getDepartment(departmentTB2.getId())).thenReturn(departmentTB2);
        when(departmentDao.getDepartment(departmentTB3.getId())).thenReturn(departmentTB3);
        when(departmentDao.getDepartment(departmentGOSB31.getId())).thenReturn(departmentGOSB31);
        when(departmentDao.getDepartment(departmentOSB311.getId())).thenReturn(departmentOSB311);
        // Иерархия подразделений (вверх)
        when(departmentDao.getParent(departmentTB2.getId())).thenReturn(root);
        when(departmentDao.getParent(departmentTB3.getId())).thenReturn(root);
        when(departmentDao.getParent(departmentGOSB31.getId())).thenReturn(departmentTB3);
        when(departmentDao.getParent(departmentOSB311.getId())).thenReturn(departmentGOSB31);
        // Иерархия подразделений (вниз)
        when(departmentDao.getAllChildren(root.getId())).thenReturn(asList(root, departmentTB2, departmentTB3, departmentGOSB31, departmentOSB311));
        when(departmentDao.getAllChildren(departmentTB2.getId())).thenReturn(asList(departmentTB2));
        when(departmentDao.getAllChildren(departmentTB3.getId())).thenReturn(asList(departmentTB3, departmentGOSB31, departmentOSB311));
        // Все подразделения
        when(departmentDao.listDepartments()).thenReturn(asList(root, departmentTB2, departmentTB3, departmentGOSB31, departmentOSB311));
        when(departmentDao.listDepartmentIds()).thenReturn(asList(root.getId(), departmentTB2.getId(), departmentTB3.getId(), departmentGOSB31.getId(), departmentOSB311.getId()));
        // Типы подразделений
        when(departmentDao.getDepartmentsByType(DepartmentType.ROOT_BANK.getCode())).thenReturn(asList(root));
        when(departmentDao.getDepartmentsByType(DepartmentType.TERR_BANK.getCode())).thenReturn(asList(departmentTB2, departmentTB3));
        when(departmentDao.getDepartmentsByType(DepartmentType.CSKO_PCP.getCode())).thenReturn(asList(departmentGOSB31));
        when(departmentDao.getDepartmentsByType(DepartmentType.MANAGEMENT.getCode())).thenReturn(asList(departmentOSB311));
        when(departmentDao.getDepartmentIdsByType(DepartmentType.ROOT_BANK.getCode())).thenReturn(asList(root.getId()));
        when(departmentDao.getDepartmentIdsByType(DepartmentType.TERR_BANK.getCode())).thenReturn(asList(departmentTB2.getId(), departmentTB3.getId()));
        when(departmentDao.getDepartmentIdsByType(DepartmentType.CSKO_PCP.getCode())).thenReturn(asList(departmentGOSB31.getId()));
        when(departmentDao.getDepartmentIdsByType(DepartmentType.MANAGEMENT.getCode())).thenReturn(asList(departmentOSB311.getId()));
        // ТБ для подразделения
        when(departmentDao.getDepartmenTB(root.getId())).thenReturn(null);
        when(departmentDao.getDepartmenTB(departmentTB2.getId())).thenReturn(departmentTB2);
        when(departmentDao.getDepartmenTB(departmentTB3.getId())).thenReturn(departmentTB3);
        when(departmentDao.getDepartmenTB(departmentGOSB31.getId())).thenReturn(departmentTB3);
        when(departmentDao.getDepartmenTB(departmentOSB311.getId())).thenReturn(departmentTB3);
        when(departmentDao.getDepartmenTBChildren(root.getId())).thenReturn(new ArrayList<Department>(0));
        when(departmentDao.getDepartmenTBChildren(departmentTB2.getId())).thenReturn(asList(departmentTB2));
        when(departmentDao.getDepartmenTBChildren(departmentTB3.getId())).thenReturn(asList(departmentTB3, departmentGOSB31, departmentOSB311));
        when(departmentDao.getDepartmenTBChildren(departmentGOSB31.getId())).thenReturn(asList(departmentTB3, departmentGOSB31, departmentOSB311));
        when(departmentDao.getDepartmenTBChildren(departmentOSB311.getId())).thenReturn(asList(departmentTB3, departmentGOSB31, departmentOSB311));

        when(periodService.getReportPeriod(any(Integer.class))).thenReturn(mock(ReportPeriod.class));

        when(departmentDao.getDepartmenTBChildrenId(root.getId())).thenReturn(new ArrayList<Integer>(0));
        when(departmentDao.getDepartmenTBChildrenId(departmentTB2.getId())).thenReturn(asList(departmentTB2.getId()));
        when(departmentDao.getDepartmenTBChildrenId(departmentTB3.getId())).thenReturn(asList(departmentTB3.getId(), departmentGOSB31.getId(), departmentOSB311.getId()));
        when(departmentDao.getDepartmenTBChildrenId(departmentGOSB31.getId())).thenReturn(asList(departmentTB3.getId(), departmentGOSB31.getId(), departmentOSB311.getId()));
        when(departmentDao.getDepartmenTBChildrenId(departmentOSB311.getId())).thenReturn(asList(departmentTB3.getId(), departmentGOSB31.getId(), departmentOSB311.getId()));

        // Роли
        taRoles = new ArrayList<TARole>();
        for (String alias : asList(TARole.ROLE_CONTROL_UNP, TARole.ROLE_ADMIN, TARole.ROLE_CONTROL_NS, TARole.ROLE_CONTROL, TARole.ROLE_OPER)) {
            TARole taRole = new TARole();
            taRole.setAlias(alias);
            taRoles.add(taRole);
        }

        DepartmentReportPeriod drpOpen = new DepartmentReportPeriod();
        drpOpen.setActive(true);
        DepartmentReportPeriod drpClose = new DepartmentReportPeriod();
        drpClose.setActive(false);
        // Отчетные периоды
        when(departmentReportPeriodDao.get(0, Long.valueOf(root.getId()))).thenReturn(drpOpen);
        when(departmentReportPeriodDao.get(0, Long.valueOf(departmentTB2.getId()))).thenReturn(drpOpen);
        when(departmentReportPeriodDao.get(0, Long.valueOf(departmentTB3.getId()))).thenReturn(drpClose);
        when(departmentReportPeriodDao.get(0, Long.valueOf(departmentGOSB31.getId()))).thenReturn(drpClose);
        when(departmentReportPeriodDao.get(0, Long.valueOf(departmentOSB311.getId()))).thenReturn(drpOpen);
        when(departmentReportPeriodDao.get(1, Long.valueOf(departmentOSB311.getId()))).thenReturn(drpClose);

        when(departmentReportPeriodDao.isPeriodOpen(root.getId(), 0)).thenReturn(true);
        when(departmentReportPeriodDao.isPeriodOpen(departmentTB2.getId(), 0)).thenReturn(true);
        when(departmentReportPeriodDao.isPeriodOpen(departmentTB3.getId(), 0)).thenReturn(false);
        when(departmentReportPeriodDao.isPeriodOpen(departmentGOSB31.getId(), 0)).thenReturn(false);
        when(departmentReportPeriodDao.isPeriodOpen(departmentOSB311.getId(), 0)).thenReturn(true);
        when(departmentReportPeriodDao.isPeriodOpen(departmentOSB311.getId(), 1)).thenReturn(false);

        when(departmentReportPeriodDao.existForDepartment(root.getId(), 0)).thenReturn(true);
        when(departmentReportPeriodDao.existForDepartment(departmentTB2.getId(), 0)).thenReturn(true);
        when(departmentReportPeriodDao.existForDepartment(departmentTB3.getId(), 0)).thenReturn(false);
        when(departmentReportPeriodDao.existForDepartment(departmentGOSB31.getId(), 0)).thenReturn(true);
        when(departmentReportPeriodDao.existForDepartment(departmentOSB311.getId(), 0)).thenReturn(true);
        when(departmentReportPeriodDao.existForDepartment(departmentOSB311.getId(), 1)).thenReturn(true);

        // Доступность по связям
        when(departmentDao.getDepartmentsBySourceControl(anyInt(), anyListOf(TaxType.class), any(Date.class), any(Date.class))).thenReturn(asList(departmentTB2.getId(), departmentTB3.getId()));
        when(departmentDao.getDepartmentsBySourceControlNs(anyInt(), anyListOf(TaxType.class), any(Date.class), any(Date.class))).thenReturn(asList(departmentTB2.getId(), departmentTB3.getId()));

        when(departmentDao.getDepartmentIdsByExecutors(Arrays.asList(311))).thenReturn(Arrays.asList(3));
        when(departmentDao.getDepartmentIdsByExecutors(Arrays.asList(31))).thenReturn(Arrays.asList(3));

        when(departmentDao.getParentTBId(311)).thenReturn(3);
        when(departmentDao.getParentTBId(31)).thenReturn(3);
        when(departmentDao.getParentTBId(3)).thenReturn(3);

        // Для дерева
        when(departmentDao.getRequiredForTreeDepartments(anyListOf(Integer.class))).thenAnswer(new Answer<List<Department>>() {
            @Override
            public List<Department> answer(InvocationOnMock invocation) throws Throwable {

                List<Integer> availableList = (List<Integer>)invocation.getArguments()[0];
                Set<Department> retVal = new HashSet<Department>();

                if (availableList.contains(root.getId())) {
                    retVal.addAll(Arrays.asList(root));
                }
                if (availableList.contains(departmentTB2.getId())) {
                    retVal.addAll(Arrays.asList(departmentTB2, root));
                }
                if (availableList.contains(departmentTB3.getId())) {
                    retVal.addAll(Arrays.asList(departmentTB3, root));
                }
                if (availableList.contains(departmentGOSB31.getId())) {
                    retVal.addAll(Arrays.asList(departmentGOSB31, departmentTB3, root));
                }
                if (availableList.contains(departmentOSB311.getId())) {
                    retVal.addAll(Arrays.asList(departmentOSB311, departmentGOSB31, departmentTB3, root));
                }

                return new ArrayList<Department>(retVal);
            }
        });
    }

    @Test
    public void depDaoTest() {
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
    public void listIdAllTest() {
        departmentService.listIdAll();
        verify(departmentDao, times(1)).listDepartmentIds();
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
        Set<Integer> available = new HashSet<Integer>(asList(departmentTB2.getId(), departmentTB3.getId()));

        Collection<Department> result = departmentService.getRequiredForTreeDepartments(available).values();
        Assert.assertEquals(3, result.size());
        Assert.assertEquals(true, result.contains(root));
        Assert.assertEquals(true, result.contains(departmentTB2));
        Assert.assertEquals(true, result.contains(departmentTB3));

        available = new HashSet<Integer>(asList(departmentOSB311.getId()));
        result = departmentService.getRequiredForTreeDepartments(available).values();
        Assert.assertEquals(4, result.size());
        Assert.assertEquals(true, result.contains(root));
        Assert.assertEquals(true, result.contains(departmentTB3));
        Assert.assertEquals(true, result.contains(departmentGOSB31));
        Assert.assertEquals(true, result.contains(departmentOSB311));

        result = departmentService.getRequiredForTreeDepartments(null).values();
        Assert.assertEquals(5, result.size());
        Assert.assertEquals(true, result.contains(root));
        Assert.assertEquals(true, result.contains(departmentTB2));
        Assert.assertEquals(true, result.contains(departmentTB3));
        Assert.assertEquals(true, result.contains(departmentGOSB31));
        Assert.assertEquals(true, result.contains(departmentOSB311));
    }

    @Test
    public void getBADepartmentsTest() {
        TAUser taUser = new TAUser();
        taUser.setRoles(taRoles);
        // Контролер УНП - все подразделения
        List<Department> result = departmentService.getBADepartments(taUser);
        Assert.assertEquals(5, result.size());
        Assert.assertTrue(result.containsAll(asList(root, departmentTB2, departmentTB3, departmentGOSB31, departmentOSB311)));

        // Контролер НС
        taUser.getRoles().remove(0);

        for (int i = 0; i < departmentID.length; i++) {
            taUser.setDepartmentId(departmentID[i]);
            result = departmentService.getBADepartments(taUser);

            switch (departmentID[i]) {
                case 0:
                    Assert.assertEquals(0, result.size());
                    break;
                case 2:
                    Assert.assertEquals(1, result.size());
                    Assert.assertTrue(result.contains(departmentTB2));
                    break;
                case 3:
                case 31:
                case 311:
                    Assert.assertEquals(3, result.size());
                    Assert.assertTrue(result.containsAll(asList(departmentTB3, departmentGOSB31, departmentOSB311)));
                    break;
            }
        }

        // Контролер
        taUser.getRoles().remove(0);
        taUser.getRoles().remove(0);
        for (int i = 0; i < departmentID.length; i++) {
            taUser.setDepartmentId(departmentID[i]);
            Assert.assertEquals(0, departmentService.getBADepartments(taUser).size());
        }
    }

    @Test
    public void getBADepartmentIdsTest() {
        TAUser taUser = new TAUser();
        taUser.setRoles(taRoles);
        // Контролер УНП - все подразделения
        List<Integer> result = departmentService.getBADepartmentIds(taUser);
        Assert.assertEquals(5, result.size());
        Assert.assertTrue(result.containsAll(asList(root.getId(), departmentTB2.getId(), departmentTB3.getId(), departmentGOSB31.getId(), departmentOSB311.getId())));

        // Контролер НС
        taUser.getRoles().remove(0);

        for (int i = 0; i < departmentID.length; i++) {
            taUser.setDepartmentId(departmentID[i]);
            result = departmentService.getBADepartmentIds(taUser);

            switch (departmentID[i]) {
                case 0:
                    Assert.assertEquals(0, result.size());
                    break;
                case 2:
                    Assert.assertEquals(1, result.size());
                    Assert.assertTrue(result.contains(departmentTB2.getId()));
                    break;
                case 3:
                case 31:
                case 311:
                    Assert.assertEquals(3, result.size());
                    Assert.assertTrue(result.containsAll(asList(departmentTB3.getId(), departmentGOSB31.getId(), departmentOSB311.getId())));
                    break;
            }
        }

        // Контролер
        taUser.getRoles().remove(0);
        taUser.getRoles().remove(0);
        for (int i = 0; i < departmentID.length; i++) {
            taUser.setDepartmentId(departmentID[i]);
            Assert.assertEquals(0, departmentService.getBADepartmentIds(taUser).size());
        }
    }

    @Test
    public void getTBDepartmentsTest() {
        TAUser taUser = new TAUser();
        taUser.setRoles(taRoles);

        List<Department> result = departmentService.getTBDepartments(taUser);
        Assert.assertEquals(3, result.size());
        Assert.assertEquals(true, result.contains(root) && result.contains(departmentTB2)
                && result.contains(departmentTB3));

        // REMOVE TARole.ROLE_CONTROL_UNP, TARole.ROLE_ADMIN
        taUser.getRoles().remove(0);
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
    public void getTBDepartmentIdsTest() {
        TAUser taUser = new TAUser();
        taUser.setRoles(taRoles);

        List<Integer> result = departmentService.getTBDepartmentIds(taUser);
        Assert.assertEquals(3, result.size());
        Assert.assertEquals(true, result.contains(root.getId()) && result.contains(departmentTB2.getId())
                && result.contains(departmentTB3.getId()));

        taUser.getRoles().remove(0);

        for (int i = 0; i < departmentID.length; i++) {
            taUser.setDepartmentId(departmentID[i]);
            result = departmentService.getTBDepartmentIds(taUser);

            switch (departmentID[i]) {
                case 0:
                    Assert.assertEquals(0, result.size());
                    break;
                case 2:
                case 3:
                    Assert.assertEquals(1, result.size());
                    Assert.assertEquals(true, result.contains(departmentTB2.getId()) || result.contains(departmentTB3.getId()));
                    break;
                case 31:
                case 311:
                    Assert.assertEquals(1, result.size());
                    Assert.assertEquals(true, result.contains(departmentTB3.getId()));
                    break;
            }
        }
    }

    @Test
    public void getBankDepartmentTest() {
        Department result = departmentService.getBankDepartment();
        Assert.assertEquals(result, root);
    }

    @Test
    public void getTaxFormDepartmentsTest() {
        TAUser taUser = new TAUser();
        taUser.setRoles(taRoles);

        List<Integer> result = departmentService.getTaxFormDepartments(taUser, asList(TaxType.INCOME), null, null);
        Assert.assertEquals(5, result.size());
        Assert.assertEquals(true, result.contains(root.getId()) && result.contains(departmentTB2.getId())
                && result.contains(departmentTB3.getId()) && result.contains(departmentGOSB31.getId())
                && result.contains(departmentOSB311.getId()));

        // TODO
        taUser.getRoles().remove(0);
        //test for ROLE_CONTROL_NS
        taUser.getRoles().remove(0);
        // test for ROLE_CONTROL
        taUser.getRoles().remove(0);
        // test for ROLE_OPER
    }

    @Test
    public void getDestinationDepartmentsTest() {
        TAUser taUser = new TAUser();
        taUser.setRoles(taRoles);

        // Контролер УНП
        List<Department> result = departmentService.getDestinationDepartments(taUser);
        Assert.assertEquals(5, result.size());
        Assert.assertTrue(result.containsAll(asList(root, departmentTB2, departmentTB3, departmentGOSB31, departmentOSB311)));

        // Контролер НС
        taUser.getRoles().remove(0);

        for (int i = 0; i < departmentID.length; i++) {
            taUser.setDepartmentId(departmentID[i]);
            result = departmentService.getDestinationDepartments(taUser);

            switch (departmentID[i]) {
                case 0:
                    Assert.assertEquals(2, result.size());
                    Assert.assertTrue(result.containsAll(asList(root, departmentGOSB31)));
                    break;
                case 2:
                    Assert.assertEquals(2, result.size());
                    Assert.assertTrue(result.containsAll(asList(departmentTB2, departmentGOSB31)));
                    break;
                case 3:
                case 31:
                case 311:
                    Assert.assertEquals(3, result.size());
                    Assert.assertTrue(result.containsAll(asList(departmentTB3, departmentGOSB31, departmentOSB311)));
                    break;
            }
        }

        // Контролер
        taUser.getRoles().remove(0);
        taUser.getRoles().remove(0);
        for (int i = 0; i < departmentID.length; i++) {
            taUser.setDepartmentId(departmentID[i]);
            Assert.assertEquals(0, departmentService.getDestinationDepartments(taUser).size());
        }
    }

    @Test
    public void getPrintFormDepartmentsTest() {
        // TODO Дописать тест после реализации getExecutorsDepartments http://jira.aplana.com/browse/SBRFACCTAX-5397
    }

    @Test
    public void getOpenPeriodDepartmentsTest() {
        TAUser taUser = new TAUser();
        taUser.setRoles(taRoles);

        List<Integer> result = departmentService.getOpenPeriodDepartments(taUser, asList(TaxType.INCOME), 0);
        Assert.assertEquals(3, result.size());
        Assert.assertTrue(result.containsAll(asList(root.getId(), departmentTB2.getId(), departmentOSB311.getId())));

        result = departmentService.getOpenPeriodDepartments(taUser, asList(TaxType.INCOME), 1);
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

package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

public class DepartmentServiceImplTest {

    private static DepartmentService departmentService;
    private static DepartmentDao departmentDao;

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
        departmentDao = mock(DepartmentDao.class);
        departmentService = new DepartmentServiceImpl(departmentDao);

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

        // ??????????????????????????
        when(departmentDao.getDepartment(root.getId())).thenReturn(root);
        when(departmentDao.getDepartment(departmentTB2.getId())).thenReturn(departmentTB2);
        when(departmentDao.getDepartment(departmentTB3.getId())).thenReturn(departmentTB3);
        when(departmentDao.getDepartment(departmentGOSB31.getId())).thenReturn(departmentGOSB31);
        when(departmentDao.getDepartment(departmentOSB311.getId())).thenReturn(departmentOSB311);
        // ???????????????? ?????????????????????????? (????????)
        when(departmentDao.getAllChildren(root.getId())).thenReturn(asList(root, departmentTB2, departmentTB3, departmentGOSB31, departmentOSB311));
        when(departmentDao.getAllChildren(departmentTB2.getId())).thenReturn(Collections.singletonList(departmentTB2));
        when(departmentDao.getAllChildren(departmentTB3.getId())).thenReturn(asList(departmentTB3, departmentGOSB31, departmentOSB311));
        // ?????? ??????????????????????????
        when(departmentDao.listDepartments()).thenReturn(asList(root, departmentTB2, departmentTB3, departmentGOSB31, departmentOSB311));
        when(departmentDao.fetchAllIds()).thenReturn(asList(root.getId(), departmentTB2.getId(), departmentTB3.getId(), departmentGOSB31.getId(), departmentOSB311.getId()));
        // ???????? ??????????????????????????
        when(departmentDao.getDepartmentsByType(DepartmentType.ROOT_BANK.getCode())).thenReturn(Collections.singletonList(root));
        when(departmentDao.getDepartmentsByType(DepartmentType.TERR_BANK.getCode())).thenReturn(asList(departmentTB2, departmentTB3));
        when(departmentDao.getDepartmentsByType(DepartmentType.CSKO_PCP.getCode())).thenReturn(Collections.singletonList(departmentGOSB31));
        when(departmentDao.getDepartmentsByType(DepartmentType.MANAGEMENT.getCode())).thenReturn(Collections.singletonList(departmentOSB311));
        when(departmentDao.getDepartmentIdsByType(DepartmentType.ROOT_BANK.getCode())).thenReturn(Collections.singletonList(root.getId()));
        when(departmentDao.getDepartmentIdsByType(DepartmentType.TERR_BANK.getCode())).thenReturn(asList(departmentTB2.getId(), departmentTB3.getId()));
        when(departmentDao.getDepartmentIdsByType(DepartmentType.CSKO_PCP.getCode())).thenReturn(Collections.singletonList(departmentGOSB31.getId()));
        when(departmentDao.getDepartmentIdsByType(DepartmentType.MANAGEMENT.getCode())).thenReturn(Collections.singletonList(departmentOSB311.getId()));
        // ???? ?????? ??????????????????????????
        when(departmentDao.getDepartmentTB(root.getId())).thenReturn(null);
        when(departmentDao.getDepartmentTB(departmentTB2.getId())).thenReturn(departmentTB2);
        when(departmentDao.getDepartmentTB(departmentTB3.getId())).thenReturn(departmentTB3);
        when(departmentDao.getDepartmentTB(departmentGOSB31.getId())).thenReturn(departmentTB3);
        when(departmentDao.getDepartmentTB(departmentOSB311.getId())).thenReturn(departmentTB3);
        when(departmentDao.getDepartmentTBChildren(root.getId())).thenReturn(new ArrayList<Department>(0));
        when(departmentDao.getDepartmentTBChildren(departmentTB2.getId())).thenReturn(Collections.singletonList(departmentTB2));
        when(departmentDao.getDepartmentTBChildren(departmentTB3.getId())).thenReturn(asList(departmentTB3, departmentGOSB31, departmentOSB311));
        when(departmentDao.getDepartmentTBChildren(departmentGOSB31.getId())).thenReturn(asList(departmentTB3, departmentGOSB31, departmentOSB311));
        when(departmentDao.getDepartmentTBChildren(departmentOSB311.getId())).thenReturn(asList(departmentTB3, departmentGOSB31, departmentOSB311));

        when(departmentDao.getDepartmentTBChildrenId(root.getId())).thenReturn(new ArrayList<Integer>(0));
        when(departmentDao.getDepartmentTBChildrenId(departmentTB2.getId())).thenReturn(Collections.singletonList(departmentTB2.getId()));
        when(departmentDao.getDepartmentTBChildrenId(departmentTB3.getId())).thenReturn(asList(departmentTB3.getId(), departmentGOSB31.getId(), departmentOSB311.getId()));
        when(departmentDao.getDepartmentTBChildrenId(departmentGOSB31.getId())).thenReturn(asList(departmentTB3.getId(), departmentGOSB31.getId(), departmentOSB311.getId()));
        when(departmentDao.getDepartmentTBChildrenId(departmentOSB311.getId())).thenReturn(asList(departmentTB3.getId(), departmentGOSB31.getId(), departmentOSB311.getId()));

        // ????????
        taRoles = new ArrayList<>();
        for (String alias : asList(TARole.N_ROLE_CONTROL_UNP, TARole.ROLE_ADMIN, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER)) {
            TARole taRole = new TARole();
            taRole.setAlias(alias);
            taRoles.add(taRole);
        }

        when(departmentDao.getParentTBId(311)).thenReturn(3);
        when(departmentDao.getParentTBId(31)).thenReturn(3);
        when(departmentDao.getParentTBId(3)).thenReturn(3);

        // ?????? ????????????
        when(departmentDao.getRequiredForTreeDepartments(anyListOf(Integer.class))).thenAnswer(new Answer<List<Department>>() {
            @Override
            public List<Department> answer(InvocationOnMock invocation) {

                List<Integer> availableList = (List<Integer>) invocation.getArguments()[0];
                Set<Department> retVal = new HashSet<>();

                if (availableList.contains(root.getId())) {
                    retVal.addAll(Collections.singletonList(root));
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

                return new ArrayList<>(retVal);
            }
        });
    }

    @Test
    public void depDaoTest() {
        Department department = new Department();
        DepartmentDao departmentDao = mock(DepartmentDao.class);
        List<Department> listDep = new ArrayList<>();
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
        verify(departmentDao, times(1)).fetchAllIds();
    }

    @Test
    public void getRequiredForTreeDepartmentsTest() {
        Set<Integer> available = new HashSet<>(asList(departmentTB2.getId(), departmentTB3.getId()));

        Collection<Department> result = departmentService.getRequiredForTreeDepartments(available).values();
        Assert.assertEquals(3, result.size());
        Assert.assertTrue(result.contains(root));
        Assert.assertTrue(result.contains(departmentTB2));
        Assert.assertTrue(result.contains(departmentTB3));

        available = new HashSet<>(Collections.singletonList(departmentOSB311.getId()));
        result = departmentService.getRequiredForTreeDepartments(available).values();
        Assert.assertEquals(4, result.size());
        Assert.assertTrue(result.contains(root));
        Assert.assertTrue(result.contains(departmentTB3));
        Assert.assertTrue(result.contains(departmentGOSB31));
        Assert.assertTrue(result.contains(departmentOSB311));

        result = departmentService.getRequiredForTreeDepartments(null).values();
        Assert.assertEquals(5, result.size());
        Assert.assertTrue(result.contains(root));
        Assert.assertTrue(result.contains(departmentTB2));
        Assert.assertTrue(result.contains(departmentTB3));
        Assert.assertTrue(result.contains(departmentGOSB31));
        Assert.assertTrue(result.contains(departmentOSB311));
    }

    //@Test
    public void getBADepartmentsTest() {
        TAUser taUser = new TAUser();
        taUser.setRoles(taRoles);
        // ?????????????????? ?????? - ?????? ??????????????????????????
        List<Department> result = departmentService.getBADepartments(taUser, TaxType.NDFL);
        Assert.assertEquals(5, result.size());
        Assert.assertTrue(result.containsAll(asList(root, departmentTB2, departmentTB3, departmentGOSB31, departmentOSB311)));

        // ?????????????????? ????
        taUser.getRoles().remove(0);

        for (int aDepartmentID : departmentID) {
            taUser.setDepartmentId(aDepartmentID);
            result = departmentService.getBADepartments(taUser, TaxType.NDFL);

            switch (aDepartmentID) {
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

        // ??????????????????
        taUser.getRoles().remove(0);
        taUser.getRoles().remove(0);
        for (int aDepartmentID : departmentID) {
            taUser.setDepartmentId(aDepartmentID);
            Assert.assertEquals(0, departmentService.getBADepartments(taUser, TaxType.NDFL).size());
        }
    }

    @Test
    public void getBADepartmentIdsTest() {
        TAUser taUser = new TAUser();
        taUser.setRoles(taRoles);
        // ?????????????????? ?????? - ?????? ??????????????????????????
        List<Integer> result = departmentService.getBADepartmentIds(taUser);
        Assert.assertEquals(5, result.size());
        Assert.assertTrue(result.containsAll(asList(root.getId(), departmentTB2.getId(), departmentTB3.getId(), departmentGOSB31.getId(), departmentOSB311.getId())));

        // ?????????????????? ????
        taUser.getRoles().remove(0);

        for (int aDepartmentID : departmentID) {
            taUser.setDepartmentId(aDepartmentID);
            result = departmentService.getBADepartmentIds(taUser);

            switch (aDepartmentID) {
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

        // ??????????????????
        taUser.getRoles().remove(0);
        taUser.getRoles().remove(0);
        for (int aDepartmentID : departmentID) {
            taUser.setDepartmentId(aDepartmentID);
            Assert.assertEquals(0, departmentService.getBADepartmentIds(taUser).size());
        }
    }

    @Test
    public void getTBDepartmentIdsTest() {
        TAUser taUser = new TAUser();
        taUser.setRoles(taRoles);

        List<Integer> result = departmentService.getTBDepartmentIds(taUser, TaxType.NDFL, true);
        Assert.assertEquals(3, result.size());
        Assert.assertTrue(result.contains(root.getId()) && result.contains(departmentTB2.getId())
                && result.contains(departmentTB3.getId()));

        taUser.getRoles().remove(0);

        for (int aDepartmentID : departmentID) {
            taUser.setDepartmentId(aDepartmentID);
            result = departmentService.getTBDepartmentIds(taUser, TaxType.NDFL, true);

            switch (aDepartmentID) {
                case 0:
                    Assert.assertEquals(0, result.size());
                    break;
                case 2:
                case 3:
                    Assert.assertEquals(1, result.size());
                    Assert.assertTrue(result.contains(departmentTB2.getId()) || result.contains(departmentTB3.getId()));
                    break;
                case 31:
                case 311:
                    Assert.assertEquals(1, result.size());
                    Assert.assertTrue(result.contains(departmentTB3.getId()));
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
    public void getDestinationDepartmentsTest() {
        TAUser taUser = new TAUser();
        taUser.setRoles(taRoles);

        // ?????????????????? ??????
        List<Department> result = departmentService.getDestinationDepartments(TaxType.NDFL, taUser);
        Assert.assertEquals(5, result.size());
        Assert.assertTrue(result.containsAll(asList(root, departmentTB2, departmentTB3, departmentGOSB31, departmentOSB311)));

        // ?????????????????? ????
        taUser.getRoles().remove(0);
        result = departmentService.getDestinationDepartments(TaxType.NDFL, taUser);
        Assert.assertEquals(5, result.size());
        Assert.assertTrue(result.containsAll(asList(root, departmentTB2, departmentTB3, departmentGOSB31, departmentOSB311)));

        // ??????????????????
        taUser.getRoles().remove(0);
        taUser.getRoles().remove(0);
        for (int aDepartmentID : departmentID) {
            taUser.setDepartmentId(aDepartmentID);
            Assert.assertEquals(0, departmentService.getDestinationDepartments(TaxType.NDFL, taUser).size());
        }
    }

    @Test
    public void testSearchDepartmentNames() {
        PagingParams pagingParams = new PagingParams();
        departmentService.searchDepartmentNames("Something", pagingParams);
        verify(departmentDao).searchDepartmentNames("Something", pagingParams);
    }
}

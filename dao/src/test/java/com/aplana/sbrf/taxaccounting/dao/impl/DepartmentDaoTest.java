package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DepartmentDaoTest.xml"})
@Transactional
public class DepartmentDaoTest {

    @Autowired
    private DepartmentDao departmentDao;

    @Test
    public void testGet() {
        Department d = departmentDao.getDepartment(Department.ROOT_BANK_ID);
        Assert.assertEquals(Department.ROOT_BANK_ID, d.getId());
        Assert.assertEquals(DepartmentType.ROOT_BANK, d.getType());
        Assert.assertEquals("Банк", d.getName());
        Assert.assertNull(d.getParentId());

        d = departmentDao.getDepartment(2);
        Assert.assertEquals(2, d.getId());
        Assert.assertEquals(DepartmentType.TERBANK, d.getType());
        Assert.assertEquals(new Integer(Department.ROOT_BANK_ID), d.getParentId());
        Assert.assertEquals("ТБ1", d.getName());

        d = departmentDao.getDepartment(3);
        Assert.assertEquals(3, d.getId());
        Assert.assertEquals(DepartmentType.TERBANK, d.getType());
        Assert.assertEquals(new Integer(Department.ROOT_BANK_ID), d.getParentId());
        Assert.assertEquals("ТБ2", d.getName());
    }

    @Test(expected = DaoException.class)
    public void testGetIncorrectId() {
        departmentDao.getDepartment(-1);
    }

    @Test
    public void getParentTest() {
        Department department;
        department = departmentDao.getParent(2);
        Assert.assertEquals(1, department.getId());
        Assert.assertEquals("Банк", department.getName());

        department = departmentDao.getParent(3);
        Assert.assertEquals(1, department.getId());
        Assert.assertEquals("Банк", department.getName());
    }

    @Test
    public void getSbrfCode() {
        Department department;
        department = departmentDao.getDepartmentBySbrfCode("23");
        Assert.assertNotNull(department);
        Assert.assertTrue(true);
    }

    @Test
    public void getName() {
        Department department;
        department = departmentDao.getDepartmentByName("ТБ2");
        Assert.assertNotNull(department);
        Assert.assertEquals(3, department.getId());
    }

    @Test
    public void getDepartmentsByType() {
        List<Department> result = departmentDao.getDepartmentsByType(DepartmentType.ROOT_BANK.getCode());
        Assert.assertEquals(1, result.size());
        result = departmentDao.getDepartmentsByType(DepartmentType.TERBANK.getCode());
        Assert.assertEquals(2, result.size());
        result = departmentDao.getDepartmentsByType(DepartmentType.GOSB.getCode());
        Assert.assertEquals(3, result.size());
        result = departmentDao.getDepartmentsByType(DepartmentType.OSB.getCode());
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getDepartmenTBTest() {
        Department result;
        result = departmentDao.getDepartmenTB(0);
        Assert.assertNull(result);
        result = departmentDao.getDepartmenTB(1);
        Assert.assertNull(result);
        result = departmentDao.getDepartmenTB(2);
        Assert.assertEquals(result.getId(), 2);
        result = departmentDao.getDepartmenTB(3);
        Assert.assertEquals(result.getId(), 3);
        result = departmentDao.getDepartmenTB(4);
        Assert.assertEquals(result.getId(), 3);
        result = departmentDao.getDepartmenTB(5);
        Assert.assertEquals(result.getId(), 3);
        result = departmentDao.getDepartmenTB(6);
        Assert.assertEquals(result.getId(), 2);
    }

    private List<Integer> getDepartmentIds(List<Department> departmentList) {
        List<Integer> retVal = new LinkedList<Integer>();
        for (Department department : departmentList) {
            retVal.add(department.getId());
        }
        return retVal;
    }

    @Test
    public void getRequiredForTreeDepartments() {
        List<Department> departmentList;
        // 1 -> 1
        departmentList = departmentDao.getRequiredForTreeDepartments(Arrays.asList(1));
        Assert.assertEquals(1, departmentList.size());
        Assert.assertEquals(1, departmentList.get(0).getId());
        // 2,5 -> 1,2,3,5
        departmentList = departmentDao.getRequiredForTreeDepartments(Arrays.asList(2, 5));
        Assert.assertEquals(4, departmentList.size());
        Assert.assertTrue(getDepartmentIds(departmentList).containsAll(Arrays.asList(1, 2, 3, 5)));
        // 6 -> 1,2,6
        departmentList = departmentDao.getRequiredForTreeDepartments(Arrays.asList(6));
        Assert.assertEquals(3, departmentList.size());
        Assert.assertTrue(getDepartmentIds(departmentList).containsAll(Arrays.asList(1, 2, 6)));
    }
}

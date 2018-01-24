package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.Arrays.asList;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DepartmentDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DepartmentDaoTest {

    @Autowired
    private DepartmentDao departmentDao;

    @Test
    public void testGet() {
        Department d = departmentDao.getDepartment(1);
        Assert.assertEquals(1, d.getId());
        Assert.assertEquals(DepartmentType.ROOT_BANK, d.getType());
        Assert.assertEquals("Банк", d.getName());
        Assert.assertNull(d.getParentId());

        d = departmentDao.getDepartment(2);
        Assert.assertEquals(2, d.getId());
        Assert.assertEquals(DepartmentType.TERR_BANK, d.getType());
        Assert.assertEquals(new Integer(1), d.getParentId());
        Assert.assertEquals("ТБ1", d.getName());

        d = departmentDao.getDepartment(3);
        Assert.assertEquals(3, d.getId());
        Assert.assertEquals(DepartmentType.TERR_BANK, d.getType());
        Assert.assertEquals(new Integer(1), d.getParentId());
        Assert.assertEquals("ТБ2", d.getName());
    }

    @Test
    public void testExist() {
        Assert.assertTrue(departmentDao.existDepartment(1));
    }

    @Test(expected = DaoException.class)
    public void testGetIncorrectId() {
        departmentDao.getDepartment(-1);
    }

    @Test
    public void getSbrfCode() {
        Department department;
        department = departmentDao.getDepartmentBySbrfCode("tB3", true);
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
        List<Integer> result = departmentDao.getDepartmentIdsByType(DepartmentType.ROOT_BANK.getCode());
        Assert.assertEquals(1, result.size());
        result = departmentDao.getDepartmentIdsByType(DepartmentType.TERR_BANK.getCode());
        Assert.assertEquals(3, result.size());
        result = departmentDao.getDepartmentIdsByType(DepartmentType.CSKO_PCP.getCode());
        Assert.assertEquals(3, result.size());
        result = departmentDao.getDepartmentIdsByType(DepartmentType.MANAGEMENT.getCode());
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getDepartmentIdsByType() {
        List<Department> result = departmentDao.getDepartmentsByType(DepartmentType.ROOT_BANK.getCode());
        Assert.assertEquals(1, result.size());
        result = departmentDao.getDepartmentsByType(DepartmentType.TERR_BANK.getCode());
        Assert.assertEquals(3, result.size());
        result = departmentDao.getDepartmentsByType(DepartmentType.CSKO_PCP.getCode());
        Assert.assertEquals(3, result.size());
        result = departmentDao.getDepartmentsByType(DepartmentType.MANAGEMENT.getCode());
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getDepartmentTBTest() {
        Department result;
        result = departmentDao.getDepartmentTB(0);
        Assert.assertNull(result);
        result = departmentDao.getDepartmentTB(1);
        Assert.assertNull(result);
        result = departmentDao.getDepartmentTB(2);
        Assert.assertEquals(result.getId(), 2);
        result = departmentDao.getDepartmentTB(3);
        Assert.assertEquals(result.getId(), 3);
        result = departmentDao.getDepartmentTB(4);
        Assert.assertEquals(result.getId(), 3);
        result = departmentDao.getDepartmentTB(5);
        Assert.assertEquals(result.getId(), 3);
        result = departmentDao.getDepartmentTB(6);
        Assert.assertEquals(result.getId(), 2);
    }

    @Test
    public void getDepartmentTBChildrenTest() {
        List<Department> result;
        result = departmentDao.getDepartmentTBChildren(0);
        Assert.assertEquals(0, result.size());
        result = departmentDao.getDepartmentTBChildren(1);
        Assert.assertEquals(0, result.size());
        result = departmentDao.getDepartmentTBChildren(2);
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(getDepartmentIds(result).containsAll(asList(2, 6)));
        result = departmentDao.getDepartmentTBChildren(3);
        Assert.assertEquals(3, result.size());
        Assert.assertTrue(getDepartmentIds(result).containsAll(asList(3, 4, 5)));
        result = departmentDao.getDepartmentTBChildren(4);
        Assert.assertEquals(3, result.size());
        Assert.assertTrue(getDepartmentIds(result).containsAll(asList(3, 4, 5)));
        result = departmentDao.getDepartmentTBChildren(5);
        Assert.assertEquals(3, result.size());
        Assert.assertTrue(getDepartmentIds(result).containsAll(asList(3, 4, 5)));
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
        departmentList = departmentDao.getRequiredForTreeDepartments(asList(1));
        Assert.assertEquals(1, departmentList.size());
        Assert.assertEquals(1, departmentList.get(0).getId());
        // 2,5 -> 1,2,3,5
        departmentList = departmentDao.getRequiredForTreeDepartments(asList(2, 5));
        Assert.assertEquals(4, departmentList.size());
        Assert.assertTrue(getDepartmentIds(departmentList).containsAll(asList(1, 2, 3, 5)));
        // 6 -> 1,2,6
        departmentList = departmentDao.getRequiredForTreeDepartments(asList(6));
        Assert.assertEquals(3, departmentList.size());
        Assert.assertTrue(getDepartmentIds(departmentList).containsAll(asList(1, 2, 6)));
    }

    @Test
    public void getDepartmentsByDeclarationsPerformersTest() {
        List<Integer> result = departmentDao.fetchAllIdsByDeclarationsPerformers(asList(5));
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.containsAll(asList(2)));

        result = departmentDao.fetchAllIdsByDeclarationsPerformers(asList(3));
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.containsAll(asList(2)));

        result = departmentDao.fetchAllIdsByDeclarationsPerformers(asList(3, 4, 5));
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.containsAll(asList(2)));

        result = departmentDao.fetchAllIdsByDeclarationsPerformers(asList(7));
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.containsAll(asList(2, 3)));
    }
}

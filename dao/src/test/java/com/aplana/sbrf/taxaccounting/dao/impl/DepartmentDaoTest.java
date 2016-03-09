package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
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
        department = departmentDao.getDepartmentBySbrfCode("tB3");
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
    public void getPerformers() {
        Department department2 = departmentDao.getDepartment(2);
        List<Integer> performers = departmentDao.getPerformers(asList(department2.getId()), 1);
        Assert.assertTrue("Department(id=2) has 1 performer", performers.size() == 1);
        Assert.assertTrue("Department(id=2) has 1 performer with id = 1", performers.get(0) == 1);
    }

    @Test
    public void getPerformersGroup() {
        Department department3 = departmentDao.getDepartment(3);
        List<Integer> performers = departmentDao.getPerformers(asList(department3.getId()), 2);
        Assert.assertTrue("Department(id=3) has 1 performer", performers.size() == 1);
        Assert.assertTrue("Department(id=3) has 1 performer with id = 2", performers.get(0) == 2);
    }

    @Test
    public void getDepartmentIdsByExcutorsTest() {
        List<Integer> result = departmentDao.getDepartmentIdsByExecutors(asList(1, 2, 3, 4, 5, 6),
                asList(TaxType.INCOME, TaxType.VAT, TaxType.TRANSPORT));
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.containsAll(asList(2, 3)));

        result = departmentDao.getDepartmentIdsByExecutors(asList(2),
                asList(TaxType.TRANSPORT));
        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.containsAll(asList(3)));
    }

    /*@Test
    public void getReportDepartmentNameTest() {
        Assert.assertEquals("ТБ2/ЦСКО 1", departmentDao.getReportDepartmentName(5));
        Assert.assertEquals("ТБ1/ЦСКО 1", departmentDao.getReportDepartmentName(6));
        Assert.assertEquals("ТБ1", departmentDao.getReportDepartmentName(2));
    }*/

    @Test
    public void getDepartmentsByDestinationSourceTest() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2013, Calendar.JANUARY, 1);
        Date dateStart = calendar.getTime();
        calendar.set(2014, Calendar.DECEMBER, 31);
        Date dateEnd = calendar.getTime();
        ArrayList<Integer> departments = new ArrayList<Integer>();
        departments.add(4);
        departments.add(5);
        departments.add(6);
        Assert.assertEquals(0, departmentDao.getDepartmentsByDestinationSource(departments, dateStart, dateEnd).size());
        departments.add(1);
        Assert.assertEquals(1, departmentDao.getDepartmentsByDestinationSource(departments, null, null).size());
        Assert.assertEquals(2, departmentDao.getDepartmentsByDestinationSource(departments, dateStart, null).get(0).getId());
        departments.add(2);
        departments.add(3);
        Assert.assertEquals(1, departmentDao.getDepartmentsByDestinationSource(departments, dateStart, dateEnd).size());
        calendar.set(2012, Calendar.DECEMBER, 31);
        dateEnd = calendar.getTime();
        Assert.assertEquals(0, departmentDao.getDepartmentsByDestinationSource(departments, dateStart, dateEnd).size());
    }

    @Test
    public void getDepartmentIdsByDestinationSourceTest() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2013, Calendar.JANUARY, 1);
        Date dateStart = calendar.getTime();
        calendar.set(2014, Calendar.DECEMBER, 31);
        Date dateEnd = calendar.getTime();
        ArrayList<Integer> departments = new ArrayList<Integer>();
        departments.add(4);
        departments.add(5);
        departments.add(6);
        Assert.assertEquals(0, departmentDao.getDepartmentIdsByDestinationSource(departments, dateStart, dateEnd).size());
        departments.add(1);
        Assert.assertEquals(1, departmentDao.getDepartmentIdsByDestinationSource(departments, null, null).size());
        Assert.assertEquals(2, (int)departmentDao.getDepartmentIdsByDestinationSource(departments, dateStart, null).get(0));
    }
}

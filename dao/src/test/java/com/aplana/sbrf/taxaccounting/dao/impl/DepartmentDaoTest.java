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

import javax.validation.ValidationException;
import java.util.LinkedList;
import java.util.List;

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
        Department d = departmentDao.getDepartment(Department.ROOT_BANK_ID);
        Assert.assertEquals(Department.ROOT_BANK_ID, d.getId());
        Assert.assertEquals(DepartmentType.ROOT_BANK, d.getType());
        Assert.assertEquals("Банк", d.getName());
        Assert.assertNull(d.getParentId());

        d = departmentDao.getDepartment(2);
        Assert.assertEquals(2, d.getId());
        Assert.assertEquals(DepartmentType.TERR_BANK, d.getType());
        Assert.assertEquals(new Integer(Department.ROOT_BANK_ID), d.getParentId());
        Assert.assertEquals("ТБ1", d.getName());

        d = departmentDao.getDepartment(3);
        Assert.assertEquals(3, d.getId());
        Assert.assertEquals(DepartmentType.TERR_BANK, d.getType());
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
        List<Integer> result = departmentDao.getDepartmentIdsByType(DepartmentType.ROOT_BANK.getCode());
        Assert.assertEquals(1, result.size());
        result = departmentDao.getDepartmentIdsByType(DepartmentType.TERR_BANK.getCode());
        Assert.assertEquals(2, result.size());
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
        Assert.assertEquals(2, result.size());
        result = departmentDao.getDepartmentsByType(DepartmentType.CSKO_PCP.getCode());
        Assert.assertEquals(3, result.size());
        result = departmentDao.getDepartmentsByType(DepartmentType.MANAGEMENT.getCode());
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

    @Test
    public void getDepartmenTBChildrenTest() {
        List<Department> result;
        result = departmentDao.getDepartmenTBChildren(0);
        Assert.assertEquals(0, result.size());
        result = departmentDao.getDepartmenTBChildren(1);
        Assert.assertEquals(0, result.size());
        result = departmentDao.getDepartmenTBChildren(2);
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(getDepartmentIds(result).containsAll(asList(2, 6)));
        result = departmentDao.getDepartmenTBChildren(3);
        Assert.assertEquals(3, result.size());
        Assert.assertTrue(getDepartmentIds(result).containsAll(asList(3, 4, 5)));
        result = departmentDao.getDepartmenTBChildren(4);
        Assert.assertEquals(3, result.size());
        Assert.assertTrue(getDepartmentIds(result).containsAll(asList(3, 4, 5)));
        result = departmentDao.getDepartmenTBChildren(5);
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
    public void getPerformers(){
        Department department2 = departmentDao.getDepartment(2);
        List<Integer> performers = departmentDao.getPerformers(asList(department2.getId()),1);
        Assert.assertTrue("Department(id=2) has 1 performer", performers.size() == 1);
        Assert.assertTrue("Department(id=2) has 1 performer with id = 1", performers.get(0) == 1);
    }

    @Test
    public void getPerformersGroup(){
        Department department3 = departmentDao.getDepartment(3);
        List<Integer> performers = departmentDao.getPerformers(asList(department3.getId()),2);
        Assert.assertTrue("Department(id=3) has 1 performer", performers.size() == 1);
        Assert.assertTrue("Department(id=3) has 1 performer with id = 2", performers.get(0) == 2);
    }

	@Test
	public void getPerformers2(){
            Department department2 = departmentDao.getDepartment(2);
            List<Integer> performers = departmentDao.getPerformers(asList(department2.getId()), asList(TaxType.TRANSPORT));
            Assert.assertTrue("Department(id=2) has 1 performer", performers.size() == 1);            Assert.assertTrue("Department(id=2) has 1 performer with id = 1", performers.get(0) == 1);
    }

    @Test
    public void getDepartmentIdsByExcutorsTest() {
        List<Integer> result = departmentDao.getDepartmentIdsByExcutors(asList(1, 2, 3, 4, 5, 6),
                asList(TaxType.INCOME, TaxType.VAT, TaxType.TRANSPORT));
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.containsAll(asList(2, 3)));

        result = departmentDao.getDepartmentIdsByExcutors(asList(2),
                asList(TaxType.TRANSPORT));
        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.containsAll(asList(3)));
    }

    @Test
    public void getDepartmentByNameTest() {
        Assert.assertEquals(2, departmentDao.getDepartmentsByName("ТБ").size());
    }

    @Test
    public void getReportDepartmentNameTest() {
        Assert.assertEquals("ТБ2/ЦСКО 1", departmentDao.getReportDepartmentName(5));
        Assert.assertEquals("ТБ1/ЦСКО 1", departmentDao.getReportDepartmentName(6));
        Assert.assertEquals("ТБ1", departmentDao.getReportDepartmentName(2));
    }

    @Test
    public void getDepartmentByCode1Test() {
        Assert.assertEquals(1, departmentDao.getDepartmentByCode(1).getId());
        Assert.assertEquals(2, departmentDao.getDepartmentByCode(2).getId());
        Assert.assertEquals(3, departmentDao.getDepartmentByCode(3).getId());
        Assert.assertEquals(4, departmentDao.getDepartmentByCode(4).getId());
        Assert.assertEquals(5, departmentDao.getDepartmentByCode(5).getId());
        Assert.assertEquals(6, departmentDao.getDepartmentByCode(6).getId());
    }

    @Test
    public void getDepartmentByCode2Test() {
        Assert.assertNull(departmentDao.getDepartmentByCode(-1));
    }

//    @Test(expected = ValidationException.class)
//    public void violationTest() throws Exception {
//        departmentDao.getParentsHierarchy(null);
//    }
}

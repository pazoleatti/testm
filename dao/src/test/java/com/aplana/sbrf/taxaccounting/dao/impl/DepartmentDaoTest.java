package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentName;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

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
        assertEquals(1, d.getId());
        assertEquals(DepartmentType.ROOT_BANK, d.getType());
        assertEquals("Банк", d.getName());
        assertNull(d.getParentId());

        d = departmentDao.getDepartment(2);
        assertEquals(2, d.getId());
        assertEquals(DepartmentType.TERR_BANK, d.getType());
        assertEquals(new Integer(1), d.getParentId());
        assertEquals("ТБ1", d.getName());

        d = departmentDao.getDepartment(3);
        assertEquals(3, d.getId());
        assertEquals(DepartmentType.TERR_BANK, d.getType());
        assertEquals(new Integer(1), d.getParentId());
        assertEquals("ТБ2", d.getName());
    }

    @Test
    public void testExist() {
        assertTrue(departmentDao.existDepartment(1));
    }

    @Test(expected = DaoException.class)
    public void testGetIncorrectId() {
        departmentDao.getDepartment(-1);
    }

    @Test
    public void getSbrfCode() {
        Department department;
        department = departmentDao.getDepartmentBySbrfCode("tB3", true);
        assertNotNull(department);
        assertTrue(true);
    }

    @Test
    public void getDepartmentsByType() {
        List<Integer> result = departmentDao.getDepartmentIdsByType(DepartmentType.ROOT_BANK.getCode());
        assertEquals(1, result.size());
        result = departmentDao.getDepartmentIdsByType(DepartmentType.TERR_BANK.getCode());
        assertEquals(3, result.size());
        result = departmentDao.getDepartmentIdsByType(DepartmentType.CSKO_PCP.getCode());
        assertEquals(3, result.size());
        result = departmentDao.getDepartmentIdsByType(DepartmentType.MANAGEMENT.getCode());
        assertEquals(0, result.size());
    }

    @Test
    public void getDepartmentIdsByType() {
        List<Department> result = departmentDao.getDepartmentsByType(DepartmentType.ROOT_BANK.getCode());
        assertEquals(1, result.size());
        result = departmentDao.getDepartmentsByType(DepartmentType.TERR_BANK.getCode());
        assertEquals(3, result.size());
        result = departmentDao.getDepartmentsByType(DepartmentType.CSKO_PCP.getCode());
        assertEquals(3, result.size());
        result = departmentDao.getDepartmentsByType(DepartmentType.MANAGEMENT.getCode());
        assertEquals(0, result.size());
    }

    @Test
    public void getDepartmentTBTest() {
        Department result;
        result = departmentDao.getDepartmentTB(0);
        assertNull(result);
        result = departmentDao.getDepartmentTB(1);
        assertNull(result);
        result = departmentDao.getDepartmentTB(2);
        assertEquals(result.getId(), 2);
        result = departmentDao.getDepartmentTB(3);
        assertEquals(result.getId(), 3);
        result = departmentDao.getDepartmentTB(4);
        assertEquals(result.getId(), 3);
        result = departmentDao.getDepartmentTB(5);
        assertEquals(result.getId(), 3);
        result = departmentDao.getDepartmentTB(6);
        assertEquals(result.getId(), 2);
    }

    @Test
    public void getDepartmentTBChildrenTest() {
        List<Department> result;
        result = departmentDao.getDepartmentTBChildren(0);
        assertEquals(0, result.size());
        result = departmentDao.getDepartmentTBChildren(1);
        assertEquals(0, result.size());
        result = departmentDao.getDepartmentTBChildren(2);
        assertEquals(2, result.size());
        assertTrue(getDepartmentIds(result).containsAll(asList(2, 6)));
        result = departmentDao.getDepartmentTBChildren(3);
        assertEquals(3, result.size());
        assertTrue(getDepartmentIds(result).containsAll(asList(3, 4, 5)));
        result = departmentDao.getDepartmentTBChildren(4);
        assertEquals(3, result.size());
        assertTrue(getDepartmentIds(result).containsAll(asList(3, 4, 5)));
        result = departmentDao.getDepartmentTBChildren(5);
        assertEquals(3, result.size());
        assertTrue(getDepartmentIds(result).containsAll(asList(3, 4, 5)));
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
        assertEquals(1, departmentList.size());
        assertEquals(1, departmentList.get(0).getId());
        // 2,5 -> 1,2,3,5
        departmentList = departmentDao.getRequiredForTreeDepartments(asList(2, 5));
        assertEquals(4, departmentList.size());
        assertTrue(getDepartmentIds(departmentList).containsAll(asList(1, 2, 3, 5)));
        // 6 -> 1,2,6
        departmentList = departmentDao.getRequiredForTreeDepartments(asList(6));
        assertEquals(3, departmentList.size());
        assertTrue(getDepartmentIds(departmentList).containsAll(asList(1, 2, 6)));
    }

    @Test
    public void getDepartmentsByDeclarationsPerformersTest() {
        List<Integer> result = departmentDao.findAllIdsByPerformerIds(asList(5));
        assertEquals(1, result.size());
        assertTrue(result.containsAll(asList(2)));

        result = departmentDao.findAllIdsByPerformerIds(asList(3));
        assertEquals(1, result.size());
        assertTrue(result.containsAll(asList(2)));

        result = departmentDao.findAllIdsByPerformerIds(asList(3, 4, 5));
        assertEquals(1, result.size());
        assertTrue(result.containsAll(asList(2)));

        result = departmentDao.findAllIdsByPerformerIds(asList(7));
        assertEquals(2, result.size());
        assertTrue(result.containsAll(asList(2, 3)));
    }

    @Test
    public void testSearchDepartmentNames() {

        PagingParams pagingParams = new PagingParams();

        List<DepartmentName> searchNull = departmentDao.searchDepartmentNames(null, pagingParams);
        assertThat(searchNull).hasSize(7);

        List<DepartmentName> searchEmpty = departmentDao.searchDepartmentNames("", pagingParams);
        assertThat(searchEmpty).hasSize(7);

        List<DepartmentName> searchExisting = departmentDao.searchDepartmentNames("банк/тб", pagingParams);
        assertThat(searchExisting).hasSize(6);

        List<DepartmentName> searchNonexistent = departmentDao.searchDepartmentNames("Не банк", pagingParams);
        assertThat(searchNonexistent).isEmpty();
    }
}

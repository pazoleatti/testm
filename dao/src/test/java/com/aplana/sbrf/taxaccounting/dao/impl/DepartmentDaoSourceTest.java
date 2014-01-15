package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Arrays.asList;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DepartmentDaoSourceTest.xml"})
@Transactional
public class DepartmentDaoSourceTest {

    @Autowired
    private DepartmentDao departmentDao;

    @Test
    public void getDepartmentsBySourceControl() {
        List<Integer> departmentIds;
        // Контролер
        departmentIds = departmentDao.getDepartmentsBySourceControl(6, TaxType.INCOME);
        Assert.assertTrue(departmentIds.containsAll(asList(6, 7, 9, 10, 11)));
        departmentIds = departmentDao.getDepartmentsBySourceControl(6, TaxType.TRANSPORT);
        Assert.assertTrue(departmentIds.containsAll(asList(6, 7, 9)));
        departmentIds = departmentDao.getDepartmentsBySourceControl(6, TaxType.DEAL);
        Assert.assertTrue(departmentIds.containsAll(asList(6, 7, 9)));
    }

    @Test
    public void getDepartmentsBySourceControlNs() {
        List<Integer> departmentIds;
        // Контролер НС
        departmentIds = departmentDao.getDepartmentsBySourceControlNs(6, TaxType.INCOME);
        Assert.assertTrue(departmentIds.containsAll(asList(2, 6, 7, 8, 9, 10, 11)));
        departmentIds = departmentDao.getDepartmentsBySourceControlNs(6, TaxType.TRANSPORT);
        Assert.assertTrue(departmentIds.containsAll(asList(1, 2, 6, 7, 8, 9, 10)));
        departmentIds = departmentDao.getDepartmentsBySourceControlNs(6, TaxType.DEAL);
        Assert.assertTrue(departmentIds.containsAll(asList(2, 6, 7, 8, 9, 10)));
    }
}

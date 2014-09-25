package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Arrays.asList;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DepartmentDaoSourceTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DepartmentDaoSourceTest {

    @Autowired
    private DepartmentDao departmentDao;

    @Test
    public void getDepartmentsBySourceControl() {
        List<Integer> departmentIds;
        // Контролер
        departmentIds = departmentDao.getDepartmentsBySourceControl(6, asList(TaxType.INCOME), null, null);
        Assert.assertEquals(5, departmentIds.size());
        Assert.assertTrue(departmentIds.containsAll(asList(6, 7, 9, 10, 11)));
        departmentIds = departmentDao.getDepartmentsBySourceControl(6, asList(TaxType.TRANSPORT), null, null);
        Assert.assertTrue(departmentIds.containsAll(asList(6, 7, 9)));
        Assert.assertEquals(3, departmentIds.size());
        departmentIds = departmentDao.getDepartmentsBySourceControl(6, asList(TaxType.DEAL), null, null);
        Assert.assertEquals(3, departmentIds.size());
        Assert.assertTrue(departmentIds.containsAll(asList(6, 7, 9)));

        // Тесты для списка
        departmentIds = departmentDao.getDepartmentsBySourceControl(8, asList(TaxType.INCOME, TaxType.DEAL, TaxType.TRANSPORT), null, null);
        Assert.assertEquals(1, departmentIds.size());
        Assert.assertTrue(departmentIds.containsAll(asList(8)));
        departmentIds = departmentDao.getDepartmentsBySourceControl(6, asList(TaxType.INCOME, TaxType.DEAL, TaxType.TRANSPORT), null, null);
        Assert.assertEquals(5, departmentIds.size());
        Assert.assertTrue(departmentIds.containsAll(asList(6, 7, 9, 10, 11)));
        departmentIds = departmentDao.getDepartmentsBySourceControlNs(6, asList(TaxType.INCOME, TaxType.VAT), null, null);
        Assert.assertEquals(8, departmentIds.size());
        Assert.assertTrue(departmentIds.containsAll(asList(2, 6, 7, 8, 9, 10, 11, 12)));
    }

    @Test
    public void getEmptyDepartmentsBySourceControl() {
        List<Integer> departmentIds = departmentDao.getDepartmentsBySourceControl(-1, asList(TaxType.VAT), null, null);
        Assert.assertNotNull(departmentIds);
        Assert.assertTrue(departmentIds.isEmpty());
    }

    @Test
    public void getDepartmentsBySourceControlNs() {
        List<Integer> departmentIds;
        // Контролер НС
        departmentIds = departmentDao.getDepartmentsBySourceControlNs(6, asList(TaxType.INCOME), null, null);
        Assert.assertEquals(7, departmentIds.size());
        Assert.assertTrue(departmentIds.containsAll(asList(2, 6, 7, 8, 9, 10, 11)));
        departmentIds = departmentDao.getDepartmentsBySourceControlNs(6, asList(TaxType.TRANSPORT), null, null);
        Assert.assertEquals(7, departmentIds.size());
        Assert.assertTrue(departmentIds.containsAll(asList(1, 2, 6, 7, 8, 9, 10)));
        departmentIds = departmentDao.getDepartmentsBySourceControlNs(6, asList(TaxType.DEAL), null, null);
        Assert.assertEquals(6, departmentIds.size());
        Assert.assertTrue(departmentIds.containsAll(asList(2, 6, 7, 8, 9, 10)));

        // Тесты для списка
        departmentIds = departmentDao.getDepartmentsBySourceControl(8, asList(TaxType.INCOME, TaxType.DEAL, TaxType.TRANSPORT), null, null);
        Assert.assertEquals(1, departmentIds.size());
        Assert.assertTrue(departmentIds.containsAll(asList(8)));
        departmentIds = departmentDao.getDepartmentsBySourceControl(6, asList(TaxType.INCOME, TaxType.DEAL, TaxType.TRANSPORT), null, null);
        Assert.assertEquals(5, departmentIds.size());
        Assert.assertTrue(departmentIds.containsAll(asList(6, 7, 9, 10, 11)));
        departmentIds = departmentDao.getDepartmentsBySourceControlNs(6, asList(TaxType.INCOME, TaxType.VAT), null, null);
        Assert.assertEquals(8, departmentIds.size());
        Assert.assertTrue(departmentIds.containsAll(asList(2, 6, 7, 8, 9, 10, 11, 12)));
    }

    @Test
    public void getEmptyDepartmentsBySourceControlNs() {
        List<Integer> departmentIds = departmentDao.getDepartmentsBySourceControlNs(-1, asList(TaxType.VAT), null, null);
        Assert.assertNotNull(departmentIds);
        Assert.assertTrue(departmentIds.isEmpty());
    }
}

package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentChangeDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentChange;
import com.aplana.sbrf.taxaccounting.model.DepartmentChangeOperationType;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DepartmentChangeDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DepartmentChangeDaoTest {

    @Autowired
    private DepartmentChangeDao departmentChangeDao;

    @Test
    public void addChange() {
        departmentChangeDao.clean();
        DepartmentChange departmentChange = new DepartmentChange();
        departmentChange.setOperationType(DepartmentChangeOperationType.CREATE);
        departmentChange.setId(101);
        departmentChange.setLevel(1);
        departmentChange.setParentId(3);
        departmentChange.setName("Name");
        departmentChange.setShortName("N.");
        departmentChange.setType(DepartmentType.TERR_BANK);
        departmentChange.setCode(101L);
        departmentChange.setTbIndex("10");
        departmentChange.setSbrfCode("11");
        departmentChange.setRegion("Region");
        departmentChange.setRegion("Region");
        departmentChange.setIsActive(true);
        departmentChange.setGarantUse(false);
        departmentChange.setSunrUse(false);
        departmentChangeDao.addChange(departmentChange);

        Assert.assertEquals(departmentChangeDao.getAllChanges().size(), 1);

        DepartmentChange depChange = departmentChangeDao.getAllChanges().get(0);
        Assert.assertEquals(departmentChange.getOperationType(), depChange.getOperationType());
        Assert.assertEquals(departmentChange.getId(), depChange.getId());
        Assert.assertEquals(departmentChange.getLevel(), depChange.getLevel());
        Assert.assertEquals(departmentChange.getParentId(), depChange.getParentId());
        Assert.assertEquals(departmentChange.getName(), depChange.getName());
        Assert.assertEquals(departmentChange.getShortName(), depChange.getShortName());
        Assert.assertEquals(departmentChange.getType(), depChange.getType());
        Assert.assertEquals(departmentChange.getCode(), depChange.getCode());
        Assert.assertEquals(departmentChange.getTbIndex(), depChange.getTbIndex());
        Assert.assertEquals(departmentChange.getSbrfCode(), depChange.getSbrfCode());
        Assert.assertEquals(departmentChange.getIsActive(), depChange.getIsActive());
        Assert.assertEquals(departmentChange.getGarantUse(), depChange.getGarantUse());
        Assert.assertEquals(departmentChange.getSunrUse(), depChange.getSunrUse());

    }

    @Test(expected = DaoException.class)
    public void addChange_1() {
        DepartmentChange departmentChange = new DepartmentChange();
        departmentChange.setOperationType(DepartmentChangeOperationType.CREATE);
        departmentChange.setId(101);
        departmentChange.setLevel(1);
        departmentChange.setParentId(3);
        //departmentChange.setName("Name");
        departmentChange.setShortName("N.");
        departmentChange.setType(DepartmentType.TERR_BANK);
        departmentChange.setCode(101L);
        departmentChange.setTbIndex("10");
        departmentChange.setSbrfCode("11");
        departmentChange.setRegion("Region");
        departmentChange.setRegion("Region");
        departmentChange.setIsActive(true);
        departmentChange.setGarantUse(false);
        departmentChange.setSunrUse(false);
        departmentChangeDao.addChange(departmentChange);
    }

    @Test
    public void addChange1() {
        DepartmentChange departmentChange = new DepartmentChange();
        departmentChange.setOperationType(DepartmentChangeOperationType.UPDATE);
        departmentChange.setId(1);
        departmentChange.setLevel(1);
        departmentChange.setParentId(0);
        departmentChange.setName("Name");
        departmentChange.setType(DepartmentType.TERR_BANK);
        departmentChange.setCode(1L);
        departmentChange.setIsActive(true);
        departmentChange.setGarantUse(false);
        departmentChange.setSunrUse(false);
        departmentChangeDao.addChange(departmentChange);
    }


    @Test
    public void addChange2() {
        DepartmentChange departmentChange = new DepartmentChange();
        departmentChange.setOperationType(DepartmentChangeOperationType.DELETE);
        departmentChange.setId(1);
        departmentChangeDao.addChange(departmentChange);
    }

    @Test
    public void clean() {
        DepartmentChange departmentChange = new DepartmentChange();
        departmentChange.setOperationType(DepartmentChangeOperationType.DELETE);
        departmentChange.setId(1);
        Assert.assertEquals(departmentChangeDao.getAllChanges().size(), 2);
        departmentChangeDao.addChange(departmentChange);
        Assert.assertEquals(departmentChangeDao.getAllChanges().size(), 3);
        departmentChangeDao.clean();
        Assert.assertEquals(departmentChangeDao.getAllChanges().size(), 0);
    }

    @Test
    public void clean2() {
        DepartmentChange departmentChange = new DepartmentChange();
        departmentChange.setOperationType(DepartmentChangeOperationType.DELETE);
        departmentChange.setId(1);
        departmentChangeDao.addChange(departmentChange);
        Assert.assertEquals(departmentChangeDao.getAllChanges().size(), 3);
        departmentChangeDao.clean(6);
        Assert.assertEquals(departmentChangeDao.getAllChanges().size(), 2);
        departmentChangeDao.clean(4);
        Assert.assertEquals(departmentChangeDao.getAllChanges().size(), 1);
        departmentChangeDao.clean();
        Assert.assertEquals(departmentChangeDao.getAllChanges().size(), 0);
    }

}

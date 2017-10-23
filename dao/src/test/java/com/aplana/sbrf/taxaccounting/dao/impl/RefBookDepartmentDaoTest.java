package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDataDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DepartmentDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookDepartmentDaoTest {

    @Autowired
    private RefBookDepartmentDataDao refBookDepartmentDataDao;

    @Test
    public void testFetchDepartments() {
        List<RefBookDepartment> result = refBookDepartmentDataDao.fetchDepartments(Lists.newArrayList(1));
        assertEquals(1, result.size());
        assertEquals(DepartmentType.ROOT_BANK, result.get(0).getType());
    }
}
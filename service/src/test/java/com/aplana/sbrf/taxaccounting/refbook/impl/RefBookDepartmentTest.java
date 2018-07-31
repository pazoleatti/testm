package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("RefBookDepartmentTest.xml")
public class RefBookDepartmentTest {
    @Autowired
    private RefBookDepartment refBookDepartment;
    @Autowired
    private RefBookDepartmentDao refBookDepartmentDao;

    @Test
    public void testGetRecordData() {
        List<Long> recordIds = new ArrayList<>();
        refBookDepartment.getRecordData(recordIds);
        verify(refBookDepartmentDao).getRecordData(recordIds);
    }
}

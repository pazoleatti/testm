package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookSimpleReadOnlyTest.xml"})
public class RefBookSimpleReadOnlyTest {
    @Autowired
    private RefBookSimpleReadOnly refBookSimpleReadOnly;
    @Autowired
    private RefBookSimpleDao refBookSimpleDao;
    @Autowired
    private RefBookDao refBookDao;

    @Test
    public void getRecordData() {
        RefBook refbook = Mockito.mock(RefBook.class);
        when(refbook.getId()).thenReturn(0L);
        refBookSimpleReadOnly.setRefBook(refbook);
        when(refBookDao.get(anyLong())).thenReturn(refbook);
        List<Long> recordDataIds = new ArrayList<>();
        refBookSimpleReadOnly.getRecordData(recordDataIds);
        Mockito.verify(refBookSimpleDao, Mockito.times(1)).getRecordData(refbook, recordDataIds);
    }
}

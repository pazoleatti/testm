package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("CommonRefBookServiceTest.xml")
public class CommonRefBookServiceTest {

    @Autowired
    private RefBookSimpleDao refBookSimpleDao;

    @Autowired
    private CommonRefBookService commonRefBookService;

    @Test
    public void testFetchAllRecords() {
        commonRefBookService.fetchAllRecords(0L, anyListOf(String.class), "", "", null);
        Mockito.verify(refBookSimpleDao, Mockito.times(1)).getRecords(any(RefBook.class), any(RefBookAttribute.class), anyString(), any(PagingParams.class), anyListOf(String.class), anyString(), anyString());
    }
}

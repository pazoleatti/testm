package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;

import static org.mockito.Mockito.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("RefBookServiceImplTest.xml")
public class RefBookServiceImplTest {

    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private RefBookService refBookService;
    @Autowired
    private CommonRefBookService commonRefBookService;

    @Test
    public void test_exportRefBookConfs() {
        refBookService.exportRefBookConfs(mock(TAUserInfo.class));
        verify(commonRefBookService).findAllVisible();
        verify(blobDataService).create(any(InputStream.class), eq("refBooksData.zip"));
        verify(blobDataService).get(anyString());
    }
}

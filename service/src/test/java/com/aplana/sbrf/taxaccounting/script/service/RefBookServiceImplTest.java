package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.script.service.impl.RefBookServiceImpl;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("RefBookServiceImplTest.xml")
public class RefBookServiceImplTest {
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private RefBookService refBookService;
    @Autowired
    private RefBookServiceImpl.ExportArchivePerformer exportArchivePerformer;

    @Test
    public void testexportRefBookConfs() throws IOException, InvocationTargetException, IllegalAccessException {
        BlobData blobData = mock(BlobData.class);
        when(exportArchivePerformer.createExportArchive()).thenReturn("uuid");
        when(blobDataService.get("uuid")).thenReturn(blobData);
        refBookService.exportRefBookConfs(mock(TAUserInfo.class));
        verify(blobDataService, Mockito.times(1)).get("uuid");
    }
}

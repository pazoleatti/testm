package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Dmitriy Levykin
 */
public class RefBookScriptingServiceImplTest {

    private RefBookScriptingService rbScriptingService;
    private static String SCRIPT_TEST_DATA = "def a = 1\nSystem.out.println(\"> Hello from test refbook script\")";

    @Before
    public void init() {
        rbScriptingService = new RefBookScriptingServiceImpl();
        // BlobDataDao
        BlobDataDao blobDao = mock(BlobDataDao.class);
        BlobData bd = new BlobData();
        bd.setInputStream(new ByteArrayInputStream(SCRIPT_TEST_DATA.getBytes()));
        when(blobDao.get("test-test")).thenReturn(bd);
        ReflectionTestUtils.setField(rbScriptingService, "blobDao", blobDao);
        // RefBookFactory
        RefBookFactory refBookFactory = mock(RefBookFactory.class);
        ReflectionTestUtils.setField(rbScriptingService, "refBookFactory", refBookFactory);
    }

    @Test
    public void executeScriptTest() {
        rbScriptingService.executeScript(null, 0L, FormDataEvent.IMPORT, new Logger(), null);
    }
}

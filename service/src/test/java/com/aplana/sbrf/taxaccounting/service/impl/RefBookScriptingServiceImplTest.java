package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Dmitriy Levykin
 */
public class RefBookScriptingServiceImplTest {

    private RefBookScriptingServiceImpl rbScriptingService;
    private static String SCRIPT_TEST_DATA =
            "switch (formDataEvent) {\n" +
            "   case FormDataEvent.IMPORT:\n" +
            "   break\n" +
            "}";

    @Before
    public void init() {
        rbScriptingService = new RefBookScriptingServiceImpl();
        // BlobDataDao
		BlobDataService blobDataService = mock(BlobDataService.class);
        BlobData bd = new BlobData();
        bd.setInputStream(new ByteArrayInputStream(SCRIPT_TEST_DATA.getBytes()));
        when(blobDataService.get("test-test")).thenReturn(bd);
        ReflectionTestUtils.setField(rbScriptingService, "blobDataService", blobDataService);
        // RefBookFactory
        RefBookFactory refBookFactory = mock(RefBookFactory.class);
        RefBook refBook = new RefBook();
        refBook.setScriptId("test-test");
        when(refBookFactory.get(0L)).thenReturn(refBook);
        ReflectionTestUtils.setField(rbScriptingService, "refBookFactory", refBookFactory);

        ApplicationContext ctx = mock(ApplicationContext.class);
        when(ctx.getBeansWithAnnotation(ScriptExposed.class)).thenReturn(new HashMap<String, Object>());
        rbScriptingService.setApplicationContext(ctx);

        LogEntryService logEntryService = mock(LogEntryService.class);
        ReflectionTestUtils.setField(rbScriptingService, "logEntryService", logEntryService);

        TransactionHelper tx = new TransactionHelper() {
            @Override
            public <T> T executeInNewTransaction(TransactionLogic<T> logic) {
                return logic.execute();
            }

            @Override
            public <T> T executeInNewReadOnlyTransaction(TransactionLogic<T> logic) {
                return logic.execute();
            }
        };
        ReflectionTestUtils.setField(rbScriptingService, "tx", tx);
    }

    @Test
    public void executeScriptTest() {
        Assert.assertTrue(rbScriptingService.executeScript(null, 0L, FormDataEvent.IMPORT, new Logger(), null));
    }

    @Test
    public void saveScript1() throws IOException {
        long refBookId = 0L;
        InputStream stream = RefBookScriptingServiceImplTest.class.getResourceAsStream("saveRefBookScript1.groovy");
        String script = IOUtils.toString(stream, "UTF-8");
        Logger logger = new Logger();
        rbScriptingService.saveScript(refBookId, script, logger);
    }

    @Test(expected = ServiceLoggerException.class)
    public void saveScript2() throws IOException {
        long refBookId = 0L;
        InputStream stream = RefBookScriptingServiceImplTest.class.getResourceAsStream("saveRefBookScript2.groovy");
        String script = IOUtils.toString(stream, "UTF-8");
        Logger logger = new Logger();
        rbScriptingService.saveScript(refBookId, script, logger);
    }
}

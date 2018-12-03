package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;
import com.aplana.sbrf.taxaccounting.service.TemplateChangesService;
import com.aplana.sbrf.taxaccounting.service.TransactionHelper;
import com.aplana.sbrf.taxaccounting.service.TransactionLogic;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.utils.ApplicationInfo;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import static org.mockito.Mockito.*;

/**
 * @author Dmitriy Levykin
 */
public class RefBookScriptingServiceImplTest {

    private RefBookScriptingServiceImpl rbScriptingService;
    private TemplateChangesService templateChangesService;
    private AuditService auditService;

    @Before
    public void init() {
        rbScriptingService = new RefBookScriptingServiceImpl();
        // BlobDataDao
        BlobDataService blobDataService = mock(BlobDataService.class);
        BlobData bd = new BlobData();
        String SCRIPT_TEST_DATA = "switch (formDataEvent) {\n" +
                "   case FormDataEvent.IMPORT:\n" +
                "   break\n" +
                "}";
        bd.setInputStream(new ByteArrayInputStream(SCRIPT_TEST_DATA.getBytes()));
        when(blobDataService.get("test-test")).thenReturn(bd);
        ReflectionTestUtils.setField(rbScriptingService, "blobDataService", blobDataService);
        // RefBookFactory
        CommonRefBookService commonRefBookService = mock(CommonRefBookService.class);
        RefBook refBook = new RefBook();
        refBook.setScriptId("test-test");
        refBook.setName("test");
        when(commonRefBookService.get(0L)).thenReturn(refBook);
        ReflectionTestUtils.setField(rbScriptingService, "commonRefBookService", commonRefBookService);

        ApplicationContext ctx = mock(ApplicationContext.class);
        when(ctx.getBeansWithAnnotation(ScriptExposed.class)).thenReturn(new HashMap<String, Object>());
        rbScriptingService.setApplicationContext(ctx);

        LogEntryService logEntryService = mock(LogEntryService.class);
        ReflectionTestUtils.setField(rbScriptingService, "logEntryService", logEntryService);

        templateChangesService = mock(TemplateChangesService.class);
        ReflectionTestUtils.setField(rbScriptingService, "templateChangesService", templateChangesService);

        auditService = mock(AuditService.class);
        ReflectionTestUtils.setField(rbScriptingService, "auditService", auditService);

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

        Properties versionInfoProperties = new Properties();
        versionInfoProperties.put("productionMode", "true");
        versionInfoProperties.put("version", "test");
        ApplicationInfo applicationInfo = new ApplicationInfo();
        ReflectionTestUtils.setField(rbScriptingService, "applicationInfo", applicationInfo);
        ReflectionTestUtils.setField(applicationInfo, "versionInfoProperties", versionInfoProperties);
    }

    @Test
    public void executeScriptTest() {
        Assert.assertTrue(rbScriptingService.executeScript(null, 0L, FormDataEvent.IMPORT, new Logger(), null));
    }

    @Test
    public void importScript() throws IOException {
        TAUserInfo userInfo = new TAUserInfo();
        long refBookId = 0L;
        InputStream stream = RefBookScriptingServiceImplTest.class.getResourceAsStream("saveRefBookScript1.groovy");
        String script = IOUtils.toString(stream, "UTF-8");
        Logger logger = new Logger();
        rbScriptingService.importScript(refBookId, script, logger, userInfo);
        ArgumentCaptor<TemplateChanges> argument = ArgumentCaptor.forClass(TemplateChanges.class);
        verify(templateChangesService, times(1)).save(argument.capture());
        Assert.assertEquals(FormDataEvent.SCRIPTS_IMPORT, argument.getAllValues().get(0).getEvent());
    }
}

package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookFactoryImpl;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.LoadRefBookDataService;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import com.aplana.sbrf.taxaccounting.service.SignService;
import org.junit.After;
import org.junit.Before;
import org.junit.rules.TemporaryFolder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * @author Dmitriy Levykin
 */
public class LoadRefBookDataServiceTest {
    LoadRefBookDataService service;

    private static String FILE_NAME_1 = "____852-4______________147212014__.rnu";
    private static String FILE_NAME_2 = "DS999999.nsi";
    private static String FILE_NAME_3 = "OKA99VVV.RR";
    private static String FILE_NAME_4 = "test.xlsx";

    private TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File uploadFolder;
    private File archiveFolder;
    private File errorFolder;

    private static final TAUserInfo USER_INFO = new TAUserInfo();
    private static final TAUserInfo SYSTEM_INFO = new TAUserInfo();

    static {
        // Пользователь
        TAUser user = new TAUser();
        user.setDepartmentId(6);
        user.setId(1);
        USER_INFO.setUser(user);
        // Система
        user = new TAUser();
        user.setId(TAUser.SYSTEM_USER_ID);
        user.setDepartmentId(3);
        SYSTEM_INFO.setUser(user);
    }

    @Before
    public void init() throws IOException {
        service = new LoadRefBookDataServiceImpl();
        RefBookFactory refBookFactory = mock(RefBookFactoryImpl.class);
        ReflectionTestUtils.setField(service, "refBookFactory", refBookFactory);
        mockAuditService();
        temporaryFolder.create();
        uploadFolder = temporaryFolder.newFolder("UPLOAD_DIRECTORY");
        archiveFolder = temporaryFolder.newFolder(ConfigurationParam.REF_BOOK_ARCHIVE_DIRECTORY.name());
        errorFolder = temporaryFolder.newFolder(ConfigurationParam.REF_BOOK_ERROR_DIRECTORY.name());
        mockConfigurationDao();
        mockRefBookScriptingService();
        mockSignService();
    }

    @After
    public void clean() throws IOException {
        uploadFolder.delete();
        archiveFolder.delete();
        archiveFolder.delete();
        temporaryFolder.delete();
    }

    private void mockAuditService() {
        AuditService auditService = mock(AuditService.class);
//        doAnswer(new Answer() {
//            @Override
//            public Object answer(InvocationOnMock invocation) throws Throwable {
//                System.out.println(invocation);
//                return null;
//            }
//        }).when(auditService).add(any(FormDataEvent.class), any(TAUserInfo.class), any(Integer.class), any(Integer.class),
//                anyString(), anyString(), any(Integer.class), anyString());
        ReflectionTestUtils.setField(service, "auditService", auditService);
    }

    private void mockConfigurationDao() {
        ConfigurationDao configurationDao = mock(ConfigurationDao.class);
        ConfigurationParamModel model = new ConfigurationParamModel();
        model.put(ConfigurationParam.DIASOFT_UPLOAD_DIRECTORY, 0, Arrays.asList("file://" + uploadFolder.getPath() + "/"));
        model.put(ConfigurationParam.OKATO_UPLOAD_DIRECTORY, 0, Arrays.asList("file://" + uploadFolder.getPath() + "/"));
        model.put(ConfigurationParam.REGION_UPLOAD_DIRECTORY, 0, Arrays.asList("file://" + uploadFolder.getPath() + "/"));
        model.put(ConfigurationParam.ACCOUNT_PLAN_UPLOAD_DIRECTORY, 0, Arrays.asList("file://" + uploadFolder.getPath() + "/"));
        model.put(ConfigurationParam.REF_BOOK_ARCHIVE_DIRECTORY, 0, Arrays.asList("file://" + archiveFolder.getPath() + "/"));
        model.put(ConfigurationParam.REF_BOOK_ERROR_DIRECTORY, 0, Arrays.asList("file://" + errorFolder.getPath() + "/"));
        when(configurationDao.getByDepartment(anyInt())).thenReturn(model);
        ReflectionTestUtils.setField(service, "configurationDao", configurationDao);
    }

    private void mockRefBookScriptingService() {
        RefBookScriptingService refBookScriptingService = mock(RefBookScriptingService.class);
        // Эмуляция успешного выполнения скрипта
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Map map = (Map) invocation.getArguments()[4];
                ScriptStatusHolder scriptStatusHolder = (ScriptStatusHolder) map.get("scriptStatusHolder");
                scriptStatusHolder.setScriptStatus(ScriptStatus.SUCCESS);
                return null;
            }
        }).when(refBookScriptingService).executeScript(any(TAUserInfo.class), anyInt(), any(FormDataEvent.class),
                any(Logger.class), any(Map.class));
        ReflectionTestUtils.setField(service, "refBookScriptingService", refBookScriptingService);
    }

    private void mockSignService() {
        SignService signService = mock(SignService.class);
        when(signService.checkSign(anyString(), anyString(), anyInt(), any(Logger.class))).thenReturn(new Pair<Boolean, Set<String>>(true, new HashSet<String>()));
        ReflectionTestUtils.setField(service, "signService", signService);

        LockDataService lockService = mock(LockDataService.class);
        when(lockService.lock(anyString(), anyInt(), anyString())).thenReturn(null);
        ReflectionTestUtils.setField(service, "lockService", lockService);

        RefBookDao refBookDao = mock(RefBookDao.class);
        RefBook refBook = new RefBook();
        refBook.setAttributes(new ArrayList<RefBookAttribute>());
        when(refBookDao.get(anyLong())).thenReturn(refBook);
        ReflectionTestUtils.setField(service, "refBookDao", refBookDao);
    }
}

package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.FileNotFoundException;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("ScriptExecutionServiceTest.xml")
public class ScriptExecutionServiceTest {

    private final String COMMON = ClassUtils.classPackageAsResourcePath(getClass()) + "/scriptImport/";

    private ScriptExecutionService scriptExecutionService;
    private DeclarationTemplateService declarationTemplateService;
    private LockDataService lockDataService;
    private CommonRefBookService commonRefBookService;
    private RefBookDao refBookDao;
    private RefBookScriptingService refBookScriptingService;

    @Autowired
    TAUserDao userDao;

    @Before
	public void init(){
        scriptExecutionService = new ScriptExecutionServiceImpl();

        LogEntryService logEntryService = mock(LogEntryService.class);
        ReflectionTestUtils.setField(scriptExecutionService, "logEntryService", logEntryService);

        AuditService auditService = mock(AuditService.class);
        ReflectionTestUtils.setField(scriptExecutionService, "auditService", auditService);

        DeclarationType declarationType10 = new DeclarationType();
        declarationType10.setId(10);
        DeclarationTemplate declarationTemplate10 = new DeclarationTemplate();
        declarationTemplate10.setId(10);
        declarationTemplate10.setName("template10");
        declarationTemplate10.setType(declarationType10);

        DeclarationType declarationType = new DeclarationType();
        declarationType.setId(11);
        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setId(11);
        declarationTemplate.setName("template");
        declarationTemplate.setType(declarationType);

        declarationTemplateService = mock(DeclarationTemplateService.class);
        when(declarationTemplateService.get(declarationTemplate.getId(), 2015)).thenReturn(declarationTemplate.getId());
        when(declarationTemplateService.get(declarationTemplate.getId())).thenReturn(declarationTemplate);
        ReflectionTestUtils.setField(scriptExecutionService, "declarationTemplateService", declarationTemplateService);

        RefBook refBook = new RefBook();
        refBook.setId(10L);
        refBook.setName("ref10");

        refBookDao = mock(RefBookDao.class);
        when(refBookDao.isRefBookExist(refBook.getId())).thenReturn(true);
        when(refBookDao.get(refBook.getId())).thenReturn(refBook);
        ReflectionTestUtils.setField(scriptExecutionService, "refBookDao", refBookDao);

        refBookScriptingService = mock(RefBookScriptingService.class);
        ReflectionTestUtils.setField(scriptExecutionService, "refBookScriptingService", refBookScriptingService);

        TAUserService userService = mock(TAUserServiceImpl.class);
        when(userService.getUser(anyInt())).thenAnswer(new Answer<TAUser>() {
            @Override
            public TAUser answer(InvocationOnMock invocation) throws Throwable {
                Integer userId = (Integer)invocation.getArguments()[0];
                TAUser user = new TAUser();
                user.setLogin(String.valueOf(userId));
                user.setId(userId);
                return user;
            }
        });
        ReflectionTestUtils.setField(scriptExecutionService, "userService", userService);

        lockDataService = mock(LockDataService.class);
        ReflectionTestUtils.setField(scriptExecutionService, "lockDataService", lockDataService);

        commonRefBookService = mock(CommonRefBookService.class);
        ReflectionTestUtils.setField(scriptExecutionService, "commonRefBookService", commonRefBookService);
    }


    /**
     * Некорректный архив
     */
	@Test
	public void test1() throws FileNotFoundException {
        Logger logger = new Logger();
        String fileName = "scripts.zip";
        TAUserInfo userInfo = new TAUserInfo();
        try {
            scriptExecutionService.importScripts(logger, Thread.currentThread().getContextClassLoader().getResourceAsStream(COMMON + fileName), fileName, userInfo);
        } finally {
            Assert.assertEquals(1, logger.getEntries().size());
            Assert.assertEquals("Импорт завершен", logger.getEntries().get(0).getMessage());
        }
    }


    /**
     * Нормальная загрузка макета, макет заблокирован текущим пользователем
     */
    @Test
    public void test2(){
        Logger logger = new Logger();
        String fileName = "declaration_template.zip";
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(new TAUser(){{setId(1);}});

        DeclarationTemplate declarationTemplate = declarationTemplateService.get(11);
        LockData lockData = new LockData();
        lockData.setUserId(1);
        when(lockDataService.findLock(LockData.LockObjects.DECLARATION_TEMPLATE.name() + "_" + declarationTemplate.getId())).thenReturn(lockData);

        scriptExecutionService.importScripts(logger, Thread.currentThread().getContextClassLoader().getResourceAsStream(COMMON + fileName), fileName, userInfo);
        Assert.assertEquals(2, logger.getEntries().size());
        Assert.assertEquals("Выполнен импорт скрипта для макета налоговой формы формы \"template\" из файла \"11-2015.groovy\"", logger.getEntries().get(0).getMessage());
        Assert.assertEquals("Импорт завершен", logger.getEntries().get(1).getMessage());
    }

    /**
     * Есть блокировка макета, макет заблокирован другим пользователем
     */
    @Test
    public void test3(){
        Logger logger = new Logger();
        String fileName = "declaration_template.zip";
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(new TAUser(){{setId(1);}});

        DeclarationTemplate declarationTemplate = declarationTemplateService.get(11);
        LockData lockData = new LockData();
        lockData.setUserId(10);
        when(lockDataService.findLock(LockData.LockObjects.DECLARATION_TEMPLATE.name() + "_" + declarationTemplate.getId())).thenReturn(lockData);

        scriptExecutionService.importScripts(logger, Thread.currentThread().getContextClassLoader().getResourceAsStream(COMMON + fileName), fileName, userInfo);
        Assert.assertEquals(2, logger.getEntries().size());
        Assert.assertEquals("Макет налоговой формы \"template\" заблокирован пользователем с логином \"10\". Макет пропущен.", logger.getEntries().get(0).getMessage());
        Assert.assertEquals("Импорт завершен", logger.getEntries().get(1).getMessage());
    }

    /**
     * Нормальная загрузка макетов нф/декларации/справочников
     */
    @Test
    public void test4(){
        Logger logger = new Logger();
        String fileName = "template2.zip";
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(new TAUser(){{setId(1);}});

        when(lockDataService.findLock(anyString())).thenReturn(null);

        scriptExecutionService.importScripts(logger, Thread.currentThread().getContextClassLoader().getResourceAsStream(COMMON + fileName), fileName, userInfo);
        Assert.assertEquals(3, logger.getEntries().size());
        Assert.assertEquals("Выполнен импорт скрипта для макета налоговой формы формы \"template\" из файла \"11-2015.groovy\"", logger.getEntries().get(0).getMessage());
        Assert.assertEquals("Выполнен импорт скрипта для справочника \"ref10\" из файла \"10.groovy\"", logger.getEntries().get(1).getMessage());
        Assert.assertEquals("Импорт завершен", logger.getEntries().get(2).getMessage());
    }

    /**
     * Загрузка для не существующих макетов нф/декларации/справочников
     */
    @Test
    public void test5(){
        Logger logger = new Logger();
        String fileName = "template3.zip";
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(new TAUser(){{setId(1);}});

        when(lockDataService.findLock(anyString())).thenReturn(null);
        when(declarationTemplateService.get(anyInt(), anyInt())).thenReturn(null);
        when(refBookDao.isRefBookExist(anyLong())).thenReturn(false);

        scriptExecutionService.importScripts(logger, Thread.currentThread().getContextClassLoader().getResourceAsStream(COMMON + fileName), fileName, userInfo);
        Assert.assertEquals(3, logger.getEntries().size());
        Assert.assertEquals("Макет налоговой формы, указанный в файле \"12-2015.groovy\" не существует. Файл пропущен.", logger.getEntries().get(0).getMessage());
        Assert.assertEquals("Справочник, указанный в файле \"12.groovy\" не существует. Файл пропущен.", logger.getEntries().get(1).getMessage());
        Assert.assertEquals("Импорт завершен", logger.getEntries().get(2).getMessage());
    }

    /**
     * Загрузка для макетов нф/декларации/справочников с неправиньмы форматом имени файла
     */
    @Test
    public void test6(){
        Logger logger = new Logger();
        String fileName = "template4.zip";
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(new TAUser(){{setId(1);}});

        when(lockDataService.findLock(anyString())).thenReturn(null);
        when(declarationTemplateService.get(anyInt(), anyInt())).thenReturn(null);
        when(refBookDao.isRefBookExist(anyLong())).thenReturn(false);

        scriptExecutionService.importScripts(logger, Thread.currentThread().getContextClassLoader().getResourceAsStream(COMMON + fileName), fileName, userInfo);
        Assert.assertEquals(3, logger.getEntries().size());
        Assert.assertEquals("Наименование файла \"1555.groovy\" некорректно. Файл пропущен.", logger.getEntries().get(0).getMessage());
        Assert.assertEquals("Наименование файла \"12-10.groovy\" некорректно. Файл пропущен.", logger.getEntries().get(1).getMessage());
        Assert.assertEquals("Импорт завершен", logger.getEntries().get(2).getMessage());
    }

    /**
     * Некорректный каталог
     */
    @Test
    public void test7(){
        Logger logger = new Logger();
        String fileName = "template5.zip";
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(new TAUser(){{setId(1);}});

        when(lockDataService.findLock(anyString())).thenReturn(null);
        when(declarationTemplateService.get(anyInt(), anyInt())).thenReturn(null);
        when(refBookDao.isRefBookExist(anyLong())).thenReturn(false);

        scriptExecutionService.importScripts(logger, Thread.currentThread().getContextClassLoader().getResourceAsStream(COMMON + fileName), fileName, userInfo);
        Assert.assertEquals(5, logger.getEntries().size());
        Assert.assertEquals("Пропущен каталог \"template3"+ File.separator+"declaration_template\", так как его имя не поддерживается", logger.getEntries().get(0).getMessage());
        Assert.assertEquals("Пропущен каталог \"template3"+ File.separator+"form_template\", так как его имя не поддерживается", logger.getEntries().get(1).getMessage());
        Assert.assertEquals("Пропущен каталог \"template3"+ File.separator+"ref_book\", так как его имя не поддерживается", logger.getEntries().get(2).getMessage());
        Assert.assertEquals("Пропущен каталог \"template3\", так как его имя не поддерживается", logger.getEntries().get(3).getMessage());
        Assert.assertEquals("Импорт завершен", logger.getEntries().get(4).getMessage());
    }
}

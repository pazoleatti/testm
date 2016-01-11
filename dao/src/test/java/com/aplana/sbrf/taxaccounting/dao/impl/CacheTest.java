/*
package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.cache.ExtendedSimpleCacheManager;
import com.aplana.sbrf.taxaccounting.cache.KeyWrapper;
import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.dao.DaoObject;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.impl.cache.CacheConstants;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import org.apache.commons.collections.MapUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

*/
/**
 * User: avanteev
 * Тест для проверки кэшей
 *//*

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"CacheTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CacheTest {

    @Autowired
    private DaoObject daoObject;

    @Autowired
    private ApplicationContext applicationContext;

    private ExtendedSimpleCacheManager cacheManager = null;

    private static InitialContext ic = null;

    @Autowired
    private FormTemplateDao formTemplateDao;

    @Autowired
    private DeclarationTemplateDao declarationTemplateDao;

    @Autowired
    private DeclarationTypeDao declarationTypeDao;

	@Autowired
	private BlobDataDao blobDataDao;

    private static String FORM_TYPE_JNDI = "services/cache/aplana/taxaccounting/FormType";
    private static String FORM_TEMPLATE_JNDI = "services/cache/aplana/taxaccounting/FormTemplate";
    private static String DECLARATION_TYPE_JNDI = "services/cache/aplana/taxaccounting/DeclarationType";
    private static String DECLARATION_TEMPLATE_JNDI = "services/cache/aplana/taxaccounting/DeclarationTemplate";
    private static String DEPARTMENT_JNDI = "services/cache/aplana/taxaccounting/Department";
    private static String USER_JNDI = "services/cache/aplana/taxaccounting/User";
    private static String PERMANENT_DATA_JNDI = "services/cache/aplana/taxaccounting/PermanentData";
    private static String DATA_BLOBS_CACHE_JNDI = "services/cache/aplana/taxaccounting/DataBlobsCache";

	private static final String SAMPLE_BLOB_ID = UUID.randomUUID().toString();
    private static SimpleNamingContextBuilder builder = null;

    @BeforeClass
    public static void initContext() throws NamingException {
        builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
        builder.bind(FORM_TYPE_JNDI, new HashMap<Object, Object>());
        builder.bind(FORM_TEMPLATE_JNDI, new HashMap<Object, Object>());
        builder.bind(DECLARATION_TYPE_JNDI, new HashMap<Object, Object>());
        builder.bind(DECLARATION_TEMPLATE_JNDI, new HashMap<Object, Object>());
        builder.bind(DEPARTMENT_JNDI, new HashMap<Object, Object>());
        builder.bind(USER_JNDI, new HashMap<Object, Object>());
        builder.bind(PERMANENT_DATA_JNDI, new HashMap<Object, Object>());
        builder.bind(DATA_BLOBS_CACHE_JNDI, new HashMap<Object, Object>());
        builder.activate();
        ic = new InitialContext();
    }

    @Before
    public void init() throws UnsupportedEncodingException {
        cacheManager = applicationContext.getBean(ExtendedSimpleCacheManager.class);
		// генерим тестовый блоб
		String sampleBlobData = "sample text";
		BlobData blob = new BlobData();
		blob.setCreationDate(new Date());
		blob.setReportId(SAMPLE_BLOB_ID);
		blob.setInputStream(new ByteArrayInputStream(sampleBlobData.getBytes("UTF-8")));
		blobDataDao.create(blob);
    }

    @After
    public void clearCache(){
        cacheManager.clearAll();
    }

    @Test
    public void testFormTemplateCache() throws NamingException {
        FormTemplate formTemplate = formTemplateDao.get(1);
        formTemplate.setFixedRows(false);
        formTemplate.setVersion(new Date());
        formTemplate.setStatus(VersionedObjectStatus.NORMAL);
        formTemplate.setName("name_3");
        formTemplate.setFullName("fullname_3");
        formTemplate.setHeader("header_3");
        formTemplate.setScript("test_script");
        DataRow<Cell> rows = new DataRow<Cell>(FormDataUtils.createCells(formTemplate.getColumns(), formTemplate.getStyles()));
        formTemplate.getRows().add(rows);
        DataRow<HeaderCell> headers1 = new DataRow<HeaderCell>(FormDataUtils.createHeaderCells(formTemplate.getColumns()));
        formTemplate.getHeaders().add(headers1);
        formTemplateDao.save(formTemplate);

        //Проверяем кэширование dataRows
        */
/*formTemplateDao.getDataCells(formTemplate);
        checkExistInCache(CacheConstants.FORM_TEMPLATE, FORM_TEMPLATE_JNDI, String.valueOf(formTemplate.getId()) + "_data_rows", rows);*//*


        //Проверяем кэширование headers
        */
/*formTemplateDao.getHeaderCells(formTemplate);
        checkExistInCache(CacheConstants.FORM_TEMPLATE, FORM_TEMPLATE_JNDI, String.valueOf(formTemplate.getId()) + "_data_headers", headers1);*//*


        //Проверяем кэширование скрипта
        formTemplate = formTemplateDao.get(1);
        Assert.assertNull(formTemplate.getScript());//проверил что убрали из маппера.
        formTemplate.setScript(formTemplateDao.getFormTemplateScript(1));
        Assert.assertEquals(2, ((HashMap) ic.lookup(FORM_TEMPLATE_JNDI)).size());
        Assert.assertEquals("test_script",
                ((HashMap) ic.lookup(FORM_TEMPLATE_JNDI)).get(new KeyWrapper(CacheConstants.FORM_TEMPLATE, String.valueOf(formTemplate.getId()) + "_script")));

        //После получения скрипта(должен закэшироваться)
        Assert.assertEquals("test_script", formTemplate.getScript());
        //Меняем скрипт
        formTemplate.setScript("after_script");
        formTemplate.setFullName("fullname");

        formTemplateDao.save(formTemplate);
        //Кэш для FT должен очиститься
        Assert.assertEquals("fullname", formTemplate.getFullName());
        Assert.assertEquals("after_script", formTemplateDao.getFormTemplateScript(1));

        cacheManager.clearAll();
    }

    @Test
    public void testDeclarationTemplate() throws NamingException {
        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setId(1);
        declarationTemplate.setName("Декларация");
        */
/*declarationTemplate.setActive(true);*//*

        declarationTemplate.setVersion(new Date());
        declarationTemplate.setCreateScript("MyScript");
        declarationTemplate.setJrxmlBlobId(SAMPLE_BLOB_ID);
        DeclarationType declarationType = declarationTypeDao.get(1);
        declarationTemplate.setType(declarationType);
        declarationTemplate.setStatus(VersionedObjectStatus.DRAFT);
        declarationTemplateDao.save(declarationTemplate);

        DeclarationTemplate savedDeclarationTemplate = declarationTemplateDao.get(1);
        assertEquals(1, savedDeclarationTemplate.getId().intValue());

        //Проверка заполнения uuid
        declarationTemplateDao.setJrxml(savedDeclarationTemplate.getId(), SAMPLE_BLOB_ID);
        assertEquals(SAMPLE_BLOB_ID, declarationTemplateDao.get(savedDeclarationTemplate.getId()).getJrxmlBlobId());

        //Проверка кэширования тела скрипта
        declarationTemplateDao.getDeclarationTemplateScript(1);
        Assert.assertEquals("MyScript",
                ((HashMap) ic.lookup(DECLARATION_TEMPLATE_JNDI)).get(new KeyWrapper(CacheConstants.DECLARATION_TEMPLATE,
                        String.valueOf(declarationTemplate.getId()) + "_script")));
        cacheManager.clearAll();
    }

    private void printJNDI(String name, String jndiName) throws NamingException {
        HashMap map =((HashMap) ic.lookup(jndiName));
        MapUtils.debugPrint(System.out, name, map);
    }

    @Test
    public void cacheTest(){
        assertEquals(daoObject.getCachedNumberFromCachedMethod(), daoObject.getCachedNumberFromCachedMethod());
    }

    @Test
    public void cacheTest2(){
        int v = daoObject.getCachedNumberFromCachedMethod();
        cacheManager.clearAll();
        assertNotEquals(v, daoObject.getCachedNumberFromCachedMethod());
    }

    @Test
    public void cacheTestPrivateInvocation(){
        assertEquals(daoObject.getCachedNumberFromPrivateInsideMethod("123"), daoObject.getCachedNumberFromPrivateInsideMethod("123"));
    }

    @Test
    public void cacheTestPrivateInvocation2(){
        int v = daoObject.getCachedNumberFromPrivateInsideMethod("1234");
        cacheManager.clearAll();
        assertNotEquals(v, daoObject.getCachedNumberFromPrivateInsideMethod("1234"));
    }
}
*/

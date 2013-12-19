package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.cache.ExtendedSimpleCacheManager;
import com.aplana.sbrf.taxaccounting.cache.KeyWrapper;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.impl.cache.CacheConstants;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import org.apache.commons.collections.MapUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * User: avanteev
 * Тест для проверки кэшей
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"CacheTest.xml"})
@Transactional
public class CacheTest {

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

    private static String FORM_TYPE_JNDI = "services/cache/aplana/taxaccounting/FormType";
    private static String FORM_TEMPLATE_JNDI = "services/cache/aplana/taxaccounting/FormTemplate";
    private static String DECLARATION_TYPE_JNDI = "services/cache/aplana/taxaccounting/DeclarationType";
    private static String DECLARATION_TEMPLATE_JNDI = "services/cache/aplana/taxaccounting/DeclarationTemplate";
    private static String DEPARTMENT_JNDI = "services/cache/aplana/taxaccounting/Department";
    private static String USER_JNDI = "services/cache/aplana/taxaccounting/User";
    private static String PERMANENT_DATA_JNDI = "services/cache/aplana/taxaccounting/PermanentData";
    private static String DATA_BLOBS_CACHE_JNDI = "services/cache/aplana/taxaccounting/DataBlobsCache";

    @BeforeClass
    public static void initContext() throws NamingException {
        SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
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
    public void init(){
        cacheManager = applicationContext.getBean(ExtendedSimpleCacheManager.class);
    }

    @Test
    public void testFormTemplateCache() throws NamingException {
        FormTemplate formTemplate = formTemplateDao.get(1);
        formTemplate.setNumberedColumns(true);
        formTemplate.setFixedRows(false);
        formTemplate.setVersion("321");
        formTemplate.setActive(true);
        formTemplate.setName("name_3");
        formTemplate.setFullName("fullname_3");
        formTemplate.setCode("code_3");
        formTemplate.setScript("test_script");
        DataRow<Cell> rows = new DataRow<Cell>(FormDataUtils.createCells(formTemplate.getColumns(), formTemplate.getStyles()));
        formTemplate.getRows().add(rows);
        DataRow<HeaderCell> headers1 = new DataRow<HeaderCell>(FormDataUtils.createHeaderCells(formTemplate.getColumns()));
        formTemplate.getHeaders().add(headers1);
        formTemplateDao.save(formTemplate);

        //Проверяем кэширование dataRows
        formTemplateDao.getDataCells(formTemplate);
        checkExistInCache(CacheConstants.FORM_TEMPLATE, FORM_TEMPLATE_JNDI, String.valueOf(formTemplate.getId()) + "_data_rows", rows);

        //Проверяем кэширование headers
        formTemplateDao.getHeaderCells(formTemplate);
        checkExistInCache(CacheConstants.FORM_TEMPLATE, FORM_TEMPLATE_JNDI, String.valueOf(formTemplate.getId()) + "_data_headers", headers1);

        //Проверяем кэширование скрипта
        formTemplate = formTemplateDao.get(1);
        Assert.assertNull(formTemplate.getScript());//проверил что убрали из маппера.
        formTemplate.setScript(formTemplateDao.getFormTemplateScript(1));
        checkExistInCache(CacheConstants.FORM_TEMPLATE, FORM_TEMPLATE_JNDI, String.valueOf(formTemplate.getId()) + "_script", "test_script");

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
        declarationTemplate.setEdition(1);
        declarationTemplate.setActive(true);
        declarationTemplate.setVersion("0.01");
        declarationTemplate.setCreateScript("MyScript");
        String uuid1 = UUID.randomUUID().toString();
        declarationTemplate.setJrxmlBlobId(uuid1);
        DeclarationType declarationType = declarationTypeDao.get(1);
        declarationTemplate.setDeclarationType(declarationType);

        declarationTemplateDao.save(declarationTemplate);

        DeclarationTemplate savedDeclarationTemplate = declarationTemplateDao.get(1);
        assertEquals(1, savedDeclarationTemplate.getId().intValue());

        //Проверка заполнения uuid
        String uuid2 = UUID.randomUUID().toString();
        declarationTemplateDao.setJrxml(savedDeclarationTemplate.getId(), uuid2);
        assertEquals(uuid2, declarationTemplateDao.get(savedDeclarationTemplate.getId()).getJrxmlBlobId());

        //Проверка кэширования тела скрипта
        declarationTemplateDao.getDeclarationTemplateScript(1);
        checkExistInCache(CacheConstants.DECLARATION_TEMPLATE, DECLARATION_TEMPLATE_JNDI, String.valueOf(declarationTemplate.getId()) + "_script", "MyScript");
        cacheManager.clearAll();
    }

    private void printJNDI(String name, String jndiName) throws NamingException {
        HashMap map =((HashMap) ic.lookup(jndiName));
        MapUtils.debugPrint(System.out, name, map);
    }

    private <T> void checkExistInCache(String cacheName, String jndiName, String cacheId, T assertString) throws NamingException {
        HashMap map =((HashMap) ic.lookup(jndiName));
        if (assertString instanceof DataRow)
            Assert.assertEquals(((DataRow)assertString).size(), ((List<DataRow>)map.get(new KeyWrapper(cacheName, cacheId))).get(0).size());
        else
            Assert.assertEquals(assertString, map.get(new KeyWrapper(cacheName, cacheId)));
    }
}

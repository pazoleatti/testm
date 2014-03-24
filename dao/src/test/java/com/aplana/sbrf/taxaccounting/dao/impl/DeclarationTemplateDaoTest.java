package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DeclarationTemplateDaoTest.xml"})
@Transactional
public class DeclarationTemplateDaoTest {
	@Autowired
	private DeclarationTemplateDao declarationTemplateDao;

	@Autowired
	private DeclarationTypeDao declarationTypeDao;

	@Autowired
	private BlobDataDao blobDataDao;

	private static final String SAMPLE_BLOB_ID = UUID.randomUUID().toString();

	@Before
	public void init() throws UnsupportedEncodingException {
		// генерим тестовый блоб
		String sampleBlobData = "sample text";
		BlobData blob = new BlobData();
		blob.setCreationDate(new Date());
		blob.setDataSize(sampleBlobData.length());
		blob.setUuid(SAMPLE_BLOB_ID);
		blob.setType(0);
		blob.setInputStream(new ByteArrayInputStream(sampleBlobData.getBytes("UTF-8")));
		blobDataDao.create(blob);
	}

	@Test
	public void testListAll() {
		assertEquals(6, declarationTemplateDao.listAll().size());
	}

	@Test
	public void testGet() {
		DeclarationTemplate d1 = declarationTemplateDao.get(1);
		assertEquals(1, d1.getId().longValue());
		assertEquals('T', d1.getType().getTaxType().getCode());
		assertFalse(d1.isActive());
        assertEquals("Декларация 1", d1.getName());

		DeclarationTemplate d2 = declarationTemplateDao.get(2);
		assertEquals(2, d2.getId().longValue());
		assertEquals('T', d2.getType().getTaxType().getCode());
		assertTrue(d2.isActive());
	}
	
	@Test(expected=DaoException.class)
	public void testGetNotExisted() {
		declarationTemplateDao.get(1000);
	}

	@Test
	public void testSaveNew() {

		DeclarationTemplate declarationTemplate = new DeclarationTemplate();
		declarationTemplate.setActive(true);
        declarationTemplate.setName("Декларация");
		declarationTemplate.setVersion(new Date());
		declarationTemplate.setCreateScript("MyScript");
		DeclarationType declarationType = declarationTypeDao.get(1);
		declarationTemplate.setType(declarationType);
        declarationTemplate.setJrxmlBlobId("1");
        declarationTemplate.setStatus(VersionedObjectStatus.NORMAL);

		int id = declarationTemplateDao.save(declarationTemplate);

		DeclarationTemplate savedDeclarationTemplate = declarationTemplateDao.get(id);
		assertEquals(id, savedDeclarationTemplate.getId().intValue());
		assertNull(savedDeclarationTemplate.getCreateScript());
		assertEquals(declarationType.getId(), savedDeclarationTemplate.getType().getId());
		assertTrue(savedDeclarationTemplate.isActive());
        assertEquals(null, savedDeclarationTemplate.getXsdId());
        assertEquals(3, savedDeclarationTemplate.getEdition().intValue());
	}

	@Test
	public void testSaveExist() {

		DeclarationTemplate declarationTemplate = new DeclarationTemplate();
		declarationTemplate.setId(1);
        declarationTemplate.setStatus(VersionedObjectStatus.FAKE);
        declarationTemplate.setName("Декларация");
		declarationTemplate.setEdition(1);
		declarationTemplate.setActive(true);
		declarationTemplate.setVersion(new Date());
		declarationTemplate.setCreateScript("MyScript");
        declarationTemplate.setJrxmlBlobId("1");
		DeclarationType declarationType = declarationTypeDao.get(1);
		declarationTemplate.setType(declarationType);

		int savedId = declarationTemplateDao.save(declarationTemplate);

		DeclarationTemplate savedDeclarationTemplate = declarationTemplateDao.get(savedId);
		assertEquals(1, savedDeclarationTemplate.getId().intValue());
		assertNull(savedDeclarationTemplate.getCreateScript());
		assertEquals(declarationType.getId(), savedDeclarationTemplate.getType().getId());
        assertEquals(null, savedDeclarationTemplate.getXsdId());
        assertEquals(VersionedObjectStatus.FAKE, savedDeclarationTemplate.getStatus());
	}

    @Test
    public void testSetJrxml() {
        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setId(1);
        declarationTemplate.setEdition(1);
        declarationTemplate.setName("Декларация");
        declarationTemplate.setActive(true);
        declarationTemplate.setStatus(VersionedObjectStatus.FAKE);
        declarationTemplate.setVersion(new Date());
        declarationTemplate.setCreateScript("MyScript");
        declarationTemplate.setJrxmlBlobId(SAMPLE_BLOB_ID);
        DeclarationType declarationType = declarationTypeDao.get(1);
        declarationTemplate.setType(declarationType);

        declarationTemplateDao.save(declarationTemplate);

        DeclarationTemplate savedDeclarationTemplate = declarationTemplateDao.get(1);
        assertEquals(1, savedDeclarationTemplate.getId().intValue());

        declarationTemplateDao.setJrxml(savedDeclarationTemplate.getId(), SAMPLE_BLOB_ID);
        assertEquals(SAMPLE_BLOB_ID, declarationTemplateDao.get(savedDeclarationTemplate.getId()).getJrxmlBlobId());
    }

	/*@Test(expected = DaoException.class)
	public void testSaveExistWithBadEdition() {
		DeclarationTemplate declarationTemplate = new DeclarationTemplate();
		declarationTemplate.setId(1);
		declarationTemplate.setEdition(1000);
		declarationTemplate.setActive(true);
        declarationTemplate.setStatus(VersionedObjectStatus.FAKE);
		declarationTemplate.setVersion(new Date());
		declarationTemplate.setCreateScript("MyScript");
		DeclarationType declarationType = declarationTypeDao.get(1);
		declarationTemplate.setType(declarationType);

		declarationTemplateDao.save(declarationTemplate);

	}*/

	@Test
	public void getActiveDeclarationTemplateIdTest() {
        assertEquals(1, declarationTemplateDao.getActiveDeclarationTemplateId(1, 1));
	}

	@Test(expected = DaoException.class)
	public void getActiveDeclarationTemplateIdMoreThanOneTest() {
        declarationTemplateDao.getActiveDeclarationTemplateId(3, 3);
	}

	@Test(expected = DaoException.class)
	public void getActiveDeclarationTemplateIdEmptyTest() {
        declarationTemplateDao.getActiveDeclarationTemplateId(3, 2);
	}

    @Test
    public void getDeclarationTemplateScriptTest(){
        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setId(1);
        declarationTemplate.setEdition(1);
        declarationTemplate.setName("Декларация");
        declarationTemplate.setStatus(VersionedObjectStatus.NORMAL);
        declarationTemplate.setActive(true);
        declarationTemplate.setVersion(new Date());
        declarationTemplate.setCreateScript("MyScript");
        declarationTemplate.setJrxmlBlobId("1");
        DeclarationType declarationType = declarationTypeDao.get(1);
        declarationTemplate.setType(declarationType);

        declarationTemplateDao.save(declarationTemplate);

        DeclarationTemplate savedDeclarationTemplate = declarationTemplateDao.get(1);
        assertEquals(1, savedDeclarationTemplate.getId().intValue());
        assertEquals("MyScript", declarationTemplateDao.getDeclarationTemplateScript(1));
    }

    @Test
    public void testGetDeclarationTemplateVersions(){
        List<Integer> list = new ArrayList<Integer>();
        list.add(VersionedObjectStatus.NORMAL.getId());
        list.add(VersionedObjectStatus.DRAFT.getId());

        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, Calendar.JANUARY, 1);
        Date actualStartVersion = calendar.getTime();
        calendar.clear();

        Assert.assertEquals(2, declarationTemplateDao.getDeclarationTemplateVersions(1, 0, list, actualStartVersion, null).size());
    }

    @Test
    public void testGetDTVersionEndDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, Calendar.JANUARY, 1);
        Date actualBeginVersion = new Date(calendar.getTime().getTime());
        calendar.clear();

        calendar.set(2013, Calendar.DECEMBER, 31, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Assert.assertEquals(calendar.getTime(), declarationTemplateDao.getDTVersionEndDate(1, 1, actualBeginVersion));

    }

    @Test
    public void testGetNearestDTVersionIdRight(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, Calendar.JANUARY, 1);
        Date actualBeginVersion = new Date(calendar.getTime().getTime());
        calendar.clear();

        List<Integer> list = new ArrayList<Integer>();
        list.add(VersionedObjectStatus.NORMAL.getId());
        list.add(VersionedObjectStatus.DRAFT.getId());
        Assert.assertEquals(1, declarationTemplateDao.getNearestDTVersionIdRight(1, list, actualBeginVersion));

    }

    @Test
    public void testGetNearestDTVersionIdLeft(){
        List<Integer> list = new ArrayList<Integer>();
        list.add(VersionedObjectStatus.NORMAL.getId());
        list.add(VersionedObjectStatus.DRAFT.getId());

        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, Calendar.JANUARY, 1);
        Date actualBeginVersion = calendar.getTime();
        calendar.clear();

        Assert.assertEquals(0, declarationTemplateDao.getNearestDTVersionIdLeft(1, list, actualBeginVersion));

    }

    @Test
    public void testVersionTemplateCount(){
        List<Integer> list = new ArrayList<Integer>();
        list.add(VersionedObjectStatus.NORMAL.getId());
        list.add(VersionedObjectStatus.DRAFT.getId());

        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, Calendar.JANUARY, 1);
        calendar.clear();

        Assert.assertEquals(2, declarationTemplateDao.versionTemplateCount(1, list));

    }

    @Test
    public void testVersionTemplateCountByType(){
        List<Map<String, Object>> list = declarationTemplateDao.versionTemplateCountByType(Arrays.asList(1,2));
        Assert.assertEquals(new BigDecimal(1), list.get(0).get("type_id"));
        Assert.assertEquals((long) 2, list.get(0).get("version_count"));
    }

    @Test
    public void testGetLastVersionEdition(){
        Assert.assertEquals(3, declarationTemplateDao.getLastVersionEdition(2));
    }


    @Test
    public void getBadDeclarationTemplateScriptTest(){
        assertEquals("", declarationTemplateDao.getDeclarationTemplateScript(100));
    }

    @Test
    public void testFindFTVersionIntersections() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");

        Assert.assertEquals(1, declarationTemplateDao.findFTVersionIntersections(1, 0, dateFormat.parse("2015.01.01"), dateFormat.parse("2014.12.31")).size());
        Assert.assertEquals(2, declarationTemplateDao.findFTVersionIntersections(1, 0, dateFormat.parse("2013.01.01"), dateFormat.parse("2015.12.31")).size());
        Assert.assertEquals(1, declarationTemplateDao.findFTVersionIntersections(1, 1, dateFormat.parse("2013.01.01"), null).size());
    }
}
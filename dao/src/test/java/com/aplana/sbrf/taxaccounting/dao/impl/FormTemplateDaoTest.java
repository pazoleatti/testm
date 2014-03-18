package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Vitalii Samolovskikh
 * 			Damir Sultanbekov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FormTemplateDaoTest.xml"})
@Transactional
public class FormTemplateDaoTest {

	// TODO: расширить тесты
	@Autowired
	private FormTemplateDao formTemplateDao;

	@Test
	public void testGet(){
		FormTemplate ft1 = formTemplateDao.get(1);
		Assert.assertEquals(1, ft1.getId().intValue());
		Assert.assertEquals(1, ft1.getType().getId());
		Assert.assertTrue(ft1.isFixedRows());
		Assert.assertFalse(ft1.isMonthly());
		Assert.assertEquals("name_1", ft1.getName());
		Assert.assertEquals("fullname_1", ft1.getFullName());
		Assert.assertEquals("code_1", ft1.getCode());

		FormTemplate ft2 = formTemplateDao.get(2);
		Assert.assertEquals(2, ft2.getId().intValue());
		Assert.assertEquals(2, ft2.getType().getId());
		Assert.assertFalse(ft2.isFixedRows());
		Assert.assertTrue(ft2.isMonthly());
		Assert.assertEquals("name_2", ft2.getName());
		Assert.assertEquals("fullname_2", ft2.getFullName());
		Assert.assertEquals("code_2", ft2.getCode());
	}
	
	@Test(expected=DaoException.class)
	public void testNotexistingGet() {
		formTemplateDao.get(-1000);
	}

	@Test
	public void testSave() {
		FormTemplate formTemplate = formTemplateDao.get(1);		
		formTemplate.setFixedRows(false);
		formTemplate.setMonthly(true);
		formTemplate.setVersion(new Date());
		formTemplate.setName("name_3");
		formTemplate.setFullName("fullname_3");
		formTemplate.setCode("code_3");
		formTemplate.setScript("test_script");
		formTemplateDao.save(formTemplate);
		formTemplate = formTemplateDao.get(1);
		Assert.assertFalse(formTemplate.isFixedRows());
		Assert.assertTrue(formTemplate.isMonthly());
		/*Assert.assertEquals("321", formTemplate.getVersion());*/
		Assert.assertEquals("name_3", formTemplate.getName());
		Assert.assertEquals("fullname_3", formTemplate.getFullName());
		Assert.assertEquals("code_3", formTemplate.getCode());
		Assert.assertEquals(0, formTemplate.getRows().size());
		Assert.assertEquals(0, formTemplate.getHeaders().size());
	}
	
	@Test
	public void testSaveDataRows() {
		FormTemplate formTemplate = formTemplateDao.get(1);
		
		DataRow<Cell> rows = new DataRow<Cell>(FormDataUtils.createCells(formTemplate.getColumns(), formTemplate.getStyles()));
		formTemplate.getRows().add(rows);
		
		DataRow<HeaderCell> headers1 = new DataRow<HeaderCell>(FormDataUtils.createHeaderCells(formTemplate.getColumns()));
		DataRow<HeaderCell> headers2 = new DataRow<HeaderCell>(FormDataUtils.createHeaderCells(formTemplate.getColumns()));
		formTemplate.getHeaders().add(headers1);
		formTemplate.getHeaders().add(headers2);
		
		formTemplate.setFixedRows(false);
		formTemplate.setStatus(VersionedObjectStatus.NORMAL);
		formTemplate.setName("name_3");
		formTemplate.setFullName("fullname_3");
		formTemplate.setCode("code_3");
		formTemplate.setScript("test_script");
		formTemplateDao.save(formTemplate);
		formTemplate = formTemplateDao.get(1);
		Assert.assertFalse(formTemplate.isFixedRows());
		/*Assert.assertEquals("321", formTemplate.getVersion());*/
		Assert.assertEquals("name_3", formTemplate.getName());
		Assert.assertEquals("fullname_3", formTemplate.getFullName());
		Assert.assertEquals("code_3", formTemplate.getCode());
		/*Assert.assertEquals("test_script", formTemplate.getScript());*/
		/*Assert.assertEquals(1, formTemplate.getRows().size());*/
		/*Assert.assertEquals(2, formTemplate.getHeaders().size());*/
		
	}

    @Test
    public void testGetTextScript() {
        FormTemplate formTemplate = formTemplateDao.get(1);
        formTemplate.setFixedRows(false);
        formTemplate.setName("name_3");
        formTemplate.setFullName("fullname_3");
        formTemplate.setCode("code_3");
        formTemplate.setScript("test_script");
        formTemplateDao.save(formTemplate);
        String scriptText = formTemplateDao.getFormTemplateScript(1);
        Assert.assertEquals("test_script", scriptText);
    }

    @Test
    public void testGetDataCells() {
        FormTemplate formTemplate = formTemplateDao.get(1);
        formTemplate.setFixedRows(false);
        formTemplate.setStatus(VersionedObjectStatus.NORMAL);
        formTemplate.setName("name_3");
        formTemplate.setFullName("fullname_3");
        formTemplate.setCode("code_3");
        formTemplate.setScript("test_script");
        DataRow<Cell> rows = new DataRow<Cell>(FormDataUtils.createCells(formTemplate.getColumns(), formTemplate.getStyles()));
        formTemplate.getRows().add(rows);
        formTemplateDao.save(formTemplate);
        Assert.assertEquals(1, formTemplate.getRows().size());
    }

    @Test
    public void testGetHeaderCells() {
        FormTemplate formTemplate = formTemplateDao.get(1);
        formTemplate.setFixedRows(false);
        formTemplate.setStatus(VersionedObjectStatus.NORMAL);
        formTemplate.setName("name_3");
        formTemplate.setFullName("fullname_3");
        formTemplate.setCode("code_3");
        formTemplate.setScript("test_script");

        DataRow<HeaderCell> headers1 = new DataRow<HeaderCell>(FormDataUtils.createHeaderCells(formTemplate.getColumns()));
        DataRow<HeaderCell> headers2 = new DataRow<HeaderCell>(FormDataUtils.createHeaderCells(formTemplate.getColumns()));
        formTemplate.getHeaders().add(headers1);
        formTemplate.getHeaders().add(headers2);

        formTemplateDao.save(formTemplate);
        Assert.assertEquals(2, formTemplate.getHeaders().size());
    }

    @Test
    public void testGetByFilter() {
        TemplateFilter filter = new TemplateFilter();
        filter.setTaxType(TaxType.TRANSPORT);
        filter.setActive(true);
        Assert.assertEquals(1, formTemplateDao.getByFilter(filter).size());
    }

    @Test
    public void testListAll(){
        Assert.assertEquals(2, formTemplateDao.listAll().size());
    }

    @Test
    public void testGetFormTemplateVersions(){
        List<Integer> list = new ArrayList<Integer>();
        list.add(VersionedObjectStatus.NORMAL.getId());
        list.add(VersionedObjectStatus.DRAFT.getId());

        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, Calendar.JANUARY, 1);
        calendar.clear();

        Assert.assertEquals(2, formTemplateDao.getFormTemplateVersions(2, list).size());
    }

    @Test
    public void testGetFTVersionEndDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.JANUARY, 1);
        Date actualStartVersion = new Date(calendar.getTime().getTime());

        //Сверка
        calendar.set(2014, Calendar.DECEMBER, 31, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Assert.assertEquals(calendar.getTime(), formTemplateDao.getFTVersionEndDate(2, 2, actualStartVersion));
    }

    @Test
    public void testGetNearestFTVersionIdRight(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2013, Calendar.JANUARY, 1);
        Date actualStartVersion = new Date(calendar.getTime().getTime());

        Assert.assertEquals(2, formTemplateDao.getNearestFTVersionIdRight(2, actualStartVersion));
    }

    @Test
    public void testGetNearestFTVersionIdLeft(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2013, Calendar.JANUARY, 1);
        Date actualStartVersion = calendar.getTime();

        List<Integer> list = new ArrayList<Integer>();
        list.add(VersionedObjectStatus.NORMAL.getId());
        list.add(VersionedObjectStatus.DRAFT.getId());
        Assert.assertEquals(0, formTemplateDao.getNearestFTVersionIdLeft(2, list, actualStartVersion));
    }

    @Test
    public void testDelete(){
        Assert.assertEquals(1, formTemplateDao.delete(2));
    }

    @Test
    public void testSaveNew(){
        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setEdition(1);
        formTemplate.setFixedRows(false);
        formTemplate.setVersion(new Date());
        formTemplate.setStatus(VersionedObjectStatus.NORMAL);
        formTemplate.setName("name_4");
        formTemplate.setFullName("fullname_4");
        formTemplate.setCode("code_4");
        formTemplate.setScript("test_script");
        formTemplate.setStatus(VersionedObjectStatus.FAKE);
        FormType type = new FormType();
        type.setId(1);
        formTemplate.setType(type);
        formTemplate.getStyles().addAll(formTemplateDao.get(1).getStyles());
        int id = formTemplateDao.saveNew(formTemplate);
        formTemplate = formTemplateDao.get(id);
        Assert.assertEquals("name_4", formTemplate.getName());
        Assert.assertEquals("fullname_4", formTemplate.getFullName());
        Assert.assertEquals("code_4", formTemplate.getCode());
    }

    @Test
    public void testVersionTemplateCount(){
        List<Integer> list = new ArrayList<Integer>();
        list.add(VersionedObjectStatus.NORMAL.getId());
        list.add(VersionedObjectStatus.DRAFT.getId());
        Assert.assertEquals(1, formTemplateDao.versionTemplateCount(1, list));
    }

    @Test
    public void testFindFTVersionIntersections() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        Assert.assertEquals(1, formTemplateDao.findFTVersionIntersections(2, 0, dateFormat.parse("2014.01.01"), dateFormat.parse("2014.12.31")).size());
        Assert.assertEquals(2, formTemplateDao.findFTVersionIntersections(2, 0, dateFormat.parse("2014.01.01"), dateFormat.parse("2015.12.31")).size());
        Assert.assertEquals(0, formTemplateDao.findFTVersionIntersections(2, 2, dateFormat.parse("2014.01.01"), dateFormat.parse("2014.12.31")).size());
        Assert.assertEquals(1, formTemplateDao.findFTVersionIntersections(2, 2, dateFormat.parse("2014.01.01"), null).size());
        Assert.assertEquals(1, formTemplateDao.findFTVersionIntersections(2, 0, dateFormat.parse("2014.01.01"), null).size());
    }

    @Test
    public void getActiveDeclarationTemplateIdTest() {
        assertEquals(2, formTemplateDao.getActiveFormTemplateId(2, 4));
    }
}

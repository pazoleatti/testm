package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationSubreportDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataFileDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationSubreport;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.FormDataFile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DeclarationSubreportDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DeclarationSubreportDaoTest {

	@Autowired
	private DeclarationSubreportDao declarationSubreportDao;

	@Test
	public void getSubreportsTest() {
		List<DeclarationSubreport> subreports = declarationSubreportDao.getDeclarationSubreports(1);
		assertEquals(2, subreports.size());
		assertEquals(1, subreports.get(0).getOrder());
        assertEquals("alias1", subreports.get(0).getAlias());
        assertEquals("name1", subreports.get(0).getName());
        assertNull(subreports.get(0).getBlobDataId());
        assertEquals(2, subreports.get(1).getOrder());
        assertEquals("alias2", subreports.get(1).getAlias());
        assertEquals("name2", subreports.get(1).getName());
        assertEquals("uuid_2", subreports.get(1).getBlobDataId());

        DeclarationSubreport subreport = declarationSubreportDao.getSubreportByAlias(1, "alias2");
        assertEquals(2, subreport.getOrder());
        assertEquals("alias2", subreport.getAlias());
        assertEquals("name2", subreport.getName());
        assertEquals("uuid_2", subreport.getBlobDataId());
    }

	@Test
	public void saveFilesTest() {
        //проверка исходных данных
        List<DeclarationSubreport> subreports = declarationSubreportDao.getDeclarationSubreports(1);
		assertEquals(2, subreports.size());
        Iterator<DeclarationSubreport> iterator = subreports.iterator();
        while (iterator.hasNext()) {
            DeclarationSubreport subreport = iterator.next();
            if (subreport.getOrder() == 1) {
                assertEquals("name1", subreport.getName());
                assertEquals("alias1", subreport.getAlias());
                assertNull(subreport.getBlobDataId());
                subreport.setName("newName1");
            } else if (subreport.getOrder() == 2) {
                assertEquals("name2", subreport.getName());
                assertEquals("alias2", subreport.getAlias());
                assertEquals("uuid_2", subreport.getBlobDataId());
            } else {
                assert false;
            }
        }
        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setId(1);
        declarationTemplate.setSubreports(subreports);

        //проверка изменения комментария к файлу
        declarationSubreportDao.updateDeclarationSubreports(declarationTemplate);
        subreports = declarationSubreportDao.getDeclarationSubreports(1);
        assertEquals(2, subreports.size());
        iterator = subreports.iterator();
        while (iterator.hasNext()) {
            DeclarationSubreport subreport = iterator.next();
            if (subreport.getOrder() == 1) {
                assertEquals("newName1", subreport.getName());
                assertEquals("alias1", subreport.getAlias());
                assertNull(subreport.getBlobDataId());
                subreport.setOrder(6);
            } else if (subreport.getOrder() == 2) {
                assertEquals("name2", subreport.getName());
                assertEquals("alias2", subreport.getAlias());
                assertEquals("uuid_2", subreport.getBlobDataId());
            } else {
                assert false;
            }
        }

        DeclarationSubreport newSubreport = new DeclarationSubreport();
        newSubreport.setName("newName");
        newSubreport.setAlias("newAlias");
        newSubreport.setOrder(5);
        newSubreport.setBlobDataId(null);
        subreports.add(newSubreport);
        declarationTemplate.setSubreports(subreports);

        //проверка добавления и удаления файла, изменения комментария к файлу
        declarationSubreportDao.updateDeclarationSubreports(declarationTemplate);
        subreports = declarationSubreportDao.getDeclarationSubreports(1);
        assertEquals(3, subreports.size());
        iterator = subreports.iterator();
        while (iterator.hasNext()) {
            DeclarationSubreport subreport = iterator.next();
            if (subreport.getOrder() == 1) {
                assertEquals("name2", subreport.getName());
                assertEquals("alias2", subreport.getAlias());
                assertEquals("uuid_2", subreport.getBlobDataId());
            } else if (subreport.getOrder() == 2) {
                assertEquals("newName", subreport.getName());
                assertEquals("newAlias", subreport.getAlias());
                assertNull(subreport.getBlobDataId());
            } else if (subreport.getOrder() == 3) {
                assertEquals("newName1", subreport.getName());
                assertEquals("alias1", subreport.getAlias());
                assertNull(subreport.getBlobDataId());
            } else {
                assert false;
            }
        }
	}

    @Test
    public void saveFilesTest2() {
        List<DeclarationSubreport> subreports = new ArrayList<DeclarationSubreport>();
        DeclarationSubreport newSubreport = new DeclarationSubreport();
        newSubreport.setName("newName");
        newSubreport.setAlias("newAlias");
        newSubreport.setOrder(5);
        newSubreport.setBlobDataId(null);
        subreports.add(newSubreport);
        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setId(1);
        declarationTemplate.setSubreports(subreports);
        declarationSubreportDao.updateDeclarationSubreports(declarationTemplate);

        subreports = declarationSubreportDao.getDeclarationSubreports(1);
        assertEquals(1, subreports.size());
        assertEquals(1, subreports.get(0).getOrder());
        assertEquals("newAlias", subreports.get(0).getAlias());
        assertEquals("newName", subreports.get(0).getName());
        assertNull(subreports.get(0).getBlobDataId());
    }
}

package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationSubreportParamDao;
import com.aplana.sbrf.taxaccounting.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DeclarationSubreportParamDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DeclarationSubreportParamDaoTest {

	@Autowired
	private DeclarationSubreportParamDao declarationSubreportParamDao;

	@Test
	public void getSubreportsTest() {
		List<DeclarationSubreportParam> subreportParams = declarationSubreportParamDao.getDeclarationSubreportParams(1);
		assertEquals(2, subreportParams.size());
		assertEquals(1, subreportParams.get(0).getOrder());
        assertEquals("alias1", subreportParams.get(0).getAlias());
        assertEquals("name1", subreportParams.get(0).getName());
        assertNull(subreportParams.get(0).getFilter());
        assertEquals(2, subreportParams.get(1).getOrder());
        assertEquals("alias2", subreportParams.get(1).getAlias());
        assertEquals("name2", subreportParams.get(1).getName());
        assertEquals("filter_2", subreportParams.get(1).getFilter());

        DeclarationSubreportParam subreportParam = declarationSubreportParamDao.getSubreportParamByAlias(1, "alias2");
        assertEquals(2, subreportParam.getOrder());
        assertEquals("alias2", subreportParam.getAlias());
        assertEquals("name2", subreportParam.getName());
        assertEquals("filter_2", subreportParam.getFilter());
    }

	@Test
	public void saveParamsTest() {
        //проверка исходных данных
        List<DeclarationSubreportParam> subreportParams = declarationSubreportParamDao.getDeclarationSubreportParams(1);
		assertEquals(2, subreportParams.size());
        Iterator<DeclarationSubreportParam> iterator = subreportParams.iterator();
        while (iterator.hasNext()) {
            DeclarationSubreportParam subreportParam = iterator.next();
            if (subreportParam.getOrder() == 1) {
                assertEquals("name1", subreportParam.getName());
                assertEquals("alias1", subreportParam.getAlias());
                assertNull(subreportParam.getFilter());
                subreportParam.setName("newName1");
            } else if (subreportParam.getOrder() == 2) {
                assertEquals("name2", subreportParam.getName());
                assertEquals("alias2", subreportParam.getAlias());
                assertEquals("filter_2", subreportParam.getFilter());
            } else {
                assert false;
            }
        }
        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setId(1);
        DeclarationSubreport subreport = new DeclarationSubreport();
        subreport.setId(1L);
        subreport.setName("name");
        subreport.setName("alias1");
        subreport.setOrder(1);
        subreport.setDeclarationSubreportParams(subreportParams);
        declarationTemplate.setSubreports(Arrays.asList(subreport));

        //проверка изменения комментария к файлу
        declarationSubreportParamDao.updateDeclarationSubreports(declarationTemplate);
        subreportParams = declarationSubreportParamDao.getDeclarationSubreportParams(1);
        assertEquals(2, subreportParams.size());
        iterator = subreportParams.iterator();
        while (iterator.hasNext()) {
            DeclarationSubreportParam subreportParam = iterator.next();
            if (subreportParam.getOrder() == 1) {
                assertEquals("newName1", subreportParam.getName());
                assertEquals("alias1", subreportParam.getAlias());
                assertNull(subreportParam.getFilter());
                subreportParam.setOrder(6);
            } else if (subreportParam.getOrder() == 2) {
                assertEquals("name2", subreportParam.getName());
                assertEquals("alias2", subreportParam.getAlias());
                assertEquals("filter_2", subreportParam.getFilter());
            } else {
                assert false;
            }
        }

        DeclarationSubreportParam newSubreportParam = new DeclarationSubreportParam();
        newSubreportParam.setName("newName");
        newSubreportParam.setAlias("newAlias");
        newSubreportParam.setType(DeclarationSubreportParamType.REFBOOK);
        newSubreportParam.setRefBookAttributeId(2051L);
        newSubreportParam.setOrder(5);
        newSubreportParam.setFilter(null);
        subreportParams.add(newSubreportParam);
        declarationTemplate.getSubreports().get(0).setDeclarationSubreportParams(subreportParams);

        //проверка добавления и удаления параметров, изменения параметров
        declarationSubreportParamDao.updateDeclarationSubreports(declarationTemplate);
        subreportParams = declarationSubreportParamDao.getDeclarationSubreportParams(1);
        System.out.println(subreportParams.toString());
        assertEquals(3, subreportParams.size());
        iterator = subreportParams.iterator();
        while (iterator.hasNext()) {
            DeclarationSubreportParam subreportParam = iterator.next();
            if (subreportParam.getOrder() == 1) {
                assertEquals("name2", subreportParam.getName());
                assertEquals("alias2", subreportParam.getAlias());
                assertEquals("filter_2", subreportParam.getFilter());
            } else if (subreportParam.getOrder() == 2) {
                assertEquals("newName", subreportParam.getName());
                assertEquals("newAlias", subreportParam.getAlias());
                assertEquals(new Long(2051L), subreportParam.getRefBookAttributeId());
                assertNull(subreportParam.getFilter());
            } else if (subreportParam.getOrder() == 3) {
                assertEquals("newName1", subreportParam.getName());
                assertEquals("alias1", subreportParam.getAlias());
                assertNull(subreportParam.getFilter());
            } else {
                assert false;
            }
        }
	}

    @Test
    public void saveParamsTest2() {
        DeclarationSubreportParam newSubreportParam = new DeclarationSubreportParam();
        newSubreportParam.setName("newName");
        newSubreportParam.setAlias("newAlias");
        newSubreportParam.setType(DeclarationSubreportParamType.NUMBER);
        newSubreportParam.setOrder(5);
        newSubreportParam.setFilter(null);

        DeclarationSubreport newSubreport = new DeclarationSubreport();
        newSubreport.setId(1);
        newSubreport.setName("newSubreportName");
        newSubreport.setAlias("newSubreportAlias");
        newSubreport.setOrder(5);
        newSubreport.setBlobDataId(null);
        newSubreport.setDeclarationSubreportParams(Arrays.asList(newSubreportParam));

        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setId(1);
        declarationTemplate.setSubreports(Arrays.asList(newSubreport));
        declarationSubreportParamDao.updateDeclarationSubreports(declarationTemplate);

        List<DeclarationSubreportParam> subreportParams = declarationSubreportParamDao.getDeclarationSubreportParams(1);
        assertEquals(1, subreportParams.size());
        assertEquals(1, subreportParams.get(0).getOrder());
        assertEquals("newAlias", subreportParams.get(0).getAlias());
        assertEquals("newName", subreportParams.get(0).getName());
        assertEquals(DeclarationSubreportParamType.NUMBER, subreportParams.get(0).getType());
        assertNull(subreportParams.get(0).getFilter());
    }
}

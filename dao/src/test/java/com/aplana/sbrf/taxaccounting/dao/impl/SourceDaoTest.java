package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.SourceDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Ignore("Работа с источниками приемниками будет переделана!")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "SourceDaoTest.xml" })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class SourceDaoTest {

    @Autowired
    SourceDao sourceDao;
    @Autowired
    DepartmentFormTypeDao departmentFormTypeDao;

    @Test
    public void deleteConsolidateInfo() {
        assertTrue(sourceDao.isDeclarationSourceConsolidated(2, 11));
        assertTrue(sourceDao.isDeclarationSourceConsolidated(2, 12));
        sourceDao.deleteDeclarationConsolidateInfo(2);
        assertFalse(sourceDao.isDeclarationSourceConsolidated(2, 11));
        assertFalse(sourceDao.isDeclarationSourceConsolidated(2, 12));
    }

    @Test
    public void addConsolidateInfo() {
        sourceDao.addFormDataConsolidationInfo(1l, Arrays.asList(1l));
        assertTrue(sourceDao.isFDSourceConsolidated(1, 1));
    }

    @Test
    public void deleteFDConsolidateInfo() {
        sourceDao.addFormDataConsolidationInfo(1l, Arrays.asList(1l));
        assertTrue(sourceDao.isDeclarationSourceConsolidated(1, 1));
        sourceDao.deleteFormDataConsolidationInfo(Arrays.asList(1l));
        assertFalse(sourceDao.isFDSourceConsolidated(1, 1));
    }

    @Test
    public void testUpdateFD(){
        sourceDao.addFormDataConsolidationInfo(1l, Arrays.asList(1l));
        sourceDao.updateFDConsolidationInfo(1);
        assertFalse(sourceDao.isFDConsolidationTopical(1));
    }

    @Test
    public void testUpdateDD(){
        sourceDao.addDeclarationConsolidationInfo(1l, Arrays.asList(1l));
        sourceDao.updateDDConsolidationInfo(1);
        assertFalse(sourceDao.isDDConsolidationTopical(1));
    }


    @Test
    public void testfindConsolidatedInstances() {
        assertEquals(0, sourceDao.findConsolidatedInstances(1l, 1l, new Date(), new Date(), true).size());
        assertEquals(0, sourceDao.findConsolidatedInstances(1l, 1l, new Date(), new Date(), false).size());
    }

    /*@Test
    public void testSaveDeclarationSources() {
        List<DepartmentFormType> sources = departmentFormTypeDao.getDeclarationSources(2, 1);
        List<Long> sourceIds = new ArrayList<Long>();

        for (DepartmentFormType source : sources) {
            sourceIds.add(source.getId());
        }

        sourceIds.add(3l);

        assertEquals(5, sourceIds.size());
        assertTrue(sourceIds.contains(3l));
        assertTrue(sourceIds.contains(5l));
        assertTrue(sourceIds.contains(6l));
        assertTrue(sourceIds.contains(21l));
        assertTrue(sourceIds.contains(22l));

        sourceIds.remove(21l);

        departmentFormTypeDao.saveDeclarationSources(1l, sourceIds);
        sources = departmentFormTypeDao.getDeclarationSources(2, 1);
        sourceIds.clear();

        for (DepartmentFormType source : sources) {
            sourceIds.add(source.getId());
        }

        assertEquals(4, sourceIds.size());
        assertTrue(sourceIds.contains(3l));
        assertTrue(sourceIds.contains(5l));
        assertTrue(sourceIds.contains(6l));
        assertTrue(sourceIds.contains(22l));
    }*/
}

package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.SourceDao;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"SourceDaoTest.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
//TODO включить в версии 3.10.1
@Ignore
public class SourceDaoTest {

    @Autowired
    SourceDao sourceDao;


    @Test
    public void testAddDeclarationConsolidationInfo() {
        sourceDao.addDeclarationConsolidationInfo(4L, Collections.singleton(5L));
        assertTrue(sourceDao.isDeclarationSourceConsolidated(4L, 5L));
    }

    @Test
    public void testIsDeclarationSourceConsolidated() {
        assertTrue(sourceDao.isDeclarationSourceConsolidated(1, 2));
        assertTrue(sourceDao.isDeclarationSourceConsolidated(1, 3));
        assertTrue(sourceDao.isDeclarationSourceConsolidated(1, 4));
    }

    @Test
    public void testUpdateDDConsolidationInfo() {
        assertTrue(sourceDao.isDeclarationSourceConsolidated(5, 5));
        sourceDao.updateDDConsolidationInfo(5);
        assertFalse(sourceDao.isDeclarationSourceConsolidated(5, 5));
    }

    @Test
    public void testIsDDConsolidationTopical() {
        assertTrue(sourceDao.isDDConsolidationTopical(1));
        assertFalse(sourceDao.isDDConsolidationTopical(123));
    }

    @Test
    public void testSourcesInfo() {
        assertEquals(3, sourceDao.getSourcesInfo(1L).size());
    }

    @Test
    public void testDestinationsInfo() {
        assertEquals(3, sourceDao.getDestinationsInfo(1L).size());
    }

    @Test
    public void testDeleteDeclarationConsolidateInfo() {
        assertTrue(sourceDao.isDeclarationSourceConsolidated(2, 1));
        assertTrue(sourceDao.isDeclarationSourceConsolidated(2, 2));
        sourceDao.deleteDeclarationConsolidateInfo(2);
        assertFalse(sourceDao.isDeclarationSourceConsolidated(2, 1));
        assertFalse(sourceDao.isDeclarationSourceConsolidated(2, 2));
    }
}

package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "SourceDaoTest.xml" })
@Transactional
public class SourceDaoTest {
    /*@Test
    public void testSaveFormSources() {
        List<DepartmentFormType> sources = departmentFormTypeDao.getFormSources(2, 2, FormDataKind.fromId(3));
        List<Long> sourceIds = new ArrayList<Long>();

        for (DepartmentFormType source : sources) {
            sourceIds.add(source.getId());
        }

        sourceIds.add(6l);

        assertEquals(3, sourceIds.size());
        assertTrue(sourceIds.contains(1l));
        assertTrue(sourceIds.contains(6l));
        assertTrue(sourceIds.contains(11l));

        sourceIds.remove(1l);

        departmentFormTypeDao.saveFormSources(12l, sourceIds);
        sources = departmentFormTypeDao.getFormSources(2, 2, FormDataKind.fromId(3));
        sourceIds.clear();

        for (DepartmentFormType source : sources) {
            sourceIds.add(source.getId());
        }

        assertEquals(2, sourceIds.size());
        assertTrue(sourceIds.contains(6l));
        assertTrue(sourceIds.contains(11l));

    }

    @Test
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

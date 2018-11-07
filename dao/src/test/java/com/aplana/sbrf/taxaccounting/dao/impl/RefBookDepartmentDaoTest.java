package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DepartmentDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookDepartmentDaoTest {

    @Autowired
    private RefBookDepartmentDao refBookDepartmentDao;

    @Test
    public void testFetchDepartments() {
        List<RefBookDepartment> result = refBookDepartmentDao.findAllActiveByIds(Lists.newArrayList(1));
        assertEquals(1, result.size());
        assertEquals(DepartmentType.ROOT_BANK, result.get(0).getType());
    }

    @Test
    public void testFetchDepartmentByIdSuccessful() {
        RefBookDepartment result = refBookDepartmentDao.fetchDepartmentById(1);

        assertNotNull(result);
        assertEquals(DepartmentType.ROOT_BANK, result.getType());
    }

    @Test(expected = DaoException.class)
    public void testFetchDepartmentByIdFailed() {
        RefBookDepartment result = refBookDepartmentDao.fetchDepartmentById(Integer.MAX_VALUE);

        assertNotNull(result);
        assertEquals(DepartmentType.ROOT_BANK, result.getType());
    }

    @Test
    public void findAllByNameWithParentsTest() {
        List<RefBookDepartment> departments = refBookDepartmentDao.findAllByNameAsTree("поДр1 от тБ2", false);
        assertThat(idsOf(departments), equalTo(singletonList(3)));
        assertThat(idsOf(childrenOf(departments)), equalTo(singletonList(4)));

        departments = refBookDepartmentDao.findAllByNameAsTree("от тБ2", false);
        assertThat(idsOf(departments), equalTo(singletonList(3)));
        assertThat(idsOf(childrenOf(departments)), equalTo(asList(4, 5)));

        departments = refBookDepartmentDao.findAllByNameAsTree("тБ2", true);
        assertThat(idsOf(departments), equalTo(Collections.EMPTY_LIST));

        departments = refBookDepartmentDao.findAllByNameAsTree("ТБ1", true);
        assertThat(idsOf(departments), equalTo(singletonList(2)));

        departments = refBookDepartmentDao.findAllByNameAsTree("", true);
        assertThat(idsOf(departments), equalTo(asList(2, 3, 7)));
        assertThat(idsOf(childrenOf(departments)), equalTo(asList(6, 4, 5)));
    }

    private List<RefBookDepartment> childrenOf(List<RefBookDepartment> departments) {
        Set<RefBookDepartment> children = new LinkedHashSet<>();
        for (RefBookDepartment department : departments) {
            if (department.getChildren() != null) {
                children.addAll(department.getChildren());
            }
        }
        return new ArrayList<>(children);
    }

    private List<Integer> idsOf(List<RefBookDepartment> departments) {
        return Lists.transform(departments, new Function<RefBookDepartment, Integer>() {
            @Override
            public Integer apply(RefBookDepartment department) {
                return department.getId();
            }
        });
    }

    @Test
    public void fetchAllActiveByType() {
        List<RefBookDepartment> departments = refBookDepartmentDao.fetchAllActiveByType(DepartmentType.TERR_BANK);
        assertEquals(3, departments.size());
        for (RefBookDepartment department : departments) {
            assertEquals(department.getType(), DepartmentType.TERR_BANK);
        }
    }
}
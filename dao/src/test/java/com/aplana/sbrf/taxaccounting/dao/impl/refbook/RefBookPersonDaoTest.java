package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookPersonDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RefBookPersonDaoTest {

    @Autowired
    private RefBookPersonDao personDao;

    @Test
    public void test_fetchOriginal() {
        List<RegistryPerson> personVersions = personDao.fetchOriginal(3L);
        assertThat(personVersions).hasSize(1);
        assertThat(personVersions.get(0).getId()).isEqualTo(1L);
    }

    @Test
    public void test_getPersonTbIds() {
        List<Integer> tbIds = personDao.getPersonTbIds(1);
        assertThat(tbIds).hasSize(2);
        assertThat(tbIds).containsExactly(1, 2);
    }

    @Test
    public void test_getPersons() {
        PagingResult<RefBookPerson> persons = personDao.getPersons(null, null);

        assertThat(persons).hasSize(4);
        assertThat(persons.getTotalCount()).isEqualTo(4);
    }

    @Test
    public void test_getPersons_filterByMiddleName() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setMiddleName("СЕРГЕЕВИЧ");

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons)
                .hasSize(2)
                .extracting("middleName")
                .containsOnly("Сергеевич");
    }

    @Test
    public void test_getPersons_filterByBirthDateFrom() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        Calendar calendar = Calendar.getInstance();
        calendar.set(1981, Calendar.OCTOBER, 16);
        filter.setBirthDateFrom(calendar.getTime());

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(3);
    }

    @Test
    public void test_getPersons_filterByBirthDateTo() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        Calendar calendar = Calendar.getInstance();
        calendar.set(1981, Calendar.OCTOBER, 16);
        filter.setBirthDateTo(calendar.getTime());

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2);
    }

    @Test
    public void test_getPersons_filterByOldId() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setId("1");

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons)
                .hasSize(2)
                .extracting("oldId")
                .containsExactlyInAnyOrder(1L, 10L);
    }

    @Ignore("На HSQL <2.3.4 не работает regexp_replace(), включить, когда разберёмся")
    @Test
    public void test_getPersons_filterByDocNumber() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setDocumentNumber("0101123456");

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(1);
        assertThat(persons.get(0).getDocNumber()).isEqualTo("01 01 123456");
    }
}

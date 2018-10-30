package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.main.RefBookDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.Address;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookPersonDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RefBookPersonDaoTest {

    @Autowired
    private RefBookPersonDao personDao;
    @Autowired
    private RefBookSimpleDaoImpl refBookSimpleDao;
    @Autowired
    private RefBookDaoImpl refBookDao;

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


    // --- Метод getPersons ---

    @Test
    public void test_getPersons() {
        PagingResult<RegistryPerson> persons = personDao.getPersons(null, null);

        assertThat(persons).hasSize(6);
        assertThat(persons.getTotalCount()).isEqualTo(6);
    }

    @Test
    public void test_getPersons_filterByNonVip() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setVip(false);

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(3)
                .extracting("vip")
                .containsOnly(false);
    }

    @Test
    public void test_getPersons_filterByMiddleName() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setMiddleName("СЕРГЕЕВИЧ");

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2)
                .extracting("middleName")
                .containsOnly("Сергеевич");
    }

    @Test
    public void test_getPersons_filterByBirthDateFrom() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        Calendar calendar = Calendar.getInstance();
        calendar.set(1981, Calendar.OCTOBER, 16);
        filter.setBirthDateFrom(calendar.getTime());

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(5);
    }

    @Test
    public void test_getPersons_filterByBirthDateTo() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        Calendar calendar = Calendar.getInstance();
        calendar.set(1981, Calendar.OCTOBER, 16);
        filter.setBirthDateTo(calendar.getTime());

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2);
    }

    @Test
    public void test_getPersons_filterByVip() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setVip(true);

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(3)
                .extracting("vip")
                .containsOnly(true);
    }

    @Test
    public void test_getPersons_filterByCitizenshipCountries() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setCitizenshipCountries(Arrays.asList(1L, 2L));

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(5)
                .extracting("citizenship.code")
                .containsOnly("643", "678")
                .containsOnlyOnce("678");
    }

    @Test
    public void test_getPersons_filterByTaxpayerStates() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setTaxpayerStates(Arrays.asList(2L, 3L));

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2)
                .extracting("taxPayerState.id")
                .containsOnly(2L, 3L);
    }

    @Test
    public void test_getPersons_filterBySourceSystems() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setSourceSystems(Arrays.asList(1L, 2L));

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2)
                .extracting("source.id")
                .containsOnly(1L, 2L);
    }

    @Test
    public void test_getPersons_filterByOldId() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setId("1");

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2)
                .extracting("oldId")
                .containsExactlyInAnyOrder(1L, 10L);
    }

    @Test
    public void test_getPersons_filterByInn() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setInn("234");

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2)
                .extracting("inn")
                .containsOnly("1234", "2345");
    }

    @Test
    public void test_getPersons_filterByInnForeign() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setInnForeign("3-D1");

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(1)
                .extracting("innForeign")
                .containsOnly("123-d123");
    }

    @Test
    public void test_getPersons_filterByRegion() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setRegion("7");

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2)
                .extracting("address.regionCode")
                .containsOnly("77");
    }

    @Test
    public void test_getPersons_filterByPostCode() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setPostalCode("43");

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons)
                .hasSize(3)
                .extracting("address.postalCode")
                .containsOnly("143080", "109431");
    }

    @Test
    public void test_getPersons_filterByDistrict() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setDistrict("Одинцовский");

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2)
                .extracting("address.district")
                .containsOnly("ОДИНЦОВСКИЙ р-н");
    }

    @Test
    public void test_getPersons_filterByLocality() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setLocality("внииссок");

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2)
                .extracting("address.locality")
                .containsOnly("ВНИИССОК п");
    }

    @Test
    public void test_getPersons_filterByStreet() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setStreet("Ленин");

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(1)
                .extracting("address.street")
                .containsOnly("Ленинские Горы ул");
    }

    @Test
    public void test_getPersons_filterByForeignAddressCountry() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setCountries(Arrays.asList(2L, 3L));

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2)
                .extracting("address.country.code")
                .containsOnly("678", "630");
    }

    @Test
    public void test_getPersons_filterByForeignAddress() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setForeignAddress("washington");

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(1);

        Address personAddress = persons.get(0).getAddress();
        assertThat(personAddress.getAddressIno()).isEqualTo("COL, Washington DC, Kennedy street, 20");
    }

    @Test
    public void test_getPersons_filterByDuplicatesOnly() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setDuplicates(true);

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2);

        RegistryPerson person = persons.get(0);
        assertThat(person.getRecordId()).isNotEqualTo(person.getOldId());
    }

    @Test
    public void test_getPersons_filterByNotDuplicates() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setDuplicates(false);

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(4);

        RegistryPerson person = persons.get(0);
        assertThat(person.getRecordId()).isEqualTo(person.getOldId());
    }


    // --- Методы работы с  RegistryPerson ---

    @Test
    public void test_setOriginal() {
        //setup
        //execution
        personDao.setOriginal(5L, 5L, 2L);
        //verification
        RegistryPerson changedPerson = personDao.fetchPersonVersion(5L);
        assertThat(changedPerson.getRecordId()).isEqualTo(2L);
    }

    @Test
    public void test_deleteOriginal() {
        //setup
        //execution
        personDao.deleteOriginal(1L, 10L);
        //verification
        RegistryPerson changedPerson = personDao.fetchPersonVersion(4L);
        assertThat(changedPerson.getRecordId()).isEqualTo(10L);
    }

    @Test
    public void setDuplicates() {
        //setup
        //execution
        personDao.setDuplicates(Collections.singletonList(5L), 2L);
        //verification
        RegistryPerson changedPerson = personDao.fetchPersonVersion(5L);
        assertThat(changedPerson.getRecordId()).isEqualTo(2L);
    }

    @Test
    public void test_deleteDuplicates() {
        //setup
        //execution
        personDao.deleteDuplicates(Collections.singletonList(10L));
        //verification
        RegistryPerson changedPerson = personDao.fetchPersonVersion(4L);
        assertThat(changedPerson.getRecordId()).isEqualTo(10L);
    }

}

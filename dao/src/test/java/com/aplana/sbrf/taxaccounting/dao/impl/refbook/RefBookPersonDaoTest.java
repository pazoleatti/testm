package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.identification.RefDataHolder;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.main.RefBookDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.person.NaturalPersonMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
        List<RegistryPerson> personVersions = personDao.findAllOriginalVersions(3L);
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

        assertThat(persons).hasSize(7);
        assertThat(persons.getTotalCount()).isEqualTo(7);
    }

    @Test
    public void test_getPersons_filterByNonVip() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setVip(false);

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(4)
                .extracting("vip")
                .containsOnly(false);
    }

    @Test
    public void test_getPersons_filterByMiddleName() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setMiddleName("СЕРГЕЕВИЧ");

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(3)
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

        assertThat(persons).hasSize(6);
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
        filter.setCitizenshipCountryIds(Arrays.asList(1L, 2L));

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(6)
                .extracting("citizenship.code")
                .containsOnly("643", "678")
                .containsOnlyOnce("678");
    }

    @Test
    public void test_getPersons_filterByTaxpayerStates() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setTaxpayerStateIds(Arrays.asList(2L, 3L));

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2)
                .extracting("taxPayerState.id")
                .containsOnly(2L, 3L);
    }

    @Test
    public void test_getPersons_filterBySourceSystems() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setSourceSystemIds(Arrays.asList(1L, 2L));

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

        assertThat(persons).hasSize(3)
                .extracting("oldId")
                .containsExactlyInAnyOrder(1L, 10L, 10L);
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

        assertThat(persons).hasSize(3)
                .extracting("address.regionCode")
                .containsOnly("77");
    }

    @Test
    public void test_getPersons_filterByPostCode() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setPostalCode("43");

        PagingResult<RegistryPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons)
                .hasSize(4)
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
        filter.setCountryIds(Arrays.asList(2L, 3L));

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

        assertThat(persons).hasSize(3);

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

    @Test
    public void test_getPersonsCount() {

        int result = personDao.getPersonsCount(null);

        assertThat(result).isEqualTo(7);
    }

    @Test
    public void test_setOriginal() {
        //setup
        //execution
        personDao.setOriginal(2L, 5L);
        //verification
        RegistryPerson changedPerson = personDao.fetchPersonVersion(5L);
        assertThat(changedPerson.getRecordId()).isEqualTo(2L);
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

    @Test
    public void test_findActualRefPersonsByDeclarationDataId() {
        //setup
        Calendar calendar = new GregorianCalendar();
        calendar.set(2017, Calendar.JUNE, 1);
        //execution
        List<RegistryPerson> persons = personDao.findActualRefPersonsByDeclarationDataId(100L, calendar.getTime());
        Collections.sort(persons, new Comparator<RegistryPerson>() {
            @Override
            public int compare(RegistryPerson o1, RegistryPerson o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        //verification
        assertThat(persons)
                .hasSize(3)
                .extracting("id")
                .containsExactlyInAnyOrder(3L, 4L, 12122L);
    }

    @Test
    public void test_fetchPersonVersion() {
        RegistryPerson result = personDao.fetchPersonVersion(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLastName()).isEqualTo("Иванов");
    }

    @Test
    public void test_fetchDuplicates() {
        List<RegistryPerson> result = personDao.findAllDuplicatesVersions(1L);

        assertThat(result)
                .hasSize(3)
                .extracting("id")
                .containsExactlyInAnyOrder(3L, 4L, 12122L);
    }

    @Test
    public void test_updateBatch() {
        RegistryPerson registryPerson1 = new RegistryPerson();
        RegistryPerson registryPerson2 = new RegistryPerson();
        String lastName1 = "ОбновляторОдин";
        String lastName2 = "ОбновляторДва";

        registryPerson1.setId(1L);
        registryPerson1.setLastName(lastName1);
        registryPerson1.setTaxPayerState(new RefBookTaxpayerState());
        registryPerson1.setCitizenship(new RefBookCountry());
        registryPerson1.setReportDoc(new IdDoc());
        registryPerson1.setSource(new RefBookAsnu());
        registryPerson1.getAddress().setCountry(new RefBookCountry());
        registryPerson2.setId(2L);
        registryPerson2.setLastName(lastName2);
        registryPerson2.setTaxPayerState(new RefBookTaxpayerState());
        registryPerson2.setCitizenship(new RefBookCountry());
        registryPerson2.setReportDoc(new IdDoc());
        registryPerson2.setSource(new RefBookAsnu());
        registryPerson2.getAddress().setCountry(new RefBookCountry());

        personDao.updateBatch(new ArrayList<>(Arrays.asList(registryPerson1, registryPerson2)));

        RegistryPerson result1 = personDao.fetchPersonVersion(1L);
        RegistryPerson result2 = personDao.fetchPersonVersion(2L);

        Calendar startDateCalendar = new GregorianCalendar();
        startDateCalendar.set(2016, Calendar.JANUARY, 1, 0, 0, 0);
        startDateCalendar.set(Calendar.MILLISECOND, 0);
        Calendar endDateCalendar = new GregorianCalendar();
        endDateCalendar.set(2016, Calendar.DECEMBER, 31, 0, 0, 0);
        endDateCalendar.set(Calendar.MILLISECOND, 0);

        assertThat(result1.getRecordId()).isEqualTo(1L);
        assertThat(result1.getOldId()).isEqualTo(1L);
        assertThat(result1.getStartDate()).isEqualTo(startDateCalendar.getTime());
        assertThat(result1.getEndDate()).isEqualTo(endDateCalendar.getTime());
        assertThat(result1.getLastName()).isEqualTo(lastName1);

        assertThat(result2.getRecordId()).isEqualTo(2L);
        assertThat(result2.getOldId()).isEqualTo(2L);
        assertThat(result2.getStartDate()).isEqualTo(startDateCalendar.getTime());
        assertThat(result2.getLastName()).isEqualTo(lastName2);
    }

    @Test
    public void test_fetchNonDuplicatesVersions() {
        List<RegistryPerson> result = personDao.fetchNonDuplicatesVersions(1L);

        assertThat(result).hasSize(1).extracting("id").contains(1L);
    }

    @Test
    public void test_fetchOriginalDuplicatesCandidates() {
        PagingResult<RegistryPerson> result = personDao.fetchOriginalDuplicatesCandidates(null, null);

        assertThat(result).hasSize(7);
    }

    @Test
    public void test_findMaxRegistryPersonId() {
        Date currentDate = new Date(2000000000000L);
        Long result = personDao.findMaxRegistryPersonId(currentDate);

        assertThat(Long.valueOf(6).equals(result));
    }

    @Test
    public void test_findNewRegistryPersons() {
        Date currentDate = new Date(2000000000000L);

        Address address = new Address();
        address.setCountry(new RefBookCountry());

        RegistryPerson registryPerson1 = new RegistryPerson();
        registryPerson1.setTaxPayerState(new RefBookTaxpayerState());
        registryPerson1.setCitizenship(new RefBookCountry());
        registryPerson1.setReportDoc(new IdDoc());
        registryPerson1.setSource(new RefBookAsnu());
        registryPerson1.setAddress(address);
        registryPerson1.setRecordId(7L);
        registryPerson1.setOldId(7L);
        registryPerson1.setStartDate(new Date(0L));

        RegistryPerson registryPerson2 = new RegistryPerson();
        registryPerson2.setTaxPayerState(new RefBookTaxpayerState());
        registryPerson2.setCitizenship(new RefBookCountry());
        registryPerson2.setReportDoc(new IdDoc());
        registryPerson2.setSource(new RefBookAsnu());
        registryPerson2.setAddress(address);
        registryPerson2.setRecordId(8L);
        registryPerson2.setOldId(8L);
        registryPerson2.setStartDate(new Date(0L));

        personDao.saveBatch(Arrays.asList(registryPerson1, registryPerson2));

        RefDataHolder refDataHolder = new RefDataHolder();
        refDataHolder.setAsnuMap(new HashMap<Long, RefBookAsnu>());
        refDataHolder.setCountryMap(new HashMap<Long, RefBookCountry>());
        refDataHolder.setDocTypeMap(new HashMap<Long, RefBookDocType>());
        refDataHolder.setTaxpayerStatusMap(new HashMap<Long, RefBookTaxpayerState>());

        List<NaturalPerson> result = personDao.findNewRegistryPersons(6L, currentDate, new NaturalPersonMapper(refDataHolder));

        assertThat(result).hasSize(2);
    }
}

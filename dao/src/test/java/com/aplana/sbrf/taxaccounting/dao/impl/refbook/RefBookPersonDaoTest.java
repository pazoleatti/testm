package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.main.RefBookDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.Permissive;
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
        List<RegistryPersonDTO> personVersions = personDao.fetchOriginal(3L);
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
        PagingResult<RefBookPerson> persons = personDao.getPersons(null, null);

        assertThat(persons).hasSize(6);
        assertThat(persons.getTotalCount()).isEqualTo(6);
    }

    @Test
    public void test_getPersons_filterByNonVip() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setVip(false);

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(3)
                .extracting("vip")
                .containsOnly(false);
    }

    @Test
    public void test_getPersons_filterByMiddleName() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setMiddleName("СЕРГЕЕВИЧ");

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

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

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(5);
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
    public void test_getPersons_filterByVip() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setVip(true);

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(3)
                .extracting("vip")
                .containsOnly(true);
    }

    @Test
    public void test_getPersons_filterByCitizenshipCountries() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setCitizenshipCountries(Arrays.asList(1L, 2L));

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(5)
                .extracting("citizenship.code")
                .containsOnly("643", "678")
                .containsOnlyOnce("678");
    }

    @Test
    public void test_getPersons_filterByTaxpayerStates() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setTaxpayerStates(Arrays.asList(2L, 3L));

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2)
                .extracting("taxpayerState.id")
                .containsOnly(2L, 3L);
    }

    @Test
    public void test_getPersons_filterBySourceSystems() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setSourceSystems(Arrays.asList(1L, 2L));

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2)
                .extracting("source.id")
                .containsOnly(1L, 2L);
    }

    @Test
    public void test_getPersons_filterByOldId() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setId("1");

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2)
                .extracting("oldId")
                .containsExactlyInAnyOrder(1L, 10L);
    }

    @Test
    public void test_getPersons_filterByInn() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setInn("234");

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2)
                .extracting("inn")
                .containsOnly("1234", "2345");
    }

    @Test
    public void test_getPersons_filterByInnForeign() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setInnForeign("3-D1");

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(1)
                .extracting("innForeign")
                .containsOnly("123-d123");
    }

    @Test
    public void test_getPersons_filterByRegion() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setRegion("7");

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2)
                .extracting("address.regionCode")
                .containsOnly("77");
    }

    @Test
    public void test_getPersons_filterByPostCode() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setPostalCode("43");

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons)
                .hasSize(3)
                .extracting("address.postalCode")
                .containsOnly("143080", "109431");
    }

    @Test
    public void test_getPersons_filterByDistrict() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setDistrict("Одинцовский");

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2)
                .extracting("address.district")
                .containsOnly("ОДИНЦОВСКИЙ р-н");
    }

    @Test
    public void test_getPersons_filterByLocality() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setLocality("внииссок");

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2)
                .extracting("address.locality")
                .containsOnly("ВНИИССОК п");
    }

    @Test
    public void test_getPersons_filterByStreet() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setStreet("Ленин");

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(1)
                .extracting("address.street")
                .containsOnly("Ленинские Горы ул");
    }

    @Test
    public void test_getPersons_filterByForeignAddressCountry() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setCountries(Arrays.asList(2L, 3L));

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2)
                .extracting("address.country.code")
                .containsOnly("678", "630");
    }

    @Test
    public void test_getPersons_filterByForeignAddress() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setForeignAddress("washington");

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(1);

        RefBookAddress personAddress = persons.get(0).getAddress();
        assertThat(personAddress.getAddress()).isEqualTo("COL, Washington DC, Kennedy street, 20");
    }

    @Test
    public void test_getPersons_filterByDuplicatesOnly() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setDuplicates(true);

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(2);

        RefBookPerson person = persons.get(0);
        assertThat(person.getRecordId()).isNotEqualTo(person.getOldId());
    }

    @Test
    public void test_getPersons_filterByNotDuplicates() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setDuplicates(false);

        PagingResult<RefBookPerson> persons = personDao.getPersons(null, filter);

        assertThat(persons).hasSize(4);

        RefBookPerson person = persons.get(0);
        assertThat(person.getRecordId()).isEqualTo(person.getOldId());
    }


    // --- Методы работы с  RegistryPerson ---

    @Test
    public void test_updateRegistryPerson() {
        //setup
        Map<String, RefBookValue> citizenship = new HashMap<>();
        RefBookValue citizenshipId = new RefBookValue(RefBookAttributeType.NUMBER, 2L);
        citizenship.put("id", citizenshipId);

        Map<String, RefBookValue> reportDoc = new HashMap<>();
        RefBookValue reportDocId = new RefBookValue(RefBookAttributeType.NUMBER, 4L);
        reportDoc.put("id", reportDocId);

        Map<String, RefBookValue> asnu = new HashMap<>();
        RefBookValue asnuId = new RefBookValue(RefBookAttributeType.NUMBER, 5L);
        asnu.put("id", asnuId);

        Map<String, RefBookValue> taxPayerState = new HashMap<>();
        RefBookValue taxPayerStateId = new RefBookValue(RefBookAttributeType.NUMBER, 6L);
        taxPayerState.put("id", taxPayerStateId);

        RegistryPersonDTO person = new RegistryPersonDTO();
        person.setId(1L);
        person.setLastName("ИзмФам");
        person.setFirstName("ИзмИм");
        person.setMiddleName("ИзмОтч");
        person.setCitizenship(Permissive.of(citizenship));
        person.setReportDoc(Permissive.of(reportDoc));
        person.setInn(Permissive.of("121212121212"));
        person.setInnForeign(Permissive.of("343434"));
        person.setSnils(Permissive.of("12-123"));
        person.setTaxPayerState(Permissive.of(taxPayerState));
        person.setSource(asnu);

        //language=sql
        String query = "UPDATE ref_book_person set last_name = :lastName, first_name = :firstName, " +
                "middle_name = :middleName, birth_date = :birthDate, citizenship = :citizenship, " +
                "report_doc = :reportDoc, inn = :inn, inn_foreign = :innForeign, snils = :snils, " +
                "taxpayer_state = :taxPayerState, source_id = :source where id = :id";
        //execution
        personDao.updateRegistryPerson(person, query);

        //verification
        RegistryPersonDTO result = personDao.fetchPersonWithVersionInfo(1L);
        assertThat(result.getLastName()).isEqualTo("ИзмФам");
        assertThat(result.getFirstName()).isEqualTo("ИзмИм");
        assertThat(result.getMiddleName()).isEqualTo("ИзмОтч");
        assertThat(result.getCitizenship().value().get("CITIZENSHIP").getReferenceValue()).isEqualTo(2L);
        assertThat(result.getReportDoc().value().get("REPORT_DOC").getReferenceValue()).isEqualTo(4L);
        assertThat(result.getInn().value()).isEqualTo("121212121212");
        assertThat(result.getInnForeign().value()).isEqualTo("343434");
        assertThat(result.getSnils().value()).isEqualTo("12-123");
        assertThat(result.getTaxPayerState().value().get("TAXPAYER_STATE").getReferenceValue()).isEqualTo(6L);
        assertThat(result.getSource().get("SOURCE_ID").getReferenceValue()).isEqualTo(5L);

    }

    @Test
    public void test_updateRegistryPersonAddress() {
        //setup
        Map<String, RefBookValue> addressVal = new HashMap<>();
        RefBookValue addressId = new RefBookValue(RefBookAttributeType.NUMBER, 1L);
        RefBookValue regionCode = new RefBookValue(RefBookAttributeType.STRING, "12");
        RefBookValue postalCode = new RefBookValue(RefBookAttributeType.STRING, "234567");
        RefBookValue district = new RefBookValue(RefBookAttributeType.STRING, "ИзмРайон");
        RefBookValue city = new RefBookValue(RefBookAttributeType.STRING, "ИзмГород");
        RefBookValue locality = new RefBookValue(RefBookAttributeType.STRING, "ИзмНасПункт");
        RefBookValue street = new RefBookValue(RefBookAttributeType.STRING, "ИзмУлица");
        RefBookValue house = new RefBookValue(RefBookAttributeType.STRING, "22");
        RefBookValue build = new RefBookValue(RefBookAttributeType.STRING, "2");
        RefBookValue appartment = new RefBookValue(RefBookAttributeType.STRING, "32");
        RefBookValue country_id = new RefBookValue(RefBookAttributeType.REFERENCE, 3L);

        addressVal.put("id", addressId);
        addressVal.put("REGION_CODE", regionCode);
        addressVal.put("POSTAL_CODE", postalCode);
        addressVal.put("DISTRICT", district);
        addressVal.put("CITY", city);
        addressVal.put("LOCALITY", locality);
        addressVal.put("STREET", street);
        addressVal.put("HOUSE", house);
        addressVal.put("BUILD", build);
        addressVal.put("APPARTMENT", appartment);
        addressVal.put("COUNTRY_ID", country_id);

        RegistryPersonDTO person = new RegistryPersonDTO();
        person.setId(1L);
        person.setAddress(Permissive.of(addressVal));
        String sql = "UPDATE ref_book_address set region_code = :REGION_CODE, postal_code = :POSTAL_CODE, " +
                "district = :DISTRICT, city = :CITY, locality = :LOCALITY, street = :STREET, house = :HOUSE, " +
                "build = :BUILD, appartment = :APPARTMENT, country_id = :COUNTRY_ID where id = :id";
        // execution
        personDao.updateRegistryPersonAddress(person.getAddress().value(), sql);

        //verification
        RefBook refBook = refBookDao.get(RefBook.Id.PERSON_ADDRESS.getId());
        Map<String, RefBookValue> result = refBookSimpleDao.getRecordData(refBook, 1L);

        assertThat(result.get("REGION_CODE").getStringValue()).isEqualTo("12");
        assertThat(result.get("POSTAL_CODE").getStringValue()).isEqualTo("234567");
        assertThat(result.get("DISTRICT").getStringValue()).isEqualTo("ИзмРайон");
        assertThat(result.get("CITY").getStringValue()).isEqualTo("ИзмГород");
        assertThat(result.get("LOCALITY").getStringValue()).isEqualTo("ИзмНасПункт");
        assertThat(result.get("STREET").getStringValue()).isEqualTo("ИзмУлица");
        assertThat(result.get("HOUSE").getStringValue()).isEqualTo("22");
        assertThat(result.get("BUILD").getStringValue()).isEqualTo("2");
        assertThat(result.get("APPARTMENT").getStringValue()).isEqualTo("32");
        assertThat(result.get("COUNTRY_ID").getReferenceValue().longValue()).isEqualTo(3L);
    }

    @Test
    public void test_updateRegistryPersonIncRepDocId() {
        //setup
        //execution
        personDao.updateRegistryPersonIncRepDocId(1L, 4L);


        RefBook refBook = refBookDao.get(RefBook.Id.ID_DOC.getId());
        Map<String, RefBookValue> oldValue = refBookSimpleDao.getRecordData(refBook, 1L);
        Map<String, RefBookValue> newValue = refBookSimpleDao.getRecordData(refBook, 4L);

        assertThat(oldValue.get("INC_REP").getNumberValue().intValue()).isEqualTo(0);
        assertThat(newValue.get("INC_REP").getNumberValue().intValue()).isEqualTo(1);
    }

    @Test
    public void test_fetchOriginalDuplicatesCandidates() {
        PagingResult<RefBookPerson> persons = personDao.fetchOriginalDuplicatesCandidates(null, null);

        assertThat(persons).hasSize(6);
        assertThat(persons.getTotalCount()).isEqualTo(6);
    }

    @Test
    public void test_setOriginal() {
        //setup
        //execution
        personDao.setOriginal(5L, 5L, 2L);
        //verification
        RegistryPersonDTO changedPerson = personDao.fetchPersonWithVersionInfo(5L);
        assertThat(changedPerson.getRecordId()).isEqualTo(2L);
    }

    @Test
    public void test_deleteOriginal() {
        //setup
        //execution
        personDao.deleteOriginal(1L, 10L);
        //verification
        RegistryPersonDTO changedPerson = personDao.fetchPersonWithVersionInfo(4L);
        assertThat(changedPerson.getRecordId()).isEqualTo(10L);
    }

    @Test
    public void setDuplicates() {
        //setup
        //execution
        personDao.setDuplicates(Collections.singletonList(5L), 2L);
        //verification
        RegistryPersonDTO changedPerson = personDao.fetchPersonWithVersionInfo(5L);
        assertThat(changedPerson.getRecordId()).isEqualTo(2L);
    }

    @Test
    public void test_deleteDuplicates() {
        //setup
        //execution
        personDao.deleteDuplicates(Collections.singletonList(10L));
        //verification
        RegistryPersonDTO changedPerson = personDao.fetchPersonWithVersionInfo(4L);
        assertThat(changedPerson.getRecordId()).isEqualTo(10L);
    }

}

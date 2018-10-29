package com.aplana.sbrf.taxaccounting.service.print.persons;

import com.aplana.sbrf.taxaccounting.model.Permissive;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.service.impl.print.persons.PersonsReportBuilder;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PersonsExcelReportBuilderTest {

    @Test
    public void test() throws Exception {
        List<RegistryPersonDTO> persons = new ArrayList<>();
        persons.add(person1());
        persons.add(person2());

        PersonsReportBuilder reportBuilder = new PersonsReportBuilder(persons, filter());
        String filepath = null;
        try {
            filepath = reportBuilder.createReport();
        } finally {
            if (filepath != null) {
                new File(filepath).delete();
            }
        }
    }

    private RegistryPersonDTO person1() {
        RegistryPersonDTO person = new RegistryPersonDTO();
        person.setId(1L);
        person.setRecordId(1L);
        person.setVip(true);
        person.setLastName("Фамилия1");
        person.setFirstName("Имя1");
        person.setMiddleName("Отч1");
        person.setReportDoc(Permissive.<IdDoc>forbidden());
        RefBookCountry country = new RefBookCountry();
        country.setCode("countryCode");
        country.setName("countryName");
        person.setCitizenship(Permissive.of(country));
        RefBookTaxpayerState taxpayerState = new RefBookTaxpayerState();
        taxpayerState.setCode("taxpayerStateCode");
        taxpayerState.setName("taxpayerStateName");
        person.setTaxPayerState(Permissive.of(taxpayerState));
        person.setInn(Permissive.of("inn"));
        person.setInnForeign(Permissive.<String>forbidden());
        person.setSnils(Permissive.of("snils"));
        person.setAddress(Permissive.of(address1()));
        RefBookAsnu asnu = new RefBookAsnu();
        asnu.setCode("asnuCode");
        asnu.setName("asnuName");
        person.setSource(asnu);
        person.setStartDate(new LocalDate(2018, 5, 5).toDate());
        person.setEndDate(new LocalDate(2018, 12, 31).toDate());
        return person;
    }

    private Address address1() {
        Address address = new Address();
        address.setPostalCode("postalCode");
        address.setRegionCode("regionCode");
        address.setDistrict("district");
        address.setCity("city");
        address.setLocality("locality");
        address.setStreet("street");
        address.setHouse("house");
        address.setBuild("build");
        address.setAppartment("appartment");

        RefBookCountry country = new RefBookCountry();
        country.setCode("countryCode");
        country.setName("countryName");
        address.setCountry(country);
        address.setAddressIno("address");
        return address;
    }

    private RegistryPersonDTO person2() {
        RegistryPersonDTO person = new RegistryPersonDTO();
        person.setId(2L);
        person.setRecordId(3L);
        person.setOldId(2L);
        person.setLastName("Фамилия2");
        Address address = address1();
        person.setAddress(Permissive.of(address));
        return person;
    }

    private RefBookPersonFilter filter() {
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setLastName("Фам");
        filter.setFirstName("Им");
        filter.setMiddleName("Отч");
        filter.setBirthDateFrom(new LocalDate(1950, 1, 1).toDate());
        filter.setBirthDateTo(null);
        filter.setDocumentNumber("");
        filter.setId("1");

        filter.setVersionDate(new Date());
        filter.setDuplicates(true);
        return filter;
    }
}

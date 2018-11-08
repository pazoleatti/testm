package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.dao.identification.IdentificationUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.Configuration;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.identification.IdentificationData;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocType;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.model.ConfigurationParam.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Drunk
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("RefBookPersonServiceTest.xml")
@DirtiesContext
public class RefBookPersonServiceTest {

    @Autowired
    RefBookPersonService personService;

    private List<NaturalPerson> getList() {
        List<NaturalPerson> personDataList = new ArrayList<NaturalPerson>();
        personDataList.add(createNaturalPerson(1L, "999", "1", "", "123-000-111 56", "", "1111", "Иванов", "Иван", "Иванович", null, 1L));
        personDataList.add(createNaturalPerson(2L, "888", "2", "", "33", null, "2222", "Сидорова", "Наталья", "Викторовна", "12.10.1954", 2L));
        personDataList.add(createNaturalPerson(3L, "777", "", "5", "45", "", "1111", "Кулькина", "Василина", null, "", 3L));
        personDataList.add(createNaturalPerson(4L, "888", "2", "", "23", null, "1111", "Иванов", "Иван", "Иванович", "12.10.1954", 4L));
        personDataList.add(createNaturalPerson(5L, "888", "2", "", "23", "", "1111", "Иванов", "Ивон", "Ивановиеч", "12.10.1954", 5L));
        personDataList.add(createNaturalPerson(6L, "888", "2", "", "123-000-111 56", "", "1111", "Иванов", "Иван", "Иванович", "12.10.1954", 6L));
        return personDataList;
    }

    @Before
    public void init() {
        RefBookPersonDao refBookPersonDao = mock(RefBookPersonDao.class);
        ConfigurationService configurationService = mock(ConfigurationService.class);
        //when(refBookPersonDao.findPersonByPersonData(any(PersonData.class), any(Date.class))).thenReturn(getList());
        ReflectionTestUtils.setField(personService, "refBookPersonDao", refBookPersonDao);
        ReflectionTestUtils.setField(personService, "configurationService", configurationService);

        List<Configuration> defaultCommonConfig = new ArrayList<>();
        defaultCommonConfig.add(new Configuration(WEIGHT_LAST_NAME.name(), 0, "5"));
        defaultCommonConfig.add(new Configuration(WEIGHT_FIRST_NAME.name(), 0, "10"));
        defaultCommonConfig.add(new Configuration(WEIGHT_MIDDLE_NAME.name(), 0, "5"));
        defaultCommonConfig.add(new Configuration(WEIGHT_BIRTHDAY.name(), 0, "10"));
        defaultCommonConfig.add(new Configuration(WEIGHT_CITIZENSHIP.name(), 0, "1"));
        defaultCommonConfig.add(new Configuration(WEIGHT_INP.name(), 0, "15"));
        defaultCommonConfig.add(new Configuration(WEIGHT_INN.name(), 0, "10"));
        defaultCommonConfig.add(new Configuration(WEIGHT_INN_FOREIGN.name(), 0, "10"));
        defaultCommonConfig.add(new Configuration(WEIGHT_SNILS.name(), 0, "15"));
        defaultCommonConfig.add(new Configuration(WEIGHT_TAX_PAYER_STATUS.name(), 0, "1"));
        defaultCommonConfig.add(new Configuration(WEIGHT_DUL.name(), 0, "10"));
        defaultCommonConfig.add(new Configuration(WEIGHT_ADDRESS.name(), 0, "1"));
        defaultCommonConfig.add(new Configuration(WEIGHT_ADDRESS_INO.name(), 0, "1"));
        Map<String, Configuration> map = new HashMap<>();
        for (Configuration configuration : defaultCommonConfig) {
            map.put(configuration.getCode(), configuration);
        }

        when(configurationService.fetchAllByEnums(anyListOf(ConfigurationParam.class))).thenReturn(map);
    }

    @Test
    public void identificatePersonTest() {
        NaturalPerson primaryPerson = createPersonData("888", "2", "", "12300011156", "", "1111", "Иванов", "Иван", "Ивановиеч", "12.10.1954");
        Map<Long, RefBookAsnu> priorityMap = new HashMap<>();
        RefBookAsnu priorityAsnu = new RefBookAsnu();
        priorityAsnu.setPriority(100);
        priorityMap.put(1L, priorityAsnu);
        priorityMap.put(2L, priorityAsnu);
        priorityMap.put(3L, priorityAsnu);
        priorityMap.put(4L, priorityAsnu);
        priorityMap.put(5L, priorityAsnu);
        priorityMap.put(6L, priorityAsnu);
        IdentificationData identificationDataFixture = new IdentificationData();
        identificationDataFixture.setNaturalPerson(primaryPerson);
        identificationDataFixture.setRefBookPersonList(getList());
        identificationDataFixture.setTresholdValue(900);
        identificationDataFixture.setDeclarationDataAsnuId(1);
        identificationDataFixture.setPriorityMap(priorityMap);
        NaturalPerson result = personService.identificatePerson(identificationDataFixture, new Logger());

        assertEquals(Long.valueOf(6L), result.getId());
    }

    @Test
    public void identificatePersonAsnuLowPriorityTest() {
        NaturalPerson primaryPerson = createPersonData("888", "2", "", "12300011156", "", "1111", "Иванов", "Иван", "Ивановиеч", "12.10.1954");
        Map<Long, RefBookAsnu> priorityMap = new HashMap<>();
        RefBookAsnu priorityAsnu100 = new RefBookAsnu();
        priorityAsnu100.setPriority(100);
        RefBookAsnu priorityAsnu200 = new RefBookAsnu();
        priorityAsnu200.setPriority(200);
        priorityMap.put(1L, priorityAsnu100);
        priorityMap.put(2L, priorityAsnu200);
        priorityMap.put(3L, priorityAsnu200);
        priorityMap.put(4L, priorityAsnu200);
        priorityMap.put(5L, priorityAsnu200);
        priorityMap.put(6L, priorityAsnu200);
        IdentificationData identificationDataFixture = new IdentificationData();
        identificationDataFixture.setNaturalPerson(primaryPerson);
        identificationDataFixture.setRefBookPersonList(getList());
        identificationDataFixture.setTresholdValue(900);
        identificationDataFixture.setDeclarationDataAsnuId(1);
        identificationDataFixture.setPriorityMap(priorityMap);
        NaturalPerson result = personService.identificatePerson(identificationDataFixture, new Logger());

        assertFalse(result.isNeedUpdate());
    }

    @Test
    public void identificatePersonEqualWeightTest() {
        NaturalPerson primaryPerson = createPersonData("888", "2", "", "12300011156", "", "1111", "Иванов", "Иван", "Ивановиеч", "12.10.1954");
        Map<Long, RefBookAsnu> priorityMap = new HashMap<>();
        RefBookAsnu priorityAsnu = new RefBookAsnu();
        priorityAsnu.setPriority(100);
        priorityMap.put(1L, priorityAsnu);
        List<NaturalPerson> personDataList = new ArrayList<NaturalPerson>();
        personDataList.add(createNaturalPerson(1L, "888", "2", "", "12300011156", "", "1112", "Иванов", "Иван", "Ивановиеч", "12.10.1954", 1L));
        personDataList.add(createNaturalPerson(2L, "888", "2", "", "12300011156", "", "1113", "Иванов", "Иван", "Ивановиеч", "12.10.1954", 1L));
        personDataList.add(createNaturalPerson(5L, "888", "2", "", "12300011156", "", "1114", "Иванов", "Иван", "Ивановиеч", "12.10.1954", 1L));
        personDataList.add(createNaturalPerson(4L, "888", "2", "", "12300011156", "", "1115", "Иванов", "Иван", "Ивановиеч", "12.10.1954", 1L));
        personDataList.add(createNaturalPerson(3L, "888", "2", "", "12300011156", "", "1116", "Иванов", "Иван", "Ивановиеч", "12.10.1954", 1L));
        IdentificationData identificationDataFixture = new IdentificationData();
        identificationDataFixture.setNaturalPerson(primaryPerson);
        identificationDataFixture.setRefBookPersonList(personDataList);
        identificationDataFixture.setTresholdValue(650);
        identificationDataFixture.setDeclarationDataAsnuId(1);
        identificationDataFixture.setPriorityMap(priorityMap);
        NaturalPerson result = personService.identificatePerson(identificationDataFixture, new Logger());
        assertEquals(Long.valueOf(5L), result.getId());
    }

    public NaturalPerson createPersonData(Long id, String inp, String inn, String innForeign, String snils, String docType,
                                          String docNumber,
                                          String lastName,
                                          String firstName, String middleName, String birthDate) {
        NaturalPerson result = new NaturalPerson();
        result.setId(id);
        result.setRecordId(id);

        result.setInn(inn);
        result.setInnForeign(innForeign);
        result.setSnils(snils);

        result.getPersonIdentityList().add(createPersonIdentifier(1L, inp, 5L));
        result.getDocuments().add(createPersonDocument(5L, docNumber));

        result.setLastName(lastName);
        result.setFirstName(firstName);
        result.setMiddleName(middleName);
        result.setBirthDate(toDate(birthDate));

        IdDoc idDoc = new IdDoc();
        RefBookDocType refBookDocType = new RefBookDocType();
        refBookDocType.setName(docType);
        idDoc.setDocType(refBookDocType);
        result.setReportDoc(idDoc);

        return result;
    }


    public NaturalPerson createPersonData(String inp, String inn, String innForeign, String snils, String docType,
                                          String docNumber,
                                          String lastName,
                                          String firstName, String middleName, String birthDate) {
        return createPersonData(null, inp, inn, innForeign, snils, docType, docNumber, lastName, firstName, middleName, birthDate);
    }

    public NaturalPerson createNaturalPerson(Long id, String inp, String inn, String innForeign, String snils, String docType,
                                             String docNumber,
                                             String lastName,
                                             String firstName, String middleName, String birthDate, Long sourceId) {
        NaturalPerson result = new NaturalPerson();
        result.setId(id);
        result.setRecordId(id);

        result.getPersonIdentityList().add(createPersonIdentifier(1L, inp, 5L));
        result.getDocuments().add(createPersonDocument(5L, docNumber));

        result.setInn(inn);
        result.setInnForeign(innForeign);
        result.setSnils(snils);

        result.setLastName(lastName);
        result.setFirstName(firstName);
        result.setMiddleName(middleName);
        result.setBirthDate(toDate(birthDate));
        RefBookAsnu source = new RefBookAsnu();
        source.setId(sourceId);
        result.setSource(source);

        IdDoc idDoc = new IdDoc();
        RefBookDocType refBookDocType = new RefBookDocType();
        refBookDocType.setName(docType);
        idDoc.setDocType(refBookDocType);
        result.setReportDoc(idDoc);

        return result;
    }

    private PersonIdentifier createPersonIdentifier(Long id, String inp, Long asnuId) {
        PersonIdentifier personIdentifier = new PersonIdentifier();
        personIdentifier.setId(id);
        personIdentifier.setInp(inp);
        RefBookAsnu asnu = new RefBookAsnu();
        asnu.setId(asnuId);
        personIdentifier.setAsnu(asnu);
        return personIdentifier;
    }

    private IdDoc createPersonDocument(Long docTypeId, String documentNumber) {
        RefBookDocType docTypeObject = new RefBookDocType();
        docTypeObject.setId(5L);
        IdDoc personDocument = new IdDoc();
        personDocument.setDocType(docTypeObject);
        personDocument.setDocumentNumber(documentNumber);
        return personDocument;
    }


    public static Date toDate(String dateStr) {
        try {
            return (dateStr != null && !dateStr.isEmpty()) ? new SimpleDateFormat("dd.MM.yyyy").parse(dateStr) : null;
        } catch (ParseException e) {
            return null;
        }
    }

    @Test
    public void buildNoticeTest() {
        {
            NaturalPerson naturalPerson = createNaturalPerson(1L, "999", "1", "", "123-000-111 56", "", "1111", "Иванов", "Иван", "Иванович", null, 1L);
            String notice = IdentificationUtils.buildNotice(naturalPerson);
            System.out.println(notice);
        }

        {
            NaturalPerson naturalPerson = createNaturalPerson(1L, null, "1", "", "123-000-111 56", "", "1111", "Иванов", "Иван", "Иванович", null, 1L);
            String notice = IdentificationUtils.buildNotice(naturalPerson);
            System.out.println(notice);
        }
    }

    public void putValue(Map<String, RefBookValue> values, String attrName, RefBookAttributeType type, Object value) {
        values.put(attrName, new RefBookValue(type, value));
    }

    @Test
    public void buildRefBookNoticeTest() {

        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
        putValue(values, RefBook.RECORD_ID_ALIAS, RefBookAttributeType.NUMBER, 112233L);
        putValue(values, "LAST_NAME", RefBookAttributeType.STRING, "Сидоров");
        putValue(values, "FIRST_NAME", RefBookAttributeType.STRING, "Иван");
        putValue(values, "MIDDLE_NAME", RefBookAttributeType.STRING, "Иванович");
        putValue(values, "SEX", RefBookAttributeType.NUMBER, 1);
        putValue(values, "INN", RefBookAttributeType.STRING, "123456");
        putValue(values, "INN_FOREIGN", RefBookAttributeType.STRING, "123456");
        putValue(values, "SNILS", RefBookAttributeType.STRING, "123456");
        putValue(values, "RECORD_ID", RefBookAttributeType.NUMBER, 654987);
        putValue(values, "BIRTH_DATE", RefBookAttributeType.DATE, new Date());
        putValue(values, "BIRTH_PLACE", RefBookAttributeType.STRING, "MSK");
        putValue(values, "ADDRESS", RefBookAttributeType.REFERENCE, 987654L);
        putValue(values, "PENSION", RefBookAttributeType.NUMBER, 2);
        putValue(values, "MEDICAL", RefBookAttributeType.NUMBER, 1);
        putValue(values, "SOCIAL", RefBookAttributeType.NUMBER, 2);
        putValue(values, "EMPLOYEE", RefBookAttributeType.NUMBER, 1);
        putValue(values, "CITIZENSHIP", RefBookAttributeType.REFERENCE, 123L);
        putValue(values, "TAXPAYER_STATE", RefBookAttributeType.REFERENCE, 654L);
        putValue(values, "SOURCE_ID", RefBookAttributeType.REFERENCE, 4L);
        putValue(values, "OLD_ID", RefBookAttributeType.REFERENCE, null);

        String value = IdentificationUtils.buildRefBookNotice(values);

        System.out.println(value);

    }


}

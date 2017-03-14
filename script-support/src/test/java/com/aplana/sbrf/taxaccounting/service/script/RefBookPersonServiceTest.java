package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.dao.identification.IdentificationUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.identification.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Andrey Drunk
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("RefBookPersonServiceTest.xml")
@DirtiesContext
public class RefBookPersonServiceTest {

    @Autowired
    RefBookPersonService personService;

    private List<IdentityPerson> getList() {
        List<IdentityPerson> personDataList = new ArrayList<IdentityPerson>();
        personDataList.add(createNaturalPerson(1L, "999", "1", "", "123-000-111 56", "", "1111", "Иванов", "Иван", "Иванович", null));
        personDataList.add(createNaturalPerson(2L, "888", "2", "", "33", null, "2222", "Сидорова", "Наталья", "Викторовна", "12.10.1954"));
        personDataList.add(createNaturalPerson(3L, "777", "", "5", "45", "", "1111", "Кулькина", "Василина", null, ""));
        personDataList.add(createNaturalPerson(4L, "888", "2", "", "23", null, "1111", "Иванов", "Иван", "Иванович", "12.10.1954"));
        personDataList.add(createNaturalPerson(5L, "888", "2", "", "23", "", "1111", "Иванов", "Ивон", "Ивановиеч", "12.10.1954"));
        personDataList.add(createNaturalPerson(6L, "888", "2", "", "123-000-111 56", "", "1111", "Иванов", "Иван", "Иванович", "12.10.1954"));
        return personDataList;
    }

    @Before
    public void init() {
        RefBookPersonDao refBookPersonDao = mock(RefBookPersonDao.class);
        //when(refBookPersonDao.findPersonByPersonData(any(PersonData.class), any(Date.class))).thenReturn(getList());
        ReflectionTestUtils.setField(personService, "refBookPersonDao", refBookPersonDao);
    }

    @Test
    public void identificatePersonTest() {
        NaturalPerson primaryPerson = createPersonData("888", "2", "", "12300011156", "", "1111", "Иванов", "Иван", "Ивановиеч", "12.10.1954");
        NaturalPerson result = personService.identificatePerson(primaryPerson, getList(), 900, new Logger());

        assertEquals(Long.valueOf(6L), result.getId());
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
        result.getPersonDocumentList().add(createPersonDocument(5L, 1, docNumber));

        result.setLastName(lastName);
        result.setFirstName(firstName);
        result.setMiddleName(middleName);
        result.setBirthDate(toDate(birthDate));
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
                                             String firstName, String middleName, String birthDate) {
        NaturalPerson result = new NaturalPerson();
        result.setId(id);
        result.setRecordId(id);

        result.getPersonIdentityList().add(createPersonIdentifier(1L, inp, 5L));
        result.getPersonDocumentList().add(createPersonDocument(5L, 1, docNumber));

        result.setInn(inn);
        result.setInnForeign(innForeign);
        result.setSnils(snils);

        result.setLastName(lastName);
        result.setFirstName(firstName);
        result.setMiddleName(middleName);
        result.setBirthDate(toDate(birthDate));
        return result;
    }

    private PersonIdentifier createPersonIdentifier(Long id, String inp, Long asnuId) {
        PersonIdentifier personIdentifier = new PersonIdentifier();
        personIdentifier.setId(id);
        personIdentifier.setInp(inp);
        personIdentifier.setAsnuId(asnuId);
        return personIdentifier;
    }

    private PersonDocument createPersonDocument(Long docTypeId, Integer incRep, String documentNumber) {
        DocType docTypeObject = new DocType();
        docTypeObject.setId(5L);
        PersonDocument personDocument = new PersonDocument();
        personDocument.setDocType(docTypeObject);
        personDocument.setIncRep(1);
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
            NaturalPerson naturalPerson = createNaturalPerson(1L, "999", "1", "", "123-000-111 56", "", "1111", "Иванов", "Иван", "Иванович", null);
            String notice = IdentificationUtils.buildNotice(naturalPerson);
            System.out.println(notice);
        }

        {
            NaturalPerson naturalPerson = createNaturalPerson(1L, null, "1", "", "123-000-111 56", "", "1111", "Иванов", "Иван", "Иванович", null);
            String notice = IdentificationUtils.buildNotice(naturalPerson);
            System.out.println(notice);
        }
    }

}

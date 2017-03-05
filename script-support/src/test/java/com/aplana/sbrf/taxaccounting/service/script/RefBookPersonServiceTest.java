package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.PersonData;
import com.aplana.sbrf.taxaccounting.model.identity.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.BaseWeigthCalculator;
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
import static org.mockito.Matchers.any;
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

    private List<IdentityPerson> getList(){
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
        PersonData person = createPersonData("888", "2", "", "12300011156", "", "1111", "Иванов", "Иван", "Ивановиеч", "12.10.1954");
        Long result = personService.identificatePerson(person, getList(), 900, new Logger());
        assertEquals(Long.valueOf(6L), result);
    }


    public PersonData createPersonData(Long id, String inp, String inn, String innForeign, String snils, String docType,
                                       String docNumber,
                                       String lastName,
                                       String firstName, String middleName, String birthDate) {
        PersonData result = new PersonData();
        result.setId(id);
        result.setRecordId(id);
        result.setAsnuId(5L);
        result.setInp(inp);
        result.setInn(inn);
        result.setInnForeign(innForeign);
        result.setSnils(snils);
        result.setDocumentTypeId(5L);
        result.setDocumentNumber(docNumber);
        result.setLastName(lastName);
        result.setFirstName(firstName);
        result.setMiddleName(middleName);
        result.setBirthDate(toDate(birthDate));
        return result;
    }




    public PersonData createPersonData(String inp, String inn, String innForeign, String snils, String docType,
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

        PersonIdentifier personIdentifier = new PersonIdentifier();
        personIdentifier.setInp(inp);
        personIdentifier.setAsnuId(5L);
        result.getPersonIdentityMap().put(1L, personIdentifier);


        DocType docTypeObject = new DocType();
        docTypeObject.setId(5L);

        PersonDocument personDocument = new PersonDocument();
        personDocument.setDocType(docTypeObject);
        personDocument.setIncRep(1);
        personDocument.setDocumentNumber(docNumber);

        result.getPersonDocumentMap().put(1L, personDocument);

        result.setInn(inn);
        result.setInnForeign(innForeign);
        result.setSnils(snils);

        result.setLastName(lastName);
        result.setFirstName(firstName);
        result.setMiddleName(middleName);
        result.setBirthDate(toDate(birthDate));
        return result;
    }


    public static Date toDate(String dateStr) {
        try {
            return (dateStr != null && !dateStr.isEmpty()) ? new SimpleDateFormat("dd.MM.yyyy").parse(dateStr) : null;
        } catch (ParseException e) {
            return null;
        }
    }

}

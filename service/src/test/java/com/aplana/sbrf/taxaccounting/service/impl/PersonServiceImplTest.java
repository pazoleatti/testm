package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.service.PersonService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("PersonServiceImplTest.xml")
public class PersonServiceImplTest {

    @Autowired
    private PersonService personService;
    @Autowired
    private RefBookPersonDao refBookPersonDao;

    @Test
    public void testCreateSearchFilterLastNameEmptyFirstNameEmptySearchPatternEmptyApproxSearch() {
        String result = personService.createSearchFilter( null, null, null, false);
        Assert.assertEquals("", result);
    }

    @Test
    public void testCreateSearchFilterLastNameEmptyFirstNameEmptySearchPatternFilledApproxSearch() {
        String result = personService.createSearchFilter( null, null, "A", false);
        String expected = "(TO_CHAR(RECORD_ID) like '%a%' or LOWER(LAST_NAME) like '%a%' or " +
                "LOWER(FIRST_NAME) like '%a%' or LOWER(MIDDLE_NAME) like '%a%' or LOWER(INN) like '%a%' or " +
                "LOWER(INN_FOREIGN) like '%a%' or LOWER(SNILS) like '%a%' or LOWER(TAXPAYER_STATE_CODE) like '%a%' or " +
                "LOWER(BIRTH_PLACE) like '%a%' or LOWER(CITIZENSHIP_CODE) like '%a%' or TO_CHAR(EMPLOYEE) like '%a%' or " +
                "LOWER(SOURCE_ID_CODE) like '%a%' or TO_CHAR(OLD_ID) like '%a%' or LOWER(ADDRESS_ADDRESS_FULL) like '%a%')";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameEmptyFirstNameEmptySearchPatternFilledExactSearch() {
        String result = personService.createSearchFilter( null, null, "A", true);
        String expected = "(TO_CHAR(RECORD_ID) = 'a' or LOWER(LAST_NAME) = 'a' or " +
                "LOWER(FIRST_NAME) = 'a' or LOWER(MIDDLE_NAME) = 'a' or LOWER(INN) = 'a' or " +
                "LOWER(INN_FOREIGN) = 'a' or LOWER(SNILS) = 'a' or LOWER(TAXPAYER_STATE_CODE) = 'a' or " +
                "LOWER(BIRTH_PLACE) = 'a' or LOWER(CITIZENSHIP_CODE) = 'a' or TO_CHAR(EMPLOYEE) = 'a' or " +
                "LOWER(SOURCE_ID_CODE) = 'a' or TO_CHAR(OLD_ID) = 'a' or LOWER(ADDRESS_ADDRESS_FULL) = 'a')";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameEmptyFirstNameFilledSearchPatternEmptyApproxSearch() {
        String result = personService.createSearchFilter( "F", null, null, false);
        String expected = "LOWER(FIRST_NAME) like '%f%'";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameEmptyFirstNameFilledSearchPatternEmptyExactSearch() {
        String result = personService.createSearchFilter("F", null, null, true);
        String expected = "LOWER(FIRST_NAME) = 'f'";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameEmptyFirstNameFilledSearchPatternFilledApproxSearch() {
        String result = personService.createSearchFilter( "F", null, "A", false);
        String expected = "LOWER(FIRST_NAME) like '%f%' and (TO_CHAR(RECORD_ID) like '%a%' or LOWER(LAST_NAME) like '%a%' or " +
                "LOWER(MIDDLE_NAME) like '%a%' or LOWER(INN) like '%a%' or " +
                "LOWER(INN_FOREIGN) like '%a%' or LOWER(SNILS) like '%a%' or LOWER(TAXPAYER_STATE_CODE) like '%a%' or " +
                "LOWER(BIRTH_PLACE) like '%a%' or LOWER(CITIZENSHIP_CODE) like '%a%' or TO_CHAR(EMPLOYEE) like '%a%' or " +
                "LOWER(SOURCE_ID_CODE) like '%a%' or TO_CHAR(OLD_ID) like '%a%' or LOWER(ADDRESS_ADDRESS_FULL) like '%a%')";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameEmptyFirstNameFilledSearchPatternFilledExactSearch() {
        String result = personService.createSearchFilter( "F", null, "A", true);
        String expected = "LOWER(FIRST_NAME) = 'f' and (TO_CHAR(RECORD_ID) = 'a' or LOWER(LAST_NAME) = 'a' or " +
                "LOWER(MIDDLE_NAME) = 'a' or LOWER(INN) = 'a' or " +
                "LOWER(INN_FOREIGN) = 'a' or LOWER(SNILS) = 'a' or LOWER(TAXPAYER_STATE_CODE) = 'a' or " +
                "LOWER(BIRTH_PLACE) = 'a' or LOWER(CITIZENSHIP_CODE) = 'a' or TO_CHAR(EMPLOYEE) = 'a' or " +
                "LOWER(SOURCE_ID_CODE) = 'a' or TO_CHAR(OLD_ID) = 'a' or LOWER(ADDRESS_ADDRESS_FULL) = 'a')";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameFilledFirstNameEmptySearchPatternEmptyApproxSearch() {
        String result = personService.createSearchFilter( null, "L", null, false);
        String expected = "LOWER(LAST_NAME) like '%l%'";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameFilledFirstNameEmptySearchPatternEmptyExactSearch() {
        String result = personService.createSearchFilter( null, "L", null, true);
        String expected = "LOWER(LAST_NAME) = 'l'";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameFilledFirstNameEmptySearchPatternFilledApproxSearch() {
        String result = personService.createSearchFilter( null, "L", "A", false);
        String expected = "LOWER(LAST_NAME) like '%l%' and (TO_CHAR(RECORD_ID) like '%a%' or LOWER(FIRST_NAME) like '%a%' or " +
                "LOWER(MIDDLE_NAME) like '%a%' or LOWER(INN) like '%a%' or " +
                "LOWER(INN_FOREIGN) like '%a%' or LOWER(SNILS) like '%a%' or LOWER(TAXPAYER_STATE_CODE) like '%a%' or " +
                "LOWER(BIRTH_PLACE) like '%a%' or LOWER(CITIZENSHIP_CODE) like '%a%' or TO_CHAR(EMPLOYEE) like '%a%' or " +
                "LOWER(SOURCE_ID_CODE) like '%a%' or TO_CHAR(OLD_ID) like '%a%' or LOWER(ADDRESS_ADDRESS_FULL) like '%a%')";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameFilledFirstNameEmptySearchPatternFilledExactSearch() {
        String result = personService.createSearchFilter( null, "L", "A", true);
        String expected = "LOWER(LAST_NAME) = 'l' and (TO_CHAR(RECORD_ID) = 'a' or LOWER(FIRST_NAME) = 'a' or " +
                "LOWER(MIDDLE_NAME) = 'a' or LOWER(INN) = 'a' or " +
                "LOWER(INN_FOREIGN) = 'a' or LOWER(SNILS) = 'a' or LOWER(TAXPAYER_STATE_CODE) = 'a' or " +
                "LOWER(BIRTH_PLACE) = 'a' or LOWER(CITIZENSHIP_CODE) = 'a' or TO_CHAR(EMPLOYEE) = 'a' or " +
                "LOWER(SOURCE_ID_CODE) = 'a' or TO_CHAR(OLD_ID) = 'a' or LOWER(ADDRESS_ADDRESS_FULL) = 'a')";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameFilledFirstNameFilledSearchPatternFilledApproxSearch() {
        String result = personService.createSearchFilter( "F", "L", "A", false);
        String expected = "LOWER(FIRST_NAME) like '%f%' and LOWER(LAST_NAME) like '%l%' and (TO_CHAR(RECORD_ID) like '%a%' or " +
                "LOWER(MIDDLE_NAME) like '%a%' or LOWER(INN) like '%a%' or " +
                "LOWER(INN_FOREIGN) like '%a%' or LOWER(SNILS) like '%a%' or LOWER(TAXPAYER_STATE_CODE) like '%a%' or " +
                "LOWER(BIRTH_PLACE) like '%a%' or LOWER(CITIZENSHIP_CODE) like '%a%' or TO_CHAR(EMPLOYEE) like '%a%' or " +
                "LOWER(SOURCE_ID_CODE) like '%a%' or TO_CHAR(OLD_ID) like '%a%' or LOWER(ADDRESS_ADDRESS_FULL) like '%a%')";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameFilledFirstNameFilledSearchPatternFilledExactSearch() {
        String result = personService.createSearchFilter( "F", "L", "A", true);
        String expected = "LOWER(FIRST_NAME) = 'f' and LOWER(LAST_NAME) = 'l' and (TO_CHAR(RECORD_ID) = 'a' or " +
                "LOWER(MIDDLE_NAME) = 'a' or LOWER(INN) = 'a' or " +
                "LOWER(INN_FOREIGN) = 'a' or LOWER(SNILS) = 'a' or LOWER(TAXPAYER_STATE_CODE) = 'a' or " +
                "LOWER(BIRTH_PLACE) = 'a' or LOWER(CITIZENSHIP_CODE) = 'a' or TO_CHAR(EMPLOYEE) = 'a' or " +
                "LOWER(SOURCE_ID_CODE) = 'a' or TO_CHAR(OLD_ID) = 'a' or LOWER(ADDRESS_ADDRESS_FULL) = 'a')";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testGetPersons() {
        Date version = Mockito.mock(Date.class);
        personService.getPersons(null, version, null, "", "", "", false);
        Mockito.verify(refBookPersonDao, Mockito.times(1)).getPersons(version, null, "", null);
        personService.getPersons(0L, version, null, "", "", "", false);
        Mockito.verify(refBookPersonDao, Mockito.times(1)).getPersonVersions(0L, null);
    }

}

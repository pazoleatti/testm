package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.service.PersonService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("PersonServiceImplTest.xml")
public class PersonServiceImplTest {

    @Autowired
    private PersonService personService;
    @Autowired
    private RefBookPersonDao refBookPersonDao;

    private static Method createSearchFilterMethod;

    @BeforeClass
    public static void setUp() throws NoSuchMethodException {
        Class<? extends PersonService> clazz = PersonServiceImpl.class;
        createSearchFilterMethod = clazz.getDeclaredMethod("createSearchFilter", String.class, String.class, String.class, Boolean.class);
        createSearchFilterMethod.setAccessible(true);
    }

    @Test
    public void testCreateSearchFilterLastNameEmptyFirstNameEmptySearchPatternEmptyApproxSearch() throws InvocationTargetException, IllegalAccessException {
        String result = (String) createSearchFilterMethod.invoke(personService, null, null, null, false);
        Assert.assertEquals("", result);
    }

    @Test
    public void testCreateSearchFilterLastNameEmptyFirstNameEmptySearchPatternFilledApproxSearch() throws InvocationTargetException, IllegalAccessException {
        String result = (String) createSearchFilterMethod.invoke(personService, null, null, "A", false);
        String expected = "(TO_CHAR(RECORD_ID) like '%a%' or LOWER(LAST_NAME) like '%a%' or " +
                "LOWER(FIRST_NAME) like '%a%' or LOWER(MIDDLE_NAME) like '%a%' or LOWER(INN) like '%a%' or " +
                "LOWER(INN_FOREIGN) like '%a%' or LOWER(SNILS) like '%a%' or LOWER(TAXPAYER_STATE_CODE) like '%a%' or " +
                "LOWER(BIRTH_PLACE) like '%a%' or LOWER(CITIZENSHIP_CODE) like '%a%' or TO_CHAR(EMPLOYEE) like '%a%' or " +
                "LOWER(SOURCE_ID_CODE) like '%a%' or TO_CHAR(OLD_ID) like '%a%' or LOWER(ADDRESS_ADDRESS_FULL) like '%a%')";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameEmptyFirstNameEmptySearchPatternFilledExactSearch() throws InvocationTargetException, IllegalAccessException {
        String result = (String) createSearchFilterMethod.invoke(personService, null, null, "A", true);
        String expected = "(TO_CHAR(RECORD_ID) = 'a' or LOWER(LAST_NAME) = 'a' or " +
                "LOWER(FIRST_NAME) = 'a' or LOWER(MIDDLE_NAME) = 'a' or LOWER(INN) = 'a' or " +
                "LOWER(INN_FOREIGN) = 'a' or LOWER(SNILS) = 'a' or LOWER(TAXPAYER_STATE_CODE) = 'a' or " +
                "LOWER(BIRTH_PLACE) = 'a' or LOWER(CITIZENSHIP_CODE) = 'a' or TO_CHAR(EMPLOYEE) = 'a' or " +
                "LOWER(SOURCE_ID_CODE) = 'a' or TO_CHAR(OLD_ID) = 'a' or LOWER(ADDRESS_ADDRESS_FULL) = 'a')";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameEmptyFirstNameFilledSearchPatternEmptyApproxSearch() throws InvocationTargetException, IllegalAccessException {
        String result = (String) createSearchFilterMethod.invoke(personService, "F", null, null, false);
        String expected = "LOWER(FIRST_NAME) like '%f%'";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameEmptyFirstNameFilledSearchPatternEmptyExactSearch() throws InvocationTargetException, IllegalAccessException {
        String result = (String) createSearchFilterMethod.invoke(personService, "F", null, null, true);
        String expected = "LOWER(FIRST_NAME) = 'f'";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameEmptyFirstNameFilledSearchPatternFilledApproxSearch() throws InvocationTargetException, IllegalAccessException {
        String result = (String) createSearchFilterMethod.invoke(personService, "F", null, "A", false);
        String expected = "LOWER(FIRST_NAME) like '%f%' and (TO_CHAR(RECORD_ID) like '%a%' or LOWER(LAST_NAME) like '%a%' or " +
                "LOWER(MIDDLE_NAME) like '%a%' or LOWER(INN) like '%a%' or " +
                "LOWER(INN_FOREIGN) like '%a%' or LOWER(SNILS) like '%a%' or LOWER(TAXPAYER_STATE_CODE) like '%a%' or " +
                "LOWER(BIRTH_PLACE) like '%a%' or LOWER(CITIZENSHIP_CODE) like '%a%' or TO_CHAR(EMPLOYEE) like '%a%' or " +
                "LOWER(SOURCE_ID_CODE) like '%a%' or TO_CHAR(OLD_ID) like '%a%' or LOWER(ADDRESS_ADDRESS_FULL) like '%a%')";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameEmptyFirstNameFilledSearchPatternFilledExactSearch() throws InvocationTargetException, IllegalAccessException {
        String result = (String) createSearchFilterMethod.invoke(personService, "F", null, "A", true);
        String expected = "LOWER(FIRST_NAME) = 'f' and (TO_CHAR(RECORD_ID) = 'a' or LOWER(LAST_NAME) = 'a' or " +
                "LOWER(MIDDLE_NAME) = 'a' or LOWER(INN) = 'a' or " +
                "LOWER(INN_FOREIGN) = 'a' or LOWER(SNILS) = 'a' or LOWER(TAXPAYER_STATE_CODE) = 'a' or " +
                "LOWER(BIRTH_PLACE) = 'a' or LOWER(CITIZENSHIP_CODE) = 'a' or TO_CHAR(EMPLOYEE) = 'a' or " +
                "LOWER(SOURCE_ID_CODE) = 'a' or TO_CHAR(OLD_ID) = 'a' or LOWER(ADDRESS_ADDRESS_FULL) = 'a')";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameFilledFirstNameEmptySearchPatternEmptyApproxSearch() throws InvocationTargetException, IllegalAccessException {
        String result = (String) createSearchFilterMethod.invoke(personService, null, "L", null, false);
        String expected = "LOWER(LAST_NAME) like '%l%'";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameFilledFirstNameEmptySearchPatternEmptyExactSearch() throws InvocationTargetException, IllegalAccessException {
        String result = (String) createSearchFilterMethod.invoke(personService, null, "L", null, true);
        String expected = "LOWER(LAST_NAME) = 'l'";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameFilledFirstNameEmptySearchPatternFilledApproxSearch() throws InvocationTargetException, IllegalAccessException {
        String result = (String) createSearchFilterMethod.invoke(personService, null, "L", "A", false);
        String expected = "LOWER(LAST_NAME) like '%l%' and (TO_CHAR(RECORD_ID) like '%a%' or LOWER(FIRST_NAME) like '%a%' or " +
                "LOWER(MIDDLE_NAME) like '%a%' or LOWER(INN) like '%a%' or " +
                "LOWER(INN_FOREIGN) like '%a%' or LOWER(SNILS) like '%a%' or LOWER(TAXPAYER_STATE_CODE) like '%a%' or " +
                "LOWER(BIRTH_PLACE) like '%a%' or LOWER(CITIZENSHIP_CODE) like '%a%' or TO_CHAR(EMPLOYEE) like '%a%' or " +
                "LOWER(SOURCE_ID_CODE) like '%a%' or TO_CHAR(OLD_ID) like '%a%' or LOWER(ADDRESS_ADDRESS_FULL) like '%a%')";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameFilledFirstNameEmptySearchPatternFilledExactSearch() throws InvocationTargetException, IllegalAccessException {
        String result = (String) createSearchFilterMethod.invoke(personService, null, "L", "A", true);
        String expected = "LOWER(LAST_NAME) = 'l' and (TO_CHAR(RECORD_ID) = 'a' or LOWER(FIRST_NAME) = 'a' or " +
                "LOWER(MIDDLE_NAME) = 'a' or LOWER(INN) = 'a' or " +
                "LOWER(INN_FOREIGN) = 'a' or LOWER(SNILS) = 'a' or LOWER(TAXPAYER_STATE_CODE) = 'a' or " +
                "LOWER(BIRTH_PLACE) = 'a' or LOWER(CITIZENSHIP_CODE) = 'a' or TO_CHAR(EMPLOYEE) = 'a' or " +
                "LOWER(SOURCE_ID_CODE) = 'a' or TO_CHAR(OLD_ID) = 'a' or LOWER(ADDRESS_ADDRESS_FULL) = 'a')";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameFilledFirstNameFilledSearchPatternFilledApproxSearch() throws InvocationTargetException, IllegalAccessException {
        String result = (String) createSearchFilterMethod.invoke(personService, "F", "L", "A", false);
        String expected = "LOWER(FIRST_NAME) like '%f%' and LOWER(LAST_NAME) like '%l%' and (TO_CHAR(RECORD_ID) like '%a%' or " +
                "LOWER(MIDDLE_NAME) like '%a%' or LOWER(INN) like '%a%' or " +
                "LOWER(INN_FOREIGN) like '%a%' or LOWER(SNILS) like '%a%' or LOWER(TAXPAYER_STATE_CODE) like '%a%' or " +
                "LOWER(BIRTH_PLACE) like '%a%' or LOWER(CITIZENSHIP_CODE) like '%a%' or TO_CHAR(EMPLOYEE) like '%a%' or " +
                "LOWER(SOURCE_ID_CODE) like '%a%' or TO_CHAR(OLD_ID) like '%a%' or LOWER(ADDRESS_ADDRESS_FULL) like '%a%')";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCreateSearchFilterLastNameFilledFirstNameFilledSearchPatternFilledExactSearch() throws InvocationTargetException, IllegalAccessException {
        String result = (String) createSearchFilterMethod.invoke(personService, "F", "L", "A", true);
        String expected = "LOWER(FIRST_NAME) = 'f' and LOWER(LAST_NAME) = 'l' and (TO_CHAR(RECORD_ID) = 'a' or " +
                "LOWER(MIDDLE_NAME) = 'a' or LOWER(INN) = 'a' or " +
                "LOWER(INN_FOREIGN) = 'a' or LOWER(SNILS) = 'a' or LOWER(TAXPAYER_STATE_CODE) = 'a' or " +
                "LOWER(BIRTH_PLACE) = 'a' or LOWER(CITIZENSHIP_CODE) = 'a' or TO_CHAR(EMPLOYEE) = 'a' or " +
                "LOWER(SOURCE_ID_CODE) = 'a' or TO_CHAR(OLD_ID) = 'a' or LOWER(ADDRESS_ADDRESS_FULL) = 'a')";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testGetPersons() throws InvocationTargetException, IllegalAccessException {
        Date version = Mockito.mock(Date.class);
        personService.getPersons(null, version, null, "", "", "", false);
        Mockito.verify(refBookPersonDao, Mockito.times(1)).getPersons(version, null, "", null);
        personService.getPersons(0L, version, null, "", "", "", false);
        Mockito.verify(refBookPersonDao, Mockito.times(1)).getPersonVersions(0L, null);
    }

}

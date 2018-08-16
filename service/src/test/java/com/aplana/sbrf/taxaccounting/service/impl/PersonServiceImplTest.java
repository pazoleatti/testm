package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.PersonService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("PersonServiceImplTest.xml")
public class PersonServiceImplTest {

    @Autowired
    private PersonService personService;
    @Autowired
    private RefBookPersonDao refBookPersonDao;
    @Autowired
    private CommonRefBookService commonRefBookService;

    @Test
    public void test_createSearchFilter_LastNameEmptyFirstNameEmptySearchPatternEmptyApproxSearch() {
        String result = personService.createSearchFilter(null, null, null, false);
        assertEquals("", result);
    }

    @Test
    public void test_createSearchFilter_lastNameEmptyFirstNameEmptySearchPatternFilledApproxSearch() {
        String result = personService.createSearchFilter(null, null, "A", false);
        String expected = "(TO_CHAR(RECORD_ID) like '%a%' or LOWER(LAST_NAME) like '%a%' or " +
                "LOWER(FIRST_NAME) like '%a%' or LOWER(MIDDLE_NAME) like '%a%' or LOWER(INN) like '%a%' or " +
                "LOWER(INN_FOREIGN) like '%a%' or LOWER(SNILS) like '%a%' or LOWER(TAXPAYER_STATE_CODE) like '%a%' or " +
                "LOWER(BIRTH_PLACE) like '%a%' or LOWER(CITIZENSHIP_CODE) like '%a%' or TO_CHAR(EMPLOYEE) like '%a%' or " +
                "LOWER(SOURCE_ID_CODE) like '%a%' or TO_CHAR(OLD_ID) like '%a%' or LOWER(ADDRESS_ADDRESS_FULL) like '%a%')";
        assertEquals(expected, result);
    }

    @Test
    public void test_createSearchFilter_lastNameEmptyFirstNameEmptySearchPatternFilledExactSearch() {
        String result = personService.createSearchFilter(null, null, "A", true);
        String expected = "(TO_CHAR(RECORD_ID) = 'a' or LOWER(LAST_NAME) = 'a' or " +
                "LOWER(FIRST_NAME) = 'a' or LOWER(MIDDLE_NAME) = 'a' or LOWER(INN) = 'a' or " +
                "LOWER(INN_FOREIGN) = 'a' or LOWER(SNILS) = 'a' or LOWER(TAXPAYER_STATE_CODE) = 'a' or " +
                "LOWER(BIRTH_PLACE) = 'a' or LOWER(CITIZENSHIP_CODE) = 'a' or TO_CHAR(EMPLOYEE) = 'a' or " +
                "LOWER(SOURCE_ID_CODE) = 'a' or TO_CHAR(OLD_ID) = 'a' or LOWER(ADDRESS_ADDRESS_FULL) = 'a')";
        assertEquals(expected, result);
    }

    @Test
    public void test_createSearchFilter_lastNameEmptyFirstNameFilledSearchPatternEmptyApproxSearch() {
        String result = personService.createSearchFilter("F", null, null, false);
        String expected = "LOWER(FIRST_NAME) like '%f%'";
        assertEquals(expected, result);
    }

    @Test
    public void test_createSearchFilter_lastNameEmptyFirstNameFilledSearchPatternEmptyExactSearch() {
        String result = personService.createSearchFilter("F", null, null, true);
        String expected = "LOWER(FIRST_NAME) = 'f'";
        assertEquals(expected, result);
    }

    @Test
    public void test_createSearchFilter_lastNameEmptyFirstNameFilledSearchPatternFilledApproxSearch() {
        String result = personService.createSearchFilter("F", null, "A", false);
        String expected = "LOWER(FIRST_NAME) like '%f%' and (TO_CHAR(RECORD_ID) like '%a%' or LOWER(LAST_NAME) like '%a%' or " +
                "LOWER(MIDDLE_NAME) like '%a%' or LOWER(INN) like '%a%' or " +
                "LOWER(INN_FOREIGN) like '%a%' or LOWER(SNILS) like '%a%' or LOWER(TAXPAYER_STATE_CODE) like '%a%' or " +
                "LOWER(BIRTH_PLACE) like '%a%' or LOWER(CITIZENSHIP_CODE) like '%a%' or TO_CHAR(EMPLOYEE) like '%a%' or " +
                "LOWER(SOURCE_ID_CODE) like '%a%' or TO_CHAR(OLD_ID) like '%a%' or LOWER(ADDRESS_ADDRESS_FULL) like '%a%')";
        assertEquals(expected, result);
    }

    @Test
    public void test_createSearchFilter_lastNameEmptyFirstNameFilledSearchPatternFilledExactSearch() {
        String result = personService.createSearchFilter("F", null, "A", true);
        String expected = "LOWER(FIRST_NAME) = 'f' and (TO_CHAR(RECORD_ID) = 'a' or LOWER(LAST_NAME) = 'a' or " +
                "LOWER(MIDDLE_NAME) = 'a' or LOWER(INN) = 'a' or " +
                "LOWER(INN_FOREIGN) = 'a' or LOWER(SNILS) = 'a' or LOWER(TAXPAYER_STATE_CODE) = 'a' or " +
                "LOWER(BIRTH_PLACE) = 'a' or LOWER(CITIZENSHIP_CODE) = 'a' or TO_CHAR(EMPLOYEE) = 'a' or " +
                "LOWER(SOURCE_ID_CODE) = 'a' or TO_CHAR(OLD_ID) = 'a' or LOWER(ADDRESS_ADDRESS_FULL) = 'a')";
        assertEquals(expected, result);
    }

    @Test
    public void test_createSearchFilter_lastNameFilledFirstNameEmptySearchPatternEmptyApproxSearch() {
        String result = personService.createSearchFilter(null, "L", null, false);
        String expected = "LOWER(LAST_NAME) like '%l%'";
        assertEquals(expected, result);
    }

    @Test
    public void test_createSearchFilter_lastNameFilledFirstNameEmptySearchPatternEmptyExactSearch() {
        String result = personService.createSearchFilter(null, "L", null, true);
        String expected = "LOWER(LAST_NAME) = 'l'";
        assertEquals(expected, result);
    }

    @Test
    public void test_createSearchFilter_lastNameFilledFirstNameEmptySearchPatternFilledApproxSearch() {
        String result = personService.createSearchFilter(null, "L", "A", false);
        String expected = "LOWER(LAST_NAME) like '%l%' and (TO_CHAR(RECORD_ID) like '%a%' or LOWER(FIRST_NAME) like '%a%' or " +
                "LOWER(MIDDLE_NAME) like '%a%' or LOWER(INN) like '%a%' or " +
                "LOWER(INN_FOREIGN) like '%a%' or LOWER(SNILS) like '%a%' or LOWER(TAXPAYER_STATE_CODE) like '%a%' or " +
                "LOWER(BIRTH_PLACE) like '%a%' or LOWER(CITIZENSHIP_CODE) like '%a%' or TO_CHAR(EMPLOYEE) like '%a%' or " +
                "LOWER(SOURCE_ID_CODE) like '%a%' or TO_CHAR(OLD_ID) like '%a%' or LOWER(ADDRESS_ADDRESS_FULL) like '%a%')";
        assertEquals(expected, result);
    }

    @Test
    public void test_createSearchFilter_lastNameFilledFirstNameEmptySearchPatternFilledExactSearch() {
        String result = personService.createSearchFilter(null, "L", "A", true);
        String expected = "LOWER(LAST_NAME) = 'l' and (TO_CHAR(RECORD_ID) = 'a' or LOWER(FIRST_NAME) = 'a' or " +
                "LOWER(MIDDLE_NAME) = 'a' or LOWER(INN) = 'a' or " +
                "LOWER(INN_FOREIGN) = 'a' or LOWER(SNILS) = 'a' or LOWER(TAXPAYER_STATE_CODE) = 'a' or " +
                "LOWER(BIRTH_PLACE) = 'a' or LOWER(CITIZENSHIP_CODE) = 'a' or TO_CHAR(EMPLOYEE) = 'a' or " +
                "LOWER(SOURCE_ID_CODE) = 'a' or TO_CHAR(OLD_ID) = 'a' or LOWER(ADDRESS_ADDRESS_FULL) = 'a')";
        assertEquals(expected, result);
    }

    @Test
    public void test_createSearchFilter_lastNameFilledFirstNameFilledSearchPatternFilledApproxSearch() {
        String result = personService.createSearchFilter("F", "L", "A", false);
        String expected = "LOWER(FIRST_NAME) like '%f%' and LOWER(LAST_NAME) like '%l%' and (TO_CHAR(RECORD_ID) like '%a%' or " +
                "LOWER(MIDDLE_NAME) like '%a%' or LOWER(INN) like '%a%' or " +
                "LOWER(INN_FOREIGN) like '%a%' or LOWER(SNILS) like '%a%' or LOWER(TAXPAYER_STATE_CODE) like '%a%' or " +
                "LOWER(BIRTH_PLACE) like '%a%' or LOWER(CITIZENSHIP_CODE) like '%a%' or TO_CHAR(EMPLOYEE) like '%a%' or " +
                "LOWER(SOURCE_ID_CODE) like '%a%' or TO_CHAR(OLD_ID) like '%a%' or LOWER(ADDRESS_ADDRESS_FULL) like '%a%')";
        assertEquals(expected, result);
    }

    @Test
    public void test_createSearchFilter_lastNameFilledFirstNameFilledSearchPatternFilledExactSearch() {
        String result = personService.createSearchFilter("F", "L", "A", true);
        String expected = "LOWER(FIRST_NAME) = 'f' and LOWER(LAST_NAME) = 'l' and (TO_CHAR(RECORD_ID) = 'a' or " +
                "LOWER(MIDDLE_NAME) = 'a' or LOWER(INN) = 'a' or " +
                "LOWER(INN_FOREIGN) = 'a' or LOWER(SNILS) = 'a' or LOWER(TAXPAYER_STATE_CODE) = 'a' or " +
                "LOWER(BIRTH_PLACE) = 'a' or LOWER(CITIZENSHIP_CODE) = 'a' or TO_CHAR(EMPLOYEE) = 'a' or " +
                "LOWER(SOURCE_ID_CODE) = 'a' or TO_CHAR(OLD_ID) = 'a' or LOWER(ADDRESS_ADDRESS_FULL) = 'a')";
        assertEquals(expected, result);
    }

    @Test
    public void test_getPersons() {
        personService.getPersons(any(PagingParams.class));
        verify(refBookPersonDao).getPersons(any(PagingParams.class));
    }

    @Test
    public void test_fetchPersonsAsMap() {
        Date version = mock(Date.class);
        PagingResult<Map<String, RefBookValue>> records = mock(PagingResult.class);
        PagingParams pagingParams = mock(PagingParams.class);
        RefBookAttribute refBookAttribute = mock(RefBookAttribute.class);
        RefBook refBook = mock(RefBook.class);
        when(refBookPersonDao.fetchPersonsAsMap(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenReturn(records);
        when(refBookPersonDao.getRefBook()).thenReturn(refBook);
        personService.fetchPersonsAsMap(version, pagingParams, "", refBookAttribute);
        verify(refBookPersonDao).fetchPersonsAsMap(version, pagingParams, "", refBookAttribute);
        verify(commonRefBookService).dereference(refBook, records);
    }
}

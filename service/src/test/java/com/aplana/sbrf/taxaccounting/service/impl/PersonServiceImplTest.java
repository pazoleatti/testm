package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.components.RegistryPersonUpdateQueryBuilder;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.Permissive;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import com.aplana.sbrf.taxaccounting.permissions.BasePermissionEvaluator;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;


public class PersonServiceImplTest {

    @InjectMocks
    @Spy
    private PersonServiceImpl personService;
    @Mock
    private RefBookPersonDao personDao;
    @Mock
    private CommonRefBookService commonRefBookService;
    @Mock
    private BasePermissionEvaluator basePermissionEvaluator;
    @Mock
    private RegistryPersonUpdateQueryBuilder registryPersonUpdateQueryBuilder;
    @Captor
    private ArgumentCaptor<List<RegistryPerson.UpdatableField>> personFieldsToUpdate;

    private static Method resolvePersonMethod;

    @BeforeClass
    public static void initClass() throws NoSuchMethodException {
        Class<? extends PersonServiceImpl> clazz = PersonServiceImpl.class;
        resolvePersonMethod = clazz.getDeclaredMethod("resolveVersion", List.class, Date.class);
        resolvePersonMethod.setAccessible(true);
    }

    @Before
    public void setuUp() {
        MockitoAnnotations.initMocks(this);
    }

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
    // TODO: Написать полноценные тесты с учётом прав TAUser на Vip'ов
    public void test_getPersons_forControlUnp() {
        PagingParams params = new PagingParams();
        RefBookPersonFilter filter = new RefBookPersonFilter();
        TAUser user = mock(TAUser.class);
        when(user.hasRole(TARole.N_ROLE_CONTROL_UNP)).thenReturn(true);

        personService.getPersons(params, filter, user);
        verify(personDao).getPersons(params, filter);
    }

    @Test
    public void test_getPersons_forOtherTb() {
        PagingParams params = new PagingParams();
        RefBookPersonFilter filter = new RefBookPersonFilter();
        TAUser user = mock(TAUser.class);
        when(user.hasRole(TARole.N_ROLE_CONTROL_UNP)).thenReturn(true);

        personService.getPersons(params, filter, user);
        verify(personDao).getPersons(params, filter);
    }

    @Test
    public void test_fetchPersonsAsMap() {
        Date version = mock(Date.class);
        PagingResult<Map<String, RefBookValue>> records = mock(PagingResult.class);
        PagingParams pagingParams = mock(PagingParams.class);
        RefBookAttribute refBookAttribute = mock(RefBookAttribute.class);
        RefBook refBook = mock(RefBook.class);
        when(personDao.fetchPersonsAsMap(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenReturn(records);
        when(personDao.getRefBook()).thenReturn(refBook);
        personService.fetchPersonsAsMap(version, pagingParams, "", refBookAttribute);
        verify(personDao).fetchPersonsAsMap(version, pagingParams, "", refBookAttribute);
        verify(commonRefBookService).dereference(refBook, records);
    }

    @Test
    public void test_fetchDuplicates() {
        Long id = 1L;
        Date actualDate = mock(Date.class);
        PagingParams pagingParams = mock(PagingParams.class);

        personService.fetchDuplicates(id, actualDate, pagingParams);

        verify(personDao).fetchDuplicates(id, pagingParams);
    }

    @Test
    public void test_fetchOriginal() {
        Long id = 1L;
        Date actualDate = mock(Date.class);

        personService.fetchOriginal(id, actualDate);

        verify(personDao).fetchOriginal(id);
    }

    @Test(expected = NullPointerException.class)
    public void test_fetchPerson() {
        Long id = 1L;

        personService.fetchPerson(id);

        verify(personDao).fetchOriginal(id);
    }

    @Test
    public void test_resolvePersonMethod_whenActualVersionPresentAndBoundedActualVersion() throws InvocationTargetException, IllegalAccessException {
        RegistryPerson registryPerson1 = mock(RegistryPerson.class);
        RegistryPerson registryPerson2 = mock(RegistryPerson.class);
        RegistryPerson registryPerson3 = mock(RegistryPerson.class);
        RegistryPerson registryPerson4 = mock(RegistryPerson.class);
        when(registryPerson1.getVersion()).thenReturn(new Date(10L));
        when(registryPerson2.getVersion()).thenReturn(new Date(20L));
        when(registryPerson3.getVersion()).thenReturn(new Date(30L));
        when(registryPerson4.getVersion()).thenReturn(new Date(40L));
        when(registryPerson1.getState()).thenReturn(0);
        when(registryPerson2.getState()).thenReturn(0);
        when(registryPerson3.getState()).thenReturn(0);
        when(registryPerson4.getState()).thenReturn(2);
        List<RegistryPerson> persons = new ArrayList<>();
        persons.add(registryPerson1);
        persons.add(registryPerson2);
        persons.add(registryPerson3);
        persons.add(registryPerson4);
        Collections.sort(persons, new VersionComparator());
        RegistryPerson result = (RegistryPerson) resolvePersonMethod.invoke(personService, persons, new Date(25L));

        assertThat(new Date(20L), is(equalTo(result.getVersion())));
    }

    @Test
    public void test_resolvePersonMethod_whenActualVersionPresentAndBoundedDummyVersion() throws InvocationTargetException, IllegalAccessException {
        RegistryPerson registryPerson1 = mock(RegistryPerson.class);
        RegistryPerson registryPerson2 = mock(RegistryPerson.class);
        RegistryPerson registryPerson3 = mock(RegistryPerson.class);

        when(registryPerson1.getVersion()).thenReturn(new Date(10L));
        when(registryPerson2.getVersion()).thenReturn(new Date(20L));
        when(registryPerson3.getVersion()).thenReturn(new Date(30L));

        when(registryPerson1.getState()).thenReturn(0);
        when(registryPerson2.getState()).thenReturn(0);
        when(registryPerson3.getState()).thenReturn(2);

        List<RegistryPerson> persons = new ArrayList<>();
        persons.add(registryPerson1);
        persons.add(registryPerson2);
        persons.add(registryPerson3);

        Collections.sort(persons, new VersionComparator());
        RegistryPerson result = (RegistryPerson) resolvePersonMethod.invoke(personService, persons, new Date(25L));

        assertThat(new Date(20L), is(equalTo(result.getVersion())));
    }

    @Test
    public void test_resolvePersonMethod_whenAllVersionsAfterActual() throws InvocationTargetException, IllegalAccessException {
        RegistryPerson registryPerson1 = mock(RegistryPerson.class);
        RegistryPerson registryPerson2 = mock(RegistryPerson.class);
        RegistryPerson registryPerson3 = mock(RegistryPerson.class);

        when(registryPerson1.getVersion()).thenReturn(new Date(30L));
        when(registryPerson2.getVersion()).thenReturn(new Date(40L));
        when(registryPerson3.getVersion()).thenReturn(new Date(50L));

        when(registryPerson1.getState()).thenReturn(0);
        when(registryPerson2.getState()).thenReturn(0);
        when(registryPerson3.getState()).thenReturn(2);

        List<RegistryPerson> persons = new ArrayList<>();
        persons.add(registryPerson1);
        persons.add(registryPerson2);
        persons.add(registryPerson3);

        Collections.sort(persons, new VersionComparator());
        RegistryPerson result = (RegistryPerson) resolvePersonMethod.invoke(personService, persons, new Date(25L));

        assertThat(new Date(30L), is(equalTo(result.getVersion())));
    }

    @Test
    public void test_resolvePersonMethod_whenAllVersionsBeforeActual() throws InvocationTargetException, IllegalAccessException {
        RegistryPerson registryPerson1 = mock(RegistryPerson.class);
        RegistryPerson registryPerson2 = mock(RegistryPerson.class);
        RegistryPerson registryPerson3 = mock(RegistryPerson.class);

        when(registryPerson1.getVersion()).thenReturn(new Date(5L));
        when(registryPerson2.getVersion()).thenReturn(new Date(10L));
        when(registryPerson3.getVersion()).thenReturn(new Date(20L));

        when(registryPerson1.getState()).thenReturn(0);
        when(registryPerson2.getState()).thenReturn(0);
        when(registryPerson3.getState()).thenReturn(2);

        List<RegistryPerson> persons = new ArrayList<>();
        persons.add(registryPerson1);
        persons.add(registryPerson2);
        persons.add(registryPerson3);

        Collections.sort(persons, new VersionComparator());
        RegistryPerson result = (RegistryPerson) resolvePersonMethod.invoke(personService, persons, new Date(25L));

        assertThat(new Date(10L), is(equalTo(result.getVersion())));
    }


    @Test
    public void test_updateRegistryPerson_whenVipPersonAndAccessGranted() {
        //setup
        RegistryPerson person = mock(RegistryPerson.class);
        final RegistryPerson persistedPerson = mock(RegistryPerson.class);
        doReturn(persistedPerson).when(personService).fetchPerson(anyLong());
        when(basePermissionEvaluator.hasPermission(any(Authentication.class), any(), any())).thenReturn(true);
        when(person.getLastName()).thenReturn("ИзмФам");
        when(person.getInn()).thenReturn(Permissive.of("213456"));
        when(persistedPerson.getLastName()).thenReturn("ОригФам");
        when(persistedPerson.getInn()).thenReturn(Permissive.of("654321"));

        //execution
        personService.updateRegistryPerson(person);
        //verification
        verify(registryPersonUpdateQueryBuilder).buildPersonUpdateQuery(personFieldsToUpdate.capture());
        Assertions.assertThat(personFieldsToUpdate.getValue().size()).isEqualTo(2);
        Assertions.assertThat(personFieldsToUpdate.getValue()).contains(RegistryPerson.UpdatableField.LAST_NAME);
        Assertions.assertThat(personFieldsToUpdate.getValue()).contains(RegistryPerson.UpdatableField.INN);
    }

    @Test
    public void test_updateRegistryPerson_whenVipPersonAndAccessDisabled() {
        //setup
        RegistryPerson person = mock(RegistryPerson.class);
        final RegistryPerson persistedPerson = mock(RegistryPerson.class);
        doReturn(persistedPerson).when(personService).fetchPerson(anyLong());
        when(basePermissionEvaluator.hasPermission(any(Authentication.class), any(), any())).thenReturn(false);
        when(person.getLastName()).thenReturn("ИзмФам");
        when(person.getInn()).thenReturn(Permissive.of("213456"));
        when(persistedPerson.getLastName()).thenReturn("ОригФам");
        when(persistedPerson.getInn()).thenReturn(Permissive.of("654321"));

        //execution
        personService.updateRegistryPerson(person);
        //verification
        verify(registryPersonUpdateQueryBuilder).buildPersonUpdateQuery(personFieldsToUpdate.capture());
        Assertions.assertThat(personFieldsToUpdate.getValue().size()).isEqualTo(1);
        Assertions.assertThat(personFieldsToUpdate.getValue()).contains(RegistryPerson.UpdatableField.LAST_NAME);
    }

    private static class VersionComparator implements Comparator<RegistryPerson> {
        @Override
        public int compare(RegistryPerson o1, RegistryPerson o2) {
            return o2.getVersion().compareTo(o1.getVersion());
        }
    }
}

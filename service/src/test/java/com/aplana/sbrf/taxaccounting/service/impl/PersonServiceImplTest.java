package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.IdDocDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.impl.IdTaxPayerDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.impl.PersonTbDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.dao.util.DBUtils;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.IdDoc;
import com.aplana.sbrf.taxaccounting.model.refbook.PersonIdentifier;
import com.aplana.sbrf.taxaccounting.model.refbook.PersonTb;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import com.aplana.sbrf.taxaccounting.model.result.CheckDulResult;
import com.aplana.sbrf.taxaccounting.permissions.BasePermissionEvaluator;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
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
    private IdDocDaoImpl idDocDaoImpl;
    @Mock
    private IdTaxPayerDaoImpl idTaxPayerDaoImpl;
    @Mock
    private PersonTbDaoImpl personTbDaoImpl;
    @Mock
    private DBUtils dbUtils;
    @Captor
    ArgumentCaptor<List<IdDoc>> idDocs;
    @Captor
    ArgumentCaptor<List<PersonIdentifier>> idTaxPayers;
    @Captor
    ArgumentCaptor<List<PersonTb>> personTbs;

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
    // TODO: Написать полноценные тесты с учётом прав TAUser на Vip'ов
    public void test_getPersons_forControlUnp() {
        PagingParams params = new PagingParams();
        RefBookPersonFilter filter = new RefBookPersonFilter();
        TAUser user = mock(TAUser.class);
        when(user.hasRole(TARole.N_ROLE_CONTROL_UNP)).thenReturn(true);
        when(personDao.getPersons(any(PagingParams.class), any(RefBookPersonFilter.class))).thenReturn(new PagingResult<RegistryPerson>());
        personService.getPersonsData(params, filter);
        verify(personDao).getPersons(params, filter);
    }

    @Test(expected = NullPointerException.class)
    public void test_fetchPerson() {
        Long id = 1L;

        personService.fetchPersonData(id);

        verify(personDao).fetchOriginal(id);
    }

    @Test
    public void test_resolvePersonMethod_whenActualVersionPresentAndBoundedDummyVersion() throws InvocationTargetException, IllegalAccessException {
        RegistryPerson registryPerson1 = mock(RegistryPerson.class);
        RegistryPerson registryPerson2 = mock(RegistryPerson.class);

        when(registryPerson1.getStartDate()).thenReturn(new Date(10L));
        when(registryPerson2.getStartDate()).thenReturn(new Date(20L));

        List<RegistryPerson> persons = new ArrayList<>();
        persons.add(registryPerson1);
        persons.add(registryPerson2);

        Collections.sort(persons, new VersionComparator());
        RegistryPerson result = (RegistryPerson) resolvePersonMethod.invoke(personService, persons, new Date(25L));

        assertThat(result.getStartDate().getTime(), is(equalTo(20L)));
    }

    @Test
    public void test_resolvePersonMethod_whenAllVersionsBeforeActual() throws InvocationTargetException, IllegalAccessException {
        RegistryPerson registryPerson1 = mock(RegistryPerson.class);
        RegistryPerson registryPerson2 = mock(RegistryPerson.class);
        RegistryPerson registryPerson3 = mock(RegistryPerson.class);

        when(registryPerson1.getStartDate()).thenReturn(new Date(5L));
        when(registryPerson2.getStartDate()).thenReturn(new Date(10L));

        List<RegistryPerson> persons = new ArrayList<>();
        persons.add(registryPerson1);
        persons.add(registryPerson2);

        Collections.sort(persons, new VersionComparator());
        RegistryPerson result = (RegistryPerson) resolvePersonMethod.invoke(personService, persons, new Date(25L));

        assertThat(result.getStartDate().getTime(), is(equalTo(10L)));
    }

    @Test
    public void test_savePersons() {
        // setUp
        List<RegistryPerson> personList = new ArrayList<>();

        RegistryPerson person1 = mock(RegistryPerson.class);
        RegistryPerson person2 = mock(RegistryPerson.class);

        List<IdDoc> idDocs1 = new ArrayList<>();
        List<PersonIdentifier> idTaxPayers1 = new ArrayList<>();
        List<PersonTb> personTbs1 = new ArrayList<>();
        List<IdDoc> idDocs2 = new ArrayList<>();
        List<PersonIdentifier> idTaxPayers2 = new ArrayList<>();
        List<PersonTb> personTbs2 = new ArrayList<>();

        when(person1.getDocuments()).thenReturn(idDocs1);
        when(person1.getPersonIdentityList()).thenReturn(idTaxPayers1);
        when(person1.getPersonTbList()).thenReturn(personTbs1);
        when(person2.getDocuments()).thenReturn(idDocs2);
        when(person2.getPersonIdentityList()).thenReturn(idTaxPayers2);
        when(person2.getPersonTbList()).thenReturn(personTbs2);

        List<Long> personIds = Collections.singletonList(1L);
        when(dbUtils.getNextRefBookRecordIds(anyInt())).thenReturn(personIds);
        when(dbUtils.getNextIds(any(DBUtils.Sequence.class), anyInt())).thenReturn(personIds);

        personList.addAll(Arrays.asList(person1, person2));

        // execute
        personService.savePersons(personList);

        //verify
        verify(personDao).saveBatch(personList);
        verify(idDocDaoImpl).saveBatch(idDocs.capture());
        verify(idTaxPayerDaoImpl).saveBatch(idTaxPayers.capture());
        verify(personTbDaoImpl).saveBatch(personTbs.capture());
        verify(personDao).updateBatch(personList);

    }

    @Test
    public void test_checkDul_codeEquals91() {
        String docCode = "91";
        String docNumber = "123";
        CheckDulResult result = personService.checkDul(docCode, docNumber);
        assertThat(result.getErrorMessage()).isNullOrEmpty();
        assertThat(result.getFormattedNumber()).isEqualTo(docNumber);
    }

    @Test
    public void test_checkDul_codeNotEquals91() {
        String docCode = "03";
        String docNumber = "123";

        CheckDulResult result = personService.checkDul(docCode, docNumber);
        assertThat(result.getErrorMessage()).isNullOrEmpty();
        assertThat(result.getFormattedNumber()).isEqualTo(docNumber);
    }


    private static class VersionComparator implements Comparator<RegistryPerson> {
        @Override
        public int compare(RegistryPerson o1, RegistryPerson o2) {
            return o2.getStartDate().compareTo(o1.getStartDate());
        }
    }
}

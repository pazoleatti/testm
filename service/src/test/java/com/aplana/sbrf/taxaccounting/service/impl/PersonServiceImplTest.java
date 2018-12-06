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
import com.aplana.sbrf.taxaccounting.model.refbook.*;
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

    private static Method convertPersonToDTO;

    @BeforeClass
    public static void initClass() throws NoSuchMethodException {
        Class<? extends PersonServiceImpl> clazz = PersonServiceImpl.class;
        resolvePersonMethod = clazz.getDeclaredMethod("resolveVersion", List.class, Date.class);
        resolvePersonMethod.setAccessible(true);
        convertPersonToDTO = clazz.getDeclaredMethod("convertPersonToDTO", Collection.class, int.class);
        convertPersonToDTO.setAccessible(true);
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

    @Test
    public void test_convertPersonToDTO() throws InvocationTargetException, IllegalAccessException {
        RegistryPerson registryPerson = mock(RegistryPerson.class);
        when(registryPerson.getRecordId()).thenReturn(1L);
        when(registryPerson.getId()).thenReturn(2L);

        Calendar startDate = new GregorianCalendar();
        startDate.set(2018, Calendar.JANUARY, 1);
        when(registryPerson.getStartDate()).thenReturn(startDate.getTime());

        when(registryPerson.getLastName()).thenReturn("Фамилия");
        when(registryPerson.getFirstName()).thenReturn("Имя");
        when(registryPerson.getMiddleName()).thenReturn("Отчество");
        when(registryPerson.getInn()).thenReturn("1234567890");
        when(registryPerson.getInnForeign()).thenReturn("2345678901");
        when(registryPerson.getSnils()).thenReturn("3456789012");

        RefBookTaxpayerState taxPayerState = mock(RefBookTaxpayerState.class);
        when(taxPayerState.getName()).thenReturn("Название кода статуса");
        when(taxPayerState.getCode()).thenReturn("Код статуса");
        when(registryPerson.getTaxPayerState()).thenReturn(taxPayerState);

        RefBookCountry citizenship = mock(RefBookCountry.class);
        when(citizenship.getName()).thenReturn("Название страны гражданства");
        when(citizenship.getCode()).thenReturn("Код страны гражданства");
        when(registryPerson.getCitizenship()).thenReturn(citizenship);

        Calendar birthDate = new GregorianCalendar();
        birthDate.set(1990, Calendar.JULY, 1);
        when(registryPerson.getBirthDate()).thenReturn(birthDate.getTime());

        when(registryPerson.getBirthPlace()).thenReturn("Место рождения");

        PersonIdentifier personIdentifier = mock(PersonIdentifier.class);
        RefBookAsnu asnu = mock(RefBookAsnu.class);
        when(asnu.getCode()).thenReturn("Код АСНУ");
        when(asnu.getName()).thenReturn("Название АСНУ");
        when(asnu.getPriority()).thenReturn(10);
        when(asnu.getType()).thenReturn("Тип АСНУ");
        when(personIdentifier.getInp()).thenReturn("4567890123");
        when(personIdentifier.getAsnu()).thenReturn(asnu);
        when(registryPerson.getPersonIdentityList()).thenReturn(Collections.singletonList(personIdentifier));

        IdDoc idDoc = mock(IdDoc.class);
        RefBookDocType refBookDocType = mock(RefBookDocType.class);
        when(refBookDocType.getName()).thenReturn("Название вида ДУЛ");
        when(refBookDocType.getCode()).thenReturn("Код вида ДУЛ");
        when(refBookDocType.getPriority()).thenReturn(1);
        when(idDoc.getDocType()).thenReturn(refBookDocType);
        when(idDoc.getDocumentNumber()).thenReturn("123456");
        when(registryPerson.getReportDoc()).thenReturn(idDoc);
        when(registryPerson.getDocuments()).thenReturn(Collections.singletonList(idDoc));

        Address address = mock(Address.class);
        when(address.getAddressIno()).thenReturn("Иностранный адрес");
        when(address.getAppartment()).thenReturn("3");
        when(address.getBuild()).thenReturn("А");
        when(address.getCity()).thenReturn("Энск");

        RefBookCountry addressCountry = mock(RefBookCountry.class);
        when(addressCountry.getName()).thenReturn("Название страны адреса");
        when(addressCountry.getCode()).thenReturn("Код страны адреса");
        when(address.getCountry()).thenReturn(addressCountry);

        when(address.getDistrict()).thenReturn("Название района");
        when(address.getHouse()).thenReturn("4");
        when(address.getLocality()).thenReturn("Название населенного пункта");
        when(address.getPostalCode()).thenReturn("012345");
        when(address.getRegionCode()).thenReturn("00");
        when(address.getStreet()).thenReturn("Название улицы");
        when(registryPerson.getAddress()).thenReturn(address);

        when(registryPerson.getOldId()).thenReturn(1L);
        when(registryPerson.getSource()).thenReturn(asnu);
        when(registryPerson.isVip()).thenReturn(false);

        PagingResult<RegistryPersonDTO> result = (PagingResult<RegistryPersonDTO>) convertPersonToDTO.invoke(personService, Collections.singletonList(registryPerson), 1);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAddress().value().getAddressIno()).isEqualTo("Иностранный адрес");
        assertThat(result.get(0).getAddress().value().getAppartment()).isEqualTo("3");
        assertThat(result.get(0).getAddress().value().getBuild()).isEqualTo("А");
        assertThat(result.get(0).getAddress().value().getCity()).isEqualTo("Энск");
        assertThat(result.get(0).getAddress().value().getCountry().getCode()).isEqualTo("Код страны адреса");
        assertThat(result.get(0).getAddress().value().getCountry().getName()).isEqualTo("Название страны адреса");
        assertThat(result.get(0).getAddress().value().getDistrict()).isEqualTo("Название района");
        assertThat(result.get(0).getAddress().value().getHouse()).isEqualTo("4");
        assertThat(result.get(0).getAddress().value().getLocality()).isEqualTo("Название населенного пункта");
        assertThat(result.get(0).getAddress().value().getPostalCode()).isEqualTo("012345");
        assertThat(result.get(0).getAddress().value().getRegionCode()).isEqualTo("00");
        assertThat(result.get(0).getAddress().value().getStreet()).isEqualTo("Название улицы");
        assertThat(result.get(0).getBirthDate()).isEqualTo(birthDate.getTime());
        assertThat(result.get(0).getBirthPlace()).isEqualTo("Место рождения");
        assertThat(result.get(0).getCitizenship().value().getName()).isEqualTo("Название страны гражданства");
        assertThat(result.get(0).getCitizenship().value().getCode()).isEqualTo("Код страны гражданства");
        assertThat(result.get(0).getDocuments().value().get(0).getDocType().getName()).isEqualTo("Название вида ДУЛ");
        assertThat(result.get(0).getDocuments().value().get(0).getDocType().getCode()).isEqualTo("Код вида ДУЛ");
        assertThat(result.get(0).getDocuments().value().get(0).getDocType().getPriority()).isEqualTo(1);
        assertThat(result.get(0).getDocuments().value().get(0).getDocumentNumber()).isEqualTo("123456");
        assertThat(result.get(0).getFirstName()).isEqualTo("Имя");
        assertThat(result.get(0).getLastName()).isEqualTo("Фамилия");
        assertThat(result.get(0).getMiddleName()).isEqualTo("Отчество");
        assertThat(result.get(0).getOldId()).isEqualTo(1L);
        assertThat(result.get(0).getPersonIdentityList().get(0).getAsnu()).isEqualTo(asnu);
        assertThat(result.get(0).getPersonIdentityList().get(0).getInp()).isEqualTo("4567890123");
        assertThat(result.get(0).getReportDoc().value().getDocType()).isEqualTo(refBookDocType);
        assertThat(result.get(0).getReportDoc().value().getDocumentNumber()).isEqualTo("123456");
        assertThat(result.get(0).getDocuments().value().get(0).getDocumentNumber()).isEqualTo("123456");
        assertThat(result.get(0).getDocuments().value().get(0).getDocType()).isEqualTo(refBookDocType);
        assertThat(result.get(0).getSnils().value()).isEqualTo("3456789012");
        assertThat(result.get(0).getInn().value()).isEqualTo("1234567890");
        assertThat(result.get(0).getInnForeign().value()).isEqualTo("2345678901");
        assertThat(result.get(0).getSource()).isEqualTo(asnu);
        assertThat(result.get(0).getStartDate()).isEqualTo(startDate.getTime());
        assertThat(result.get(0).getTaxPayerState().value().getName()).isEqualTo("Название кода статуса");
        assertThat(result.get(0).getTaxPayerState().value().getCode()).isEqualTo("Код статуса");
        assertThat(result.get(0).isVip()).isFalse();
    }


    private static class VersionComparator implements Comparator<RegistryPerson> {
        @Override
        public int compare(RegistryPerson o1, RegistryPerson o2) {
            return o2.getStartDate().compareTo(o1.getStartDate());
        }
    }
}

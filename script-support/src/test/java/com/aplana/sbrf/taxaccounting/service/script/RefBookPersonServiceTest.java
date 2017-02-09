package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.PersonData;
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

    @Before
    public void init() {

        RefBookPersonDao refBookPersonDao = mock(RefBookPersonDao.class);

        List<PersonData> personDataList = new ArrayList<PersonData>();

        personDataList.add(createPersonData(1L, "999", "1", "", "23", "", "1111", "Иванов", "Иван", "Иванович", null));
        personDataList.add(createPersonData(2L, "888", "2", "", "33", null, "2222", "Сидорова", "Наталья", "Викторовна", "12.10.1954"));
        personDataList.add(createPersonData(3L, "777", "", "5", "45", "", "1111", "Кулькина", "Василина", null, ""));
        personDataList.add(createPersonData(4L, "888", "2", "", "23", null, "1111", "Иванов", "Иван", "Иванович", "12.10.1954"));
        personDataList.add(createPersonData(5L, "888", "2", "", "23", "", "1111", "Иванов", "Ивон", "Ивановиеч", "12.10.1954"));
        personDataList.add(createPersonData(6L, "888", "2", "", "23", "", "1111", "Иванов", "Иван", "Ивановиеч", "12.10.1954"));

        when(refBookPersonDao.findPersonByPersonData(any(PersonData.class))).thenReturn(personDataList);

        ReflectionTestUtils.setField(personService, "refBookPersonDao", refBookPersonDao);

    }

    @Test
    public void identificatePersonTest() {
        PersonData person = createPersonData("888", "2", "", "23", "", "1111", "Иванов", "Иван", "Ивановиеч", "12.10.1954");
        Long result = personService.identificatePerson(person, 900);
        assertEquals(Long.valueOf(6L), result);

    }


    public PersonData createPersonData(Long id, String inp, String inn, String innForeign, String snils, String docType,
                                       String docNumber,
                                       String lastName,
                                       String firstName, String middleName, String birthDate) {
        PersonData result = new PersonData();
        result.setId(id);
        result.setRecordId(id);
        result.setInp(inp);
        result.setInn(inn);
        result.setInnForeign(innForeign);
        result.setSnils(snils);
        result.setDocumentType(docType);
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

    public List<BaseWeigthCalculator> getComporatorList() {

        List<BaseWeigthCalculator> result = new ArrayList<BaseWeigthCalculator>();

        //Фамилия
        result.add(new BaseWeigthCalculator<PersonData>(5) {
            @Override
            public double calc(PersonData a, PersonData b) {
                return compareString(a.getLastName(), b.getLastName());
            }
        });

        //Имя
        result.add(new BaseWeigthCalculator<PersonData>(5) {
            @Override
            public double calc(PersonData a, PersonData b) {
                return compareString(a.getFirstName(), b.getFirstName());
            }
        });

        //Отчество
        result.add(new BaseWeigthCalculator<PersonData>(0.5) {
            @Override
            public double calc(PersonData a, PersonData b) {
                return compareString(a.getMiddleName(), b.getMiddleName());
            }
        });

        //Пол
        /*result.add(new BaseWeigthComporator<PersonData>(1) {
            @Override
            public double calc(PersonData a, PersonData b) {
                return compareNumber(a.getSex(), b.getSex());
            }
        });*/

        //Дата рождения
        result.add(new BaseWeigthCalculator<PersonData>(5) {
            @Override
            public double calc(PersonData a, PersonData b) {
                return compareDate(a.getBirthDate(), b.getBirthDate());
            }
        });

        //Гражданство
        /*result.add(new BaseWeigthComporator<PersonData>(1) {
            @Override
            public double calc(PersonData a, PersonData b) {
                return compareString(a.getCitizenship(), b.getCitizenship());
            }
        });*/

        //Идентификатор физлица номер и код АСНУ
       /* result.add(new BaseWeigthComporator<PersonData>(10) {
            @Override
            public double calc(PersonData a, PersonData b) {
                boolean result = equalsNullSafe(prepareStr(a.getInp()), prepareStr(b.getInp()));
                if (!result) {
                    return 0D;
                }
                return compareString(a.getAsnu(), b.getAsnu());
            }
        });*/

        //ИНН в РФ
        result.add(new BaseWeigthCalculator<PersonData>(10) {
            @Override
            public double calc(PersonData a, PersonData b) {
                return compareString(a.getInn(), b.getInn());
            }
        });

        //ИНН в стране гражданства
        result.add(new BaseWeigthCalculator<PersonData>(10) {
            @Override
            public double calc(PersonData a, PersonData b) {
                return compareString(a.getInnForeign(), b.getInnForeign());
            }
        });

        //СНИЛС
        result.add(new BaseWeigthCalculator<PersonData>(15) {
            @Override
            public double calc(PersonData a, PersonData b) {
                return compareString(a.getInnForeign(), b.getInnForeign());
            }

            @Override
            protected String prepareStr(String string) {
                if (string != null) {
                    return super.prepareStr(string).replaceAll("[-]", "");
                } else {
                    return null;
                }
            }
        });

        //Статус налогоплатильщика
//        result.add(new BaseWeigthComporator<PersonData>(1) {
//            @Override
//            public double calc(PersonData a, PersonData b) {
//                return compareString(a.getStatus(), b.getStatus());
//            }
//        });

        //Документ вид документа и код
        result.add(new BaseWeigthCalculator<PersonData>(10) {
            @Override
            public double calc(PersonData a, PersonData b) {
                boolean result = equalsNullSafe(prepareStr(a.getDocumentType()), prepareStr(b.getDocumentType()));
                if (!result) {
                    return 0D;
                }
                return compareString(a.getDocumentNumber(), b.getDocumentNumber());
            }
        });

        //weithMap.add("address", 1); //Адрес в РФ Одновременно поля <Адрес РФ> равны соответствующим полям  ЗСФЛ."Адрес места жительства в Российской Федерации"
        //weithMap.add("address_foreign", 1); //Адрес в стране регистрации, Одновременно поля <Адрес Ино> равны соответствующим полям  ЗСФЛ."Адрес за пределами Российской Федерации "
        //weithMap.add("ДУЛ", 1); //<Признак ОПС> = ЗСФЛ."ИНН в Российской Федерации"
        //weithMap.add("ДУЛ", 1); //<Признак ОПС> = ЗСФЛ."ИНН в Российской Федерации"
        //weithMap.add("ДУЛ", 1); //<Признак ОПС> = ЗСФЛ."ИНН в Российской Федерации"
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

package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.PersonData;
import com.aplana.sbrf.taxaccounting.model.util.BaseWeigthCalculator;
import com.aplana.sbrf.taxaccounting.model.util.WeigthCalculator;
import com.aplana.sbrf.taxaccounting.service.script.RefBookPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Andrey Drunk
 */
@Service("refBookPersonService")
public class RefBookPersonServiceImpl implements RefBookPersonService {

    @Autowired
    private RefBookPersonDao refBookPersonDao;

    @Override
    public Long identificatePerson(PersonData personData) {
        return identificatePerson(personData, 900);
    }

    public Long identificatePerson(PersonData personData, int tresholdValue) {
        return identificatePerson(personData, tresholdValue, new PersonDataWeigthCalculator(getComporatorList()));
    }


    public Long identificatePerson(PersonData personData, int tresholdValue, WeigthCalculator<PersonData>
            weigthComporators) {

        double treshold = tresholdValue / 1000D;

        List<PersonData> personDataList = refBookPersonDao.findPersonByPersonData(personData);

        if (personDataList != null && !personDataList.isEmpty()) {

            calculateWeigth(personData, personDataList, weigthComporators);

            PersonData identificatedPerson = Collections.max(personDataList, new PersonDataComparator());

            if (identificatedPerson.getWeigth() > treshold) {
                return identificatedPerson.getRecordId();
            } else {
                return refBookPersonDao.createPerson(personData);
            }

        } else {
            return refBookPersonDao.createPerson(personData);
        }

    }

    private static void calculateWeigth(PersonData searchPersonData, List<PersonData> personDataList, WeigthCalculator<PersonData> weigthComporators) {

        for (PersonData personData : personDataList) {
            double weigth = weigthComporators.calc(searchPersonData, personData);
            personData.setWeigth(weigth);
        }

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
        result.add(new BaseWeigthCalculator<PersonData>(1) {
            @Override
            public double calc(PersonData a, PersonData b) {
                return compareNumber(a.getSex(), b.getSex());
            }
        });

        //Дата рождения
        result.add(new BaseWeigthCalculator<PersonData>(5) {
            @Override
            public double calc(PersonData a, PersonData b) {
                return compareDate(a.getBirthDate(), b.getBirthDate());
            }
        });

        //Гражданство
        result.add(new BaseWeigthCalculator<PersonData>(1) {
            @Override
            public double calc(PersonData a, PersonData b) {
                return compareString(a.getCitizenship(), b.getCitizenship());
            }
        });

        //Идентификатор физлица номер и код АСНУ
        result.add(new BaseWeigthCalculator<PersonData>(10) {
            @Override
            public double calc(PersonData a, PersonData b) {
                boolean result = equalsNullSafe(prepareStr(a.getInp()), prepareStr(b.getInp()));
                if (!result) {
                    return 0D;
                }
                return compareString(a.getAsnu(), b.getAsnu());
            }
        });

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

    private class PersonDataComparator implements Comparator<PersonData> {
        @Override
        public int compare(PersonData a, PersonData b) {
            return Double.compare(a.getWeigth(), b.getWeigth());
        }
    }


    public class PersonDataWeigthCalculator implements WeigthCalculator<PersonData> {

        private List<BaseWeigthCalculator> compareList;

        public PersonDataWeigthCalculator(List<BaseWeigthCalculator> compareList) {
            this.compareList = compareList;
        }

        @Override
        public double calc(PersonData a, PersonData b) {
            double summWeigth = 0D;
            double summParameterWeigt = 0D;
            for (BaseWeigthCalculator calculator : compareList) {
                summWeigth += calculator.calc(a, b);
                summParameterWeigt += calculator.getWeigth();
            }
            return summWeigth / summParameterWeigt;
        }
    }


  /*  public Map<String, Long> getAsnuCodeIds() {
        RefBookDataProvider dataProvider = refBookFactory.getDataProvider(RefBook.Id.ASNU.getId());
        PagingResult<Map<String, RefBookValue>> refBookRecords = refBookDao.getRecords(RefBook.Id.ASNU.getId(), RefBook.Table.ASNU.getTable(), null, null, null, null);
        Map<String, Long> result = new HashMap<String, Long>();
        for (Map<String, RefBookValue> refbookRecord : refBookRecords) {
            String code = refbookRecord.get("CODE").getStringValue();
            long id = refbookRecord.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
            result.put(code, id);
        }
        return result;
    }


    public Map<String, Long> getCountryCodeIds() {
        PagingResult<Map<String, RefBookValue>> refBookRecords = refBookDao.getRecords(RefBook.Id.COUNTRY.getId(), new Date(), null, null, null);
        Map<String, Long> result = new HashMap<String, Long>();
        for (Map<String, RefBookValue> refbookRecord : refBookRecords) {
            String code = refbookRecord.get("CODE").getStringValue();
            long id = refbookRecord.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
            result.put(code, id);
        }
        return result;
    }

    public Map<String, Long> getTaxpayerStatusCodeIds() {
        PagingResult<Map<String, RefBookValue>> refBookRecords = refBookDao.getRecords(RefBook.Id.TAXPAYER_STATUS.getId(), new Date(), null, null, null);
        Map<String, Long> result = new HashMap<String, Long>();
        for (Map<String, RefBookValue> refbookRecord : refBookRecords) {
            String code = refbookRecord.get("CODE").getStringValue();
            long id = refbookRecord.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
            result.put(code, id);
        }
        return result;
    }*/


}

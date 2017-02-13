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
    public Long identificatePerson(PersonData personData, int tresholdValue) {
        return identificatePerson(personData, tresholdValue, new PersonDataWeigthCalculator(getBaseCalculateList()));
    }

    public Long identificatePerson(PersonData personData, int tresholdValue, WeigthCalculator<PersonData> weigthComporators) {

        double treshold = tresholdValue / 1000D;
        List<PersonData> personDataList = refBookPersonDao.findPersonByPersonData(personData);
        if (personDataList != null && !personDataList.isEmpty()) {
            calculateWeigth(personData, personDataList, weigthComporators);
            //Выбор из найденных записей одной записи с максимальной Степенью соответствия критериям
            PersonData identificatedPerson = Collections.max(personDataList, new PersonDataComparator());
            if (identificatedPerson.getWeigth() > treshold) {
                //Если Степень соответствия записи выбранной записи > ПорогСхожести, то обновление данных выбранной записи справочника
                return identificatedPerson.getId();
            } else {
                return null;
            }
        } else {
            return null;
        }

    }

    private static void calculateWeigth(PersonData searchPersonData, List<PersonData> personDataList, WeigthCalculator<PersonData> weigthComporators) {
        for (PersonData personData : personDataList) {
            double weigth = weigthComporators.calc(searchPersonData, personData);
            personData.setWeigth(weigth);
        }
    }

    /**
     * Метод формирует список по которому будет рассчитывается схожесть записи
     *
     * @return
     */
    public List<BaseWeigthCalculator> getBaseCalculateList() {

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
        result.add(new BaseWeigthCalculator<PersonData>(5) {
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
                return compareNumber(a.getCitizenshipId(), b.getCitizenshipId());
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
                return compareNumber(a.getAsnuId(), b.getAsnuId());
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
                boolean result = equalsNullSafe(a.getDocumentTypeId(), b.getDocumentTypeId());
                if (!result) {
                    return 0D;
                }
                return compareString(a.getDocumentNumber(), b.getDocumentNumber());
            }
        });

        /**
         * Адрес в РФ
         */
        result.add(new BaseWeigthCalculator<PersonData>(1) {
            @Override
            public double calc(PersonData a, PersonData b) {

                boolean result = equalsNullSafeStr(a.getRegionCode(), b.getRegionCode());
                if (!result) {
                    return 0D;
                }

                result = equalsNullSafeStr(a.getPostalCode(), b.getPostalCode());
                if (!result) {
                    return 0D;
                }

                result = equalsNullSafeStr(a.getDistrict(), b.getDistrict());
                if (!result) {
                    return 0D;
                }

                result = equalsNullSafeStr(a.getCity(), b.getCity());
                if (!result) {
                    return 0D;
                }

                result = equalsNullSafeStr(a.getLocality(), b.getLocality());
                if (!result) {
                    return 0D;
                }

                result = equalsNullSafeStr(a.getStreet(), b.getStreet());
                if (!result) {
                    return 0D;
                }

                result = equalsNullSafeStr(a.getHouse(), b.getHouse());
                if (!result) {
                    return 0D;
                }

                result = equalsNullSafeStr(a.getBuild(), b.getBuild());
                if (!result) {
                    return 0D;
                }

                result = equalsNullSafeStr(a.getAppartment(), b.getAppartment());
                if (!result) {
                    return 0D;
                }

                return weigth;
            }
        });

        //адрес ино
        result.add(new BaseWeigthCalculator<PersonData>(1) {
            @Override
            public double calc(PersonData a, PersonData b) {

                boolean result = equalsNullSafe(a.getCountryId(), b.getCountryId());
                if (!result) {
                    return 0D;
                }
                return compareString(a.getAddressIno(), b.getAddressIno());
            }
        });



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

}

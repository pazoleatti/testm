package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.PersonData;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.BaseWeigthCalculator;
import com.aplana.sbrf.taxaccounting.model.util.WeigthCalculator;
import com.aplana.sbrf.taxaccounting.service.script.RefBookPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
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
        return identificatePerson(personData, tresholdValue, new Logger());
    }

    @Override
    public Long identificatePerson(PersonData personData, int tresholdValue, Logger logger) {
        return identificatePerson(personData, tresholdValue, new PersonDataWeigthCalculator(getBaseCalculateList()), logger);
    }


    public Long identificatePerson(PersonData personData, int tresholdValue, WeigthCalculator<PersonData> weigthComporators, Logger logger) {

        double treshold = tresholdValue / 1000D;
        List<PersonData> personDataList = refBookPersonDao.findPersonByPersonData(personData);
        if (personDataList != null && !personDataList.isEmpty()) {

            calculateWeigth(personData, personDataList, weigthComporators);

            StringBuffer msg = new StringBuffer();
            msg.append("Для ФЛ " + buildNotice(personData) + " сходных записей найдено: " + personDataList.size()).append(" ");

            DecimalFormat df = new DecimalFormat("0.00");

            for (PersonData applicablePersonData : personDataList) {
                msg.append("[").append(buildNotice(applicablePersonData) + " ("+df.format(applicablePersonData.getWeigth())+")").append("]");
            }

            //Выбор из найденных записей одной записи с максимальной Степенью соответствия критериям
            PersonData identificatedPerson = Collections.max(personDataList, new PersonDataComparator());
            if (identificatedPerson.getWeigth() > treshold) {
                //Если Степень соответствия записи выбранной записи > ПорогСхожести, то обновление данных выбранной записи справочника
                msg.append(". Выбрана запись: [" + buildNotice(identificatedPerson)  + " ("+df.format(identificatedPerson.getWeigth())+")]");
                logger.info(msg.toString());
                return identificatedPerson.getId();
            } else {
                msg.append(". Записей превышающих установленный порог схожести "+treshold+" не найдено");
                logger.info(msg.toString());
                return null;
            }
        } else {
            logger.info("Для ФЛ " + buildNotice(personData) + " сходных записей не найдено");
            return null;
        }

    }


    /**
     * Формирует строку в виде
     * <Номер в форме(ИНП)>: <Фамилия> <Имя> <Отчество>, <Название ДУЛ> № <Серия и номер ДУЛ>
     *
     * @param personData
     * @return
     */
    public static String buildNotice(PersonData personData) {
        StringBuffer sb = new StringBuffer();
        sb.append(personData.getInp()).append(": ");
        sb.append(emptyIfNull(personData.getLastName())).append(" ");
        sb.append(emptyIfNull(personData.getFirstName())).append(" ");
        sb.append(emptyIfNull(personData.getMiddleName())).append(" ");
        if (personData.getDocumentTypeCode() != null){
            sb.append(personData.getDocumentTypeCode() != null ? ("код: "+personData.getDocumentTypeCode()) : "").append(", ");
        }
        sb.append(emptyIfNull(personData.getDocumentNumber()));
        return sb.toString();
    }

    private static String emptyIfNull(String string){
        if (string != null){
            return string;
        } else {
            return "";
        }
    }


    private List<PersonData> getApplicable(List<PersonData> personDataList, double treshold) {
        List<PersonData> result = new ArrayList<PersonData>();
        for (PersonData personData : personDataList) {
            double weigth = personData.getWeigth();
            if (weigth > treshold) {
                result.add(personData);
            }
        }
        return result;
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
                return compareString(a.getSnils(), b.getSnils());
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

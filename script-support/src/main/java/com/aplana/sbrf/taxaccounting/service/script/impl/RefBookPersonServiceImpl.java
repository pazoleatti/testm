package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonPrimaryRnuRowMapper;
import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.PersonData;
import com.aplana.sbrf.taxaccounting.model.identification.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.BaseWeigthCalculator;
import com.aplana.sbrf.taxaccounting.model.util.WeigthCalculator;
import com.aplana.sbrf.taxaccounting.service.script.RefBookPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;

/**
 * @author Andrey Drunk
 */
@Service("refBookPersonService")
public class RefBookPersonServiceImpl implements RefBookPersonService {

    @Autowired
    private RefBookPersonDao refBookPersonDao;

    // ----------------------------- РНУ-НДФЛ  -----------------------------

    @Override
    public void fillRecordVersions(Date version) {
        refBookPersonDao.fillRecordVersions(version);
    }

    @Override
    public Map<Long, NaturalPerson> findPersonForInsertFromPrimaryRnuNdfl(Long declarationDataId, Long asnuId, Date version, NaturalPersonPrimaryRnuRowMapper naturalPersonPrimaryRnuRowMapper) {
        return refBookPersonDao.findPersonForInsertFromPrimaryRnuNdfl(declarationDataId, asnuId, version, naturalPersonPrimaryRnuRowMapper);
    }

    @Override
    public Map<Long, Map<Long, NaturalPerson>> findPersonForUpdateFromPrimaryRnuNdfl(Long declarationDataId, Long asnuId, Date version, NaturalPersonRefbookHandler naturalPersonHandler) {
        return refBookPersonDao.findPersonForUpdateFromPrimaryRnuNdfl(declarationDataId, asnuId, version, naturalPersonHandler);
    }

    @Override
    public Map<Long, Map<Long, NaturalPerson>> findPersonForCheckFromPrimaryRnuNdfl(Long declarationDataId, Long asnuId, Date version, NaturalPersonRefbookHandler naturalPersonHandler) {
        return refBookPersonDao.findPersonForCheckFromPrimaryRnuNdfl(declarationDataId, asnuId, version, naturalPersonHandler);
    }

    public List<NaturalPerson> findNaturalPersonPrimaryDataFromNdfl(long declarationDataId, RowMapper<NaturalPerson> naturalPersonRowMapper) {
        return refBookPersonDao.findNaturalPersonPrimaryDataFromNdfl(declarationDataId, naturalPersonRowMapper);
    }


    // ----------------------------- 1151111 -----------------------------

    @Override
    public Map<Long, List<PersonData>> findRefBookPersonByPrimary1151111(Long declarationDataId, Long asnuId, Date version) {
        return refBookPersonDao.findRefBookPersonByPrimary1151111(declarationDataId, asnuId, version);
    }


    /**
     * Получить "Страны"
     *
     * @return
     */
   /* def getRefCountryCode() {
        if (countryCodeCache.size() == 0) {
            def refBookMap = getRefBook(RefBook.Id.COUNTRY.getId())
            refBookMap.each { refBook ->
                    countryCodeCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
            }
        }
        return countryCodeCache;
    }

    def getRefDocumentType() {
        if (documentTypeCache.size() == 0) {
            def refBookList = getRefBook(RefBook.Id.DOCUMENT_CODES.getId())
            refBookList.each { refBook ->
                    documentTypeCache.put(refBook?.id?.numberValue, refBook)
            }
        }
        return documentTypeCache;
    }*/
    @Override
    public NaturalPerson identificatePerson(PersonData personData, List<IdentityPerson> refBookPersonList, int tresholdValue, Logger logger) {
        return identificatePerson(personData, refBookPersonList, tresholdValue, new PersonDataWeigthCalculator(getBaseCalculateList()), logger);
    }

    @Override
    public NaturalPerson identificatePerson(PersonData personData, List<IdentityPerson> refBookPersonList, int tresholdValue, WeigthCalculator<IdentityPerson> weigthComporators, Logger logger) {

        double treshold = tresholdValue / 1000D;
        List<IdentityPerson> personDataList = refBookPersonList;
        if (personDataList != null && !personDataList.isEmpty()) {

            calculateWeigth(personData, personDataList, weigthComporators);

            StringBuffer msg = new StringBuffer();


            msg.append("Для ФЛ " + buildNotice(personData) + " сходных записей найдено: " + personDataList.size()).append(" ");
            DecimalFormat df = new DecimalFormat("0.00");
            for (IdentityPerson applicablePersonData : personDataList) {
                msg.append("[").append(buildRefBookNotice(applicablePersonData) + " (" + df.format(applicablePersonData.getWeigth()) + ")").append("]");
            }

            //Выбор из найденных записей одной записи с максимальной Степенью соответствия критериям
            IdentityPerson identificatedPerson = Collections.max(personDataList, new PersonDataComparator());
            if (identificatedPerson.getWeigth() > treshold) {
                //Если Степень соответствия выбранной записи > ПорогСхожести, то обновление данных выбранной записи справочника
                //TODO убрать вывод в лог
                //if (personDataList.size() > 1){
                msg.append(". Выбрана запись: [" + buildRefBookNotice(identificatedPerson) + " (" + df.format(identificatedPerson.getWeigth()) + ")]");
                logger.info(msg.toString());
                //}

                return (NaturalPerson) identificatedPerson;
            } else {
                msg.append(". Записей превышающих установленный порог схожести " + treshold + " не найдено");
                logger.info(msg.toString());
                return null;
            }
        } else {
            //TODO убрать вывод в лог
            logger.info("Для ФЛ " + buildNotice(personData) + " сходных записей не найдено");
            return null;
        }

    }

    public String buildRefBookNotice(IdentityPerson personData) {

        StringBuffer sb = new StringBuffer();

        sb.append("СНИЛС: " + personData.getSnils()).append(": ");

        sb.append(emptyIfNull(personData.getLastName())).append(" ");
        sb.append(emptyIfNull(personData.getFirstName())).append(" ");
        sb.append(emptyIfNull(personData.getMiddleName())).append(" ");

        NaturalPerson naturalPerson = (NaturalPerson) personData;

        PersonDocument personDocument = naturalPerson.getIncludeReportDocument();

        if (personDocument != null) {
            if (personDocument.getDocType() != null) {
                sb.append(personDocument.getDocType().getCode() != null ? ("код: " + personDocument.getDocType().getCode()) : "").append(", ");
            }
            sb.append(emptyIfNull(personDocument.getDocumentNumber()));
        }

        return sb.toString();
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

        if (personData.getPersonNumber() != null) {
            sb.append("Номер: " + personData.getPersonNumber()).append(": ");
        } else if (personData.getInp() != null) {
            sb.append("ИНП: " + personData.getInp()).append(": ");
        } else if (personData.getSnils() != null) {
            sb.append("СНИЛС: " + personData.getSnils()).append(": ");
        }

        sb.append(emptyIfNull(personData.getLastName())).append(" ");
        sb.append(emptyIfNull(personData.getFirstName())).append(" ");
        sb.append(emptyIfNull(personData.getMiddleName())).append(" ");
        if (personData.getDocumentTypeCode() != null) {
            sb.append(personData.getDocumentTypeCode() != null ? ("код: " + personData.getDocumentTypeCode()) : "").append(", ");
        }
        sb.append(emptyIfNull(personData.getDocumentNumber()));
        return sb.toString();
    }

    private static String emptyIfNull(String string) {
        if (string != null) {
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


    private static void calculateWeigth(IdentityPerson searchPersonData, List<IdentityPerson> personDataList, WeigthCalculator<IdentityPerson> weigthComporators) {
        for (IdentityPerson personData : personDataList) {
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
        result.add(new BaseWeigthCalculator<IdentityPerson>("Фамилия", 5) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareString(a.getLastName(), b.getLastName());
            }
        });

        //Имя
        result.add(new BaseWeigthCalculator<IdentityPerson>("Имя", 5) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareString(a.getFirstName(), b.getFirstName());
            }
        });

        //Отчество
        result.add(new BaseWeigthCalculator<IdentityPerson>("Отчество", 5) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareString(a.getMiddleName(), b.getMiddleName());
            }
        });

        //Пол
        result.add(new BaseWeigthCalculator<IdentityPerson>("Пол", 1) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareNumber(a.getSex(), b.getSex());
            }
        });

        //Дата рождения
        result.add(new BaseWeigthCalculator<IdentityPerson>("Дата рождения", 5) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareDate(a.getBirthDate(), b.getBirthDate());
            }
        });

        //Гражданство
        result.add(new BaseWeigthCalculator<IdentityPerson>("Гражданство", 1) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareNumber(getIdOrNull(a.getCitizenship()), getIdOrNull(b.getCitizenship()));
            }

        });

        //Идентификатор физлица номер и код АСНУ
        result.add(new BaseWeigthCalculator<IdentityPerson>("Идентификатор физлица", 10) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {

                //Запись первичной НФ
                NaturalPerson primaryPerson = (NaturalPerson) a;
                //Запись справочника физлиц
                NaturalPerson refBookPerson = (NaturalPerson) b;

                PersonIdentifier primaryPersonId = primaryPerson.getPersonIdentifier();

                if (primaryPersonId != null) {
                    //Ищем совпадение в списке идентификаторов
                    PersonIdentifier refBookPersonId = findIdentifier(refBookPerson, primaryPersonId.getInp(), primaryPersonId.getAsnuId());
                    return (refBookPersonId != null) ? weigth : 0D;
                } else {
                    //если  значени параметра не задано то оно не должно учитыватся при сравнении со списком
                    return weigth;
                }
            }

            private PersonIdentifier findIdentifier(NaturalPerson person, String inp, Long asnuId) {
                for (PersonIdentifier personIdentifier : person.getPersonIdentityList()) {
                    if (equalsNullSafe(prepareStr(inp), prepareStr(personIdentifier.getInp())) && equalsNullSafe(asnuId, personIdentifier.getAsnuId())) {
                        return personIdentifier;
                    }
                }
                return null;
            }
        });

        //ИНН в РФ
        result.add(new BaseWeigthCalculator<IdentityPerson>("ИНН в РФ", 10) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareString(a.getInn(), b.getInn());
            }
        });

        //ИНН в стране гражданства
        result.add(new BaseWeigthCalculator<IdentityPerson>("ИНН Ино", 10) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareString(a.getInnForeign(), b.getInnForeign());
            }
        });

        //СНИЛС
        result.add(new BaseWeigthCalculator<IdentityPerson>("СНИЛС", 15) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
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

        //Статус налогоплательщика
//        result.add(new BaseWeigthComporator<IdentityPerson>(1) {
//            @Override
//            public double calc(IdentityPerson a, IdentityPerson b) {
//                return compareString(a.getStatus(), b.getStatus());
//            }
//        });

        //Документ вид документа и код
        result.add(new BaseWeigthCalculator<IdentityPerson>("ДУЛ", 10) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                //Запись первичной НФ
                NaturalPerson primaryPerson = (NaturalPerson) a;
                //Запись справочника физлиц
                NaturalPerson refBookPerson = (NaturalPerson) b;

                PersonDocument primaryPersonDocument = primaryPerson.getPersonDocument();

                if (primaryPersonDocument != null) {
                    Long docTypeId = primaryPersonDocument.getDocType() != null ? primaryPersonDocument.getDocType().getId() : null;
                    PersonDocument personDocument = findDocument(refBookPerson, docTypeId, primaryPersonDocument.getDocumentNumber());
                    return (personDocument != null) ? weigth : 0D;
                } else {
                    return weigth;
                }
            }

            public PersonDocument findDocument(NaturalPerson person, Long docTypeId, String docNumber) {
                for (PersonDocument personDocument : person.getPersonDocumentList()) {
                    DocType docType = personDocument.getDocType();
                    if (docType != null) {
                        if (BaseWeigthCalculator.isValueEquals(docTypeId, docType.getId())
                                && BaseWeigthCalculator.isEqualsNullSafeStr(docNumber, personDocument.getDocumentNumber())) {
                            return personDocument;
                        }
                    }
                }
                return null;
            }
        });

        /**
         * Адрес в РФ
         */
        result.add(new BaseWeigthCalculator<IdentityPerson>("Адрес в РФ", 1) {
            @Override
            public double calc(IdentityPerson personA, IdentityPerson personB) {

                Address a = personA.getAddress();
                Address b = personB.getAddress();

                if (a != null && b != null) {
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
                } else if (a == null && b == null) {
                    return weigth;
                } else {
                    return 0D;
                }
            }


        });

        //адрес ино
        result.add(new BaseWeigthCalculator<IdentityPerson>("Адрес ино", 1) {
            @Override
            public double calc(IdentityPerson personA, IdentityPerson personB) {

                Address a = personA.getAddress();
                Address b = personB.getAddress();

                if (a != null && b != null) {
                    boolean result = equalsNullSafe(getIdOrNull(a.getCountry()), getIdOrNull(b.getCountry()));
                    if (!result) {
                        return 0D;
                    }
                    return compareString(a.getAddressIno(), b.getAddressIno());
                } else if (a == null && b == null) {
                    return weigth;
                } else {
                    return 0D;
                }
            }
        });

        return result;
    }


    private class PersonDataComparator implements Comparator<IdentityPerson> {
        @Override
        public int compare(IdentityPerson a, IdentityPerson b) {
            return Double.compare(a.getWeigth(), b.getWeigth());
        }
    }

    public class PersonDataWeigthCalculator implements WeigthCalculator<IdentityPerson> {

        private List<BaseWeigthCalculator> compareList;

        public PersonDataWeigthCalculator(List<BaseWeigthCalculator> compareList) {
            this.compareList = compareList;
        }

        @Override
        public double calc(IdentityPerson a, IdentityPerson b) {
            double summWeigth = 0D;
            double summParameterWeigt = 0D;
            for (BaseWeigthCalculator calculator : compareList) {
                double weigth = calculator.calc(a, b);
                summWeigth += weigth;
                summParameterWeigt += calculator.getWeigth();
            }
            return summWeigth / summParameterWeigt;
        }
    }

}

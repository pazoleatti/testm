package com.aplana.sbrf.taxaccounting.script.service.impl;

import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.Configuration;
import com.aplana.sbrf.taxaccounting.model.identification.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.Address;
import com.aplana.sbrf.taxaccounting.model.refbook.IdDoc;
import com.aplana.sbrf.taxaccounting.model.refbook.PersonIdentifier;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookTaxpayerState;
import com.aplana.sbrf.taxaccounting.model.util.BaseWeightCalculator;
import com.aplana.sbrf.taxaccounting.model.util.WeightCalculator;
import com.aplana.sbrf.taxaccounting.model.util.impl.PersonDataWeightCalculator;
import com.aplana.sbrf.taxaccounting.script.service.RefBookPersonService;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.model.ConfigurationParam.*;
import static java.util.Arrays.asList;

/**
 * @author Andrey Drunk
 */
@Service("refBookPersonService")
public class RefBookPersonServiceImpl implements RefBookPersonService {

    @Autowired
    private RefBookPersonDao refBookPersonDao;
    @Autowired
    private ConfigurationService configurationService;

    // ----------------------------- РНУ-НДФЛ  -----------------------------


    @Override
    public void clearRnuNdflPerson(Long declarationDataId) {
        refBookPersonDao.clearRnuNdflPerson(declarationDataId);
    }

    @Override
    public void fillRecordVersions() {
        refBookPersonDao.fillRecordVersions();
    }

    @Override
    public Map<Long, Map<Long, NaturalPerson>> findPersonForUpdateFromPrimaryRnuNdfl(Long declarationDataId, NaturalPersonRefbookHandler naturalPersonHandler) {
        return refBookPersonDao.findPersonForUpdateFromPrimaryRnuNdfl(declarationDataId, naturalPersonHandler);
    }

    @Override
    public Map<Long, Map<Long, NaturalPerson>> findPersonForCheckFromPrimaryRnuNdfl(Long declarationDataId, NaturalPersonRefbookHandler naturalPersonHandler) {
        return refBookPersonDao.findPersonForCheckFromPrimaryRnuNdfl(declarationDataId, naturalPersonHandler);
    }

    @Override
    public List<NaturalPerson> findNaturalPersonPrimaryDataFromNdfl(long declarationDataId, RowMapper<NaturalPerson> naturalPersonRowMapper) {
        return refBookPersonDao.findNaturalPersonPrimaryDataFromNdfl(declarationDataId, naturalPersonRowMapper);
    }


    // ----------------------------- identification -----------------------------

    @Override
    public NaturalPerson identificatePerson(IdentificationData identificationData, Logger logger) {
        return identificatePerson(identificationData, new PersonDataWeightCalculator(getBaseCalculateList()), logger);
    }

    @Override
    public NaturalPerson identificatePerson(IdentificationData identificationData, WeightCalculator<IdentityPerson> weightCalculator, Logger logger) {

        double treshold = identificationData.getTresholdValue() / 1000D;
        List<NaturalPerson> personDataList = identificationData.getRefBookPersonList();
        if (personDataList != null && !personDataList.isEmpty()) {

            /* Если приоритет Асну в справочнике > приоритета Асну в РНУ, то устанавливаем ИД АСНУ == null, чтобы указать
            что запись не нужно ни обновлять ни создавать*/
            for (NaturalPerson person : personDataList) {
                if (person.getSource() != null && person.getSource().getId() != null && identificationData.getPriorityMap().get(person.getSource().getId()).getPriority() > identificationData.getPriorityMap().get(identificationData.getDeclarationDataAsnuId()).getPriority()) {
                    person.setNeedUpdate(false);
                }
            }

            calculateWeight(identificationData.getNaturalPerson(), personDataList, weightCalculator);

            // Удаляем ФЛ с весом < порога схожести
            List<NaturalPerson> personForRemoveList = new LinkedList<>();
            for (NaturalPerson person : personDataList) {
                if (person.getWeight() <= treshold) {
                    personForRemoveList.add(person);
                }
            }
            personDataList.removeAll(personForRemoveList);

            if (!personDataList.isEmpty()) {
                //Выбор из найденных записей одной записи с максимальной Степенью соответствия критериям

                Collections.sort(personDataList, new PersonDataComparator());

                IdentityPerson identificatedPerson = personDataList.get(0);

                if (identificatedPerson != null) {
                    DecimalFormat df = new DecimalFormat("0.00");
                    if (identificatedPerson.getWeight() > treshold) {
                        StringBuilder msg = new StringBuilder();
                        //Если Степень соответствия выбранной записи > ПорогСхожести, то обновление данных выбранной записи справочника
                        NaturalPerson declarationDataPerson = identificationData.getNaturalPerson();
                        msg.append("Раздел 1 Строка")
                                .append(declarationDataPerson.getNum())
                                .append(". ")
                                .append("Для физического лица ")
                                .append(buildFio(declarationDataPerson))
                                .append(", ")
                                .append(!declarationDataPerson.getDocuments().isEmpty() ? declarationDataPerson.getDocuments().get(0).getDocType().getName() : "")
                                .append(" № ")
                                .append(!declarationDataPerson.getDocuments().isEmpty() ? declarationDataPerson.getDocuments().get(0).getDocumentNumber() : "");
                        if (personDataList.size() > 1) {
                            msg.append(" Найдены записи в реестре ФЛ:\n");
                            for (NaturalPerson refBookPerson : personDataList) {
                                msg.append("Идентификатор ФЛ: ")
                                        .append(refBookPerson.getRecordId())
                                        .append(", ФИО: ")
                                        .append(buildFio(refBookPerson))
                                        .append(" (схожесть: ")
                                        .append(df.format(refBookPerson.getWeight()))
                                        .append(")\n");
                            }
                        }
                        msg.append(" Выбрана запись с параметрами  Идентификатор ФЛ: ")
                                .append(identificatedPerson.getRecordId())
                                .append(", ФИО: ")
                                .append(buildFio((identificatedPerson)))
                                .append(" (схожесть: ")
                                .append(df.format(identificatedPerson.getWeight()))
                                .append(")");
                        logger.infoExp(msg.toString(), "", String.format("%s, ИНП: %s", buildFio(declarationDataPerson), declarationDataPerson.getPersonIdentifier().getInp()));

                        return (NaturalPerson) identificatedPerson;
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public void calculateWeight(NaturalPerson searchPersonData, List<NaturalPerson> personDataList, WeightCalculator<IdentityPerson> weightCalculator) {
        for (IdentityPerson personData : personDataList) {
            double weight = weightCalculator.calc(searchPersonData, personData);
            personData.setWeight(weight);
        }
    }

    /**
     * Метод формирует список по которому будет рассчитываться схожесть записи
     *
     * @return
     */
    public List<BaseWeightCalculator> getBaseCalculateList() {
        Map<String, Configuration> configurations = configurationService.fetchAllByEnums(asList(
                WEIGHT_LAST_NAME, WEIGHT_FIRST_NAME, WEIGHT_MIDDLE_NAME, WEIGHT_BIRTHDAY, WEIGHT_CITIZENSHIP, WEIGHT_INP,
                WEIGHT_INN, WEIGHT_INN_FOREIGN, WEIGHT_SNILS, WEIGHT_TAX_PAYER_STATUS, WEIGHT_DUL, WEIGHT_ADDRESS, WEIGHT_ADDRESS_INO
        ));
        Map<String, Integer> weightsByCode = Maps.transformEntries(configurations, new Maps.EntryTransformer<String, Configuration, Integer>() {
            @Override
            public Integer transformEntry(String key, Configuration value) {
                return Integer.valueOf(value.getValue());
            }
        });


        List<BaseWeightCalculator> result = new ArrayList<>();

        //Фамилия
        result.add(new BaseWeightCalculator<IdentityPerson>("Фамилия", weightsByCode.get(WEIGHT_LAST_NAME.name())) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareString(a.getLastName(), b.getLastName());
            }
        });

        //Имя
        result.add(new BaseWeightCalculator<IdentityPerson>("Имя", weightsByCode.get(WEIGHT_FIRST_NAME.name())) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareString(a.getFirstName(), b.getFirstName());
            }
        });

        //Отчество
        result.add(new BaseWeightCalculator<IdentityPerson>("Отчество", weightsByCode.get(WEIGHT_MIDDLE_NAME.name())) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareString(a.getMiddleName(), b.getMiddleName());
            }
        });

        //Дата рождения
        result.add(new BaseWeightCalculator<IdentityPerson>("Дата рождения", weightsByCode.get(WEIGHT_BIRTHDAY.name())) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareDate(a.getBirthDate(), b.getBirthDate());
            }
        });

        //Гражданство
        result.add(new BaseWeightCalculator<IdentityPerson>("Гражданство", weightsByCode.get(WEIGHT_CITIZENSHIP.name())) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareNumber(getIdOrNull(a.getCitizenship()), getIdOrNull(b.getCitizenship()));
            }
        });

        //Идентификатор физлица номер и код АСНУ
        result.add(new BaseWeightCalculator<IdentityPerson>("Идентификатор физлица", weightsByCode.get(WEIGHT_INP.name())) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {

                //Запись первичной НФ
                NaturalPerson primaryPerson = (NaturalPerson) a;
                //Запись справочника физлиц
                NaturalPerson refBookPerson = (NaturalPerson) b;

                PersonIdentifier primaryPersonId = primaryPerson.getPersonIdentifier();

                if (primaryPersonId != null) {
                    //Ищем совпадение в списке идентификаторов
                    PersonIdentifier refBookPersonId = findIdentifier(refBookPerson, primaryPersonId.getInp(), primaryPersonId.getAsnu().getId());
                    return (refBookPersonId != null) ? weight : 0D;
                } else {
                    //Если  значени параметра не задано то оно не должно учитыватся при сравнении со списком
                    return weight;
                }
            }

        });

        //ИНН в РФ
        result.add(new BaseWeightCalculator<IdentityPerson>("ИНН в РФ", weightsByCode.get(WEIGHT_INN.name())) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareString(a.getInn(), b.getInn());
            }
        });

        //ИНН в стране гражданства
        result.add(new BaseWeightCalculator<IdentityPerson>("ИНН Ино", weightsByCode.get(WEIGHT_INN_FOREIGN.name())) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareString(a.getInnForeign(), b.getInnForeign());
            }
        });

        //СНИЛС
        result.add(new BaseWeightCalculator<IdentityPerson>("СНИЛС", weightsByCode.get(WEIGHT_SNILS.name())) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareString(prepareSnils(a.getSnils()), prepareSnils(b.getSnils()));
            }
        });

        //Статус налогоплательщика
        result.add(new BaseWeightCalculator<IdentityPerson>("Статус налогоплательщика", weightsByCode.get(WEIGHT_TAX_PAYER_STATUS.name())) {
            @Override
            public double calc(IdentityPerson personA, IdentityPerson personB) {
                RefBookTaxpayerState a = personA.getTaxPayerState();
                RefBookTaxpayerState b = personB.getTaxPayerState();

                if (a != null && b != null) {
                    return compareNumber(a.getId(), b.getId());
                } else if (a == null && b == null) {
                    return weight;
                } else {
                    return 0D;
                }
            }
        });

        //Документ вид документа и код
        result.add(new BaseWeightCalculator<IdentityPerson>("ДУЛ", weightsByCode.get(WEIGHT_DUL.name())) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                //Запись первичной НФ
                NaturalPerson primaryPerson = (NaturalPerson) a;
                //Запись справочника физлиц
                NaturalPerson refBookPerson = (NaturalPerson) b;

                IdDoc primaryPersonDocument = primaryPerson.getDocuments().get(0);

                if (primaryPersonDocument != null) {
                    Long docTypeId = primaryPersonDocument.getDocType() != null ? primaryPersonDocument.getDocType().getId() : null;
                    IdDoc personDocument = findDocument(refBookPerson, docTypeId, primaryPersonDocument.getDocumentNumber());
                    return (personDocument != null) ? weight : 0D;
                } else {
                    return weight;
                }
            }


        });

        //Адрес в РФ
        result.add(new BaseWeightCalculator<IdentityPerson>("Адрес в РФ", weightsByCode.get(WEIGHT_ADDRESS.name())) {
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
                    return weight;
                } else if (a == null && b == null) {
                    return weight;
                } else {
                    return 0D;
                }
            }


        });

        //Адрес ино
        result.add(new BaseWeightCalculator<IdentityPerson>("Адрес ино", weightsByCode.get(WEIGHT_ADDRESS_INO.name())) {
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
                    return weight;
                } else {
                    return 0D;
                }
            }
        });

        return result;
    }

    private String buildFio(IdentityPerson person) {
        String lastname = person.getLastName() != null ? person.getLastName() + " " : "";
        String firstname = person.getFirstName() != null ? person.getFirstName() + " " : "";
        String middlename = person.getMiddleName() != null ? person.getMiddleName() : "";
        return lastname + firstname + middlename;
    }

    private class PersonDataComparator implements Comparator<IdentityPerson> {
        @Override
        public int compare(IdentityPerson a, IdentityPerson b) {
            int weightComp = Double.compare(b.getWeight(), a.getWeight());
            if (weightComp == 0) {
                return Long.compare(b.getRecordId(), a.getRecordId());
            }
            return weightComp;
        }
    }

}

package com.aplana.sbrf.taxaccounting.script.service.impl;

import com.aplana.sbrf.taxaccounting.dao.identification.IdentificationUtils;
import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.identification.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.BaseWeigthCalculator;
import com.aplana.sbrf.taxaccounting.model.util.WeigthCalculator;
import com.aplana.sbrf.taxaccounting.model.util.impl.PersonDataWeightCalculator;
import com.aplana.sbrf.taxaccounting.script.service.RefBookPersonService;
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
    public void clearRnuNdflPerson(Long declarationDataId) {
        refBookPersonDao.clearRnuNdflPerson(declarationDataId);
    }

    @Override
    public void fillRecordVersions(Date version) {
        refBookPersonDao.fillRecordVersions(version);
    }

    @Override
    public List<NaturalPerson> findPersonForInsertFromPrimaryRnuNdfl(Long declarationDataId, Long asnuId, Date version, RowMapper<NaturalPerson> naturalPersonPrimaryRnuRowMapper) {
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
    public NaturalPerson identificatePerson(IdentificationData identificationData, WeigthCalculator<IdentityPerson> weigthComporators, Logger logger) {

        double treshold = identificationData.getTresholdValue() / 1000D;
        List<NaturalPerson> personDataList = identificationData.getRefBookPersonList();
        if (personDataList != null && !personDataList.isEmpty()) {

            /* Если приоритет Асну в справочнике > приоритета Асну в РНУ, то устанавливаем ИД АСНУ == null, чтобы указать
            что запись не нужно ни обновлять ни создавать*/
            for (NaturalPerson person : personDataList) {
                if (person.getSourceId() != null && identificationData.getPriorityMap().get(person.getSourceId()) > identificationData.getPriorityMap().get(identificationData.getDeclarationDataAsnuId())) {
                    person.setNeedUpdate(false);
                }
            }

            calculateWeigth(identificationData.getNaturalPerson(), personDataList, weigthComporators);

            // Удаляем ФЛ с весом < порога схожести
            List<NaturalPerson> personForRemoveList = new LinkedList<>();
            for (NaturalPerson person : personDataList) {
                if (person.getWeigth() <= treshold) {
                    personForRemoveList.add(person);
                }
            }
            personDataList.removeAll(personForRemoveList);

            if (!personDataList.isEmpty()) {
                //Выбор из найденных записей одной записи с максимальной Степенью соответствия критериям

                Collections.sort(personDataList, new PersonDataComparator());

                IdentityPerson identificatedPerson = personDataList.get(0);

                if (identificatedPerson != null) {
                    StringBuffer msg = new StringBuffer();

                    msg.append("Для ФЛ " + IdentificationUtils.buildNotice(identificationData.getNaturalPerson()) + " сходных записей найдено: " + personDataList.size()).append(" ");
                    DecimalFormat df = new DecimalFormat("0.00");
                    for (IdentityPerson applicablePersonData : personDataList) {
                        msg.append("[").append(IdentificationUtils.buildRefBookNotice((NaturalPerson) applicablePersonData) + " (" + df.format(applicablePersonData.getWeigth()) + ")").append("]");
                    }
                    if (identificatedPerson.getWeigth() > treshold) {
                        //Если Степень соответствия выбранной записи > ПорогСхожести, то обновление данных выбранной записи справочника
                        if (personDataList.size() > 1) {
                            msg.append(". Выбрана запись: [" + IdentificationUtils.buildRefBookNotice((NaturalPerson) identificatedPerson) + " (" + df.format(identificatedPerson.getWeigth()) + ")]");
                            logger.info(msg.toString());
                        }

                        return (NaturalPerson) identificatedPerson;
                    } else {
                        //msg.append(". Записей превышающих установленный порог схожести " + treshold + " не найдено");
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            //logger.info("Для ФЛ " + buildNotice(personData) + " сходных записей не найдено");
            return null;
        }
    }

    public void calculateWeigth(NaturalPerson searchPersonData, List<NaturalPerson> personDataList, WeigthCalculator<IdentityPerson> weigthComporators) {
        for (IdentityPerson personData : personDataList) {
            double weigth = weigthComporators.calc(searchPersonData, personData);
            personData.setWeigth(weigth);
        }
    }

    /**
     * Метод формирует список по которому будет рассчитываться схожесть записи
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
        result.add(new BaseWeigthCalculator<IdentityPerson>("Имя", 10) {
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

        //Дата рождения
        result.add(new BaseWeigthCalculator<IdentityPerson>("Дата рождения", 10) {
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
        result.add(new BaseWeigthCalculator<IdentityPerson>("Идентификатор физлица", 15) {
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
                    //Если  значени параметра не задано то оно не должно учитыватся при сравнении со списком
                    return weigth;
                }
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
                return compareString(prepareSnils(a.getSnils()), prepareSnils(b.getSnils()));
            }
        });

        //Статус налогоплательщика
        result.add(new BaseWeigthCalculator<IdentityPerson>("Статус налогоплательщика", 1) {
            @Override
            public double calc(IdentityPerson personA, IdentityPerson personB) {
                TaxpayerStatus a = personA.getTaxPayerStatus();
                TaxpayerStatus b = personB.getTaxPayerStatus();

                if (a != null && b != null) {
                    return compareNumber(a.getId(), b.getId());
                } else if (a == null && b == null) {
                    return weigth;
                } else {
                    return 0D;
                }
            }
        });

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


        });

        //Адрес в РФ
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

        //Адрес ино
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
            int weightComp = Double.compare(a.getWeigth(), b.getWeigth());
            if (weightComp == 0) {
                return Long.compare(b.getRecordId(), a.getRecordId());
            }
            return weightComp;
        }
    }

}

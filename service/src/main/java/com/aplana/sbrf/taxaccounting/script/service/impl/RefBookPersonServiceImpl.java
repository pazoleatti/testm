package com.aplana.sbrf.taxaccounting.script.service.impl;

import com.aplana.sbrf.taxaccounting.dao.IdDocDao;
import com.aplana.sbrf.taxaccounting.dao.IdTaxPayerDao;
import com.aplana.sbrf.taxaccounting.dao.PersonTbDao;
import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.person.NaturalPersonMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.Configuration;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.identification.IdentificationData;
import com.aplana.sbrf.taxaccounting.model.identification.IdentityPerson;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.Address;
import com.aplana.sbrf.taxaccounting.model.refbook.IdDoc;
import com.aplana.sbrf.taxaccounting.model.refbook.PersonIdentifier;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookTaxpayerState;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import com.aplana.sbrf.taxaccounting.model.util.BaseWeightCalculator;
import com.aplana.sbrf.taxaccounting.model.util.WeightCalculator;
import com.aplana.sbrf.taxaccounting.model.util.impl.PersonDataWeightCalculator;
import com.aplana.sbrf.taxaccounting.script.service.RefBookPersonService;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.TransactionHelper;
import com.aplana.sbrf.taxaccounting.service.TransactionLogic;
import com.aplana.sbrf.taxaccounting.service.component.lock.BaseLockKeyGenerator;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.model.ConfigurationParam.WEIGHT_ADDRESS;
import static com.aplana.sbrf.taxaccounting.model.ConfigurationParam.WEIGHT_ADDRESS_INO;
import static com.aplana.sbrf.taxaccounting.model.ConfigurationParam.WEIGHT_BIRTHDAY;
import static com.aplana.sbrf.taxaccounting.model.ConfigurationParam.WEIGHT_CITIZENSHIP;
import static com.aplana.sbrf.taxaccounting.model.ConfigurationParam.WEIGHT_DUL;
import static com.aplana.sbrf.taxaccounting.model.ConfigurationParam.WEIGHT_FIRST_NAME;
import static com.aplana.sbrf.taxaccounting.model.ConfigurationParam.WEIGHT_INN;
import static com.aplana.sbrf.taxaccounting.model.ConfigurationParam.WEIGHT_INN_FOREIGN;
import static com.aplana.sbrf.taxaccounting.model.ConfigurationParam.WEIGHT_INP;
import static com.aplana.sbrf.taxaccounting.model.ConfigurationParam.WEIGHT_LAST_NAME;
import static com.aplana.sbrf.taxaccounting.model.ConfigurationParam.WEIGHT_MIDDLE_NAME;
import static com.aplana.sbrf.taxaccounting.model.ConfigurationParam.WEIGHT_SNILS;
import static com.aplana.sbrf.taxaccounting.model.ConfigurationParam.WEIGHT_TAX_PAYER_STATUS;
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
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private BaseLockKeyGenerator baseLockKeyGenerator;
    @Autowired
    private IdTaxPayerDao idTaxPayerDao;
    @Autowired
    private IdDocDao idDocDao;
    @Autowired
    private PersonTbDao personTbDao;
    @Autowired
    private TransactionHelper tx;

    @Override
    public RegistryPerson findById(long id) {
        return refBookPersonDao.fetchPersonVersion(id);
    }

    @Override
    public  List<RegistryPerson> findAllRecordList(long id) {
        return refBookPersonDao.fetchNonDuplicatesVersions(id);
    }

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

            /* ???????? ?????????????????? ???????? ?? ?????????????????????? > ???????????????????? ???????? ?? ??????, ???? ?????????????????????????? ???? ???????? == null, ?????????? ??????????????
            ?????? ???????????? ???? ?????????? ???? ?????????????????? ???? ??????????????????*/
            for (NaturalPerson person : personDataList) {
                if (person.getSource() != null && person.getSource().getId() != null && identificationData.getPriorityMap().get(person.getSource().getId()).getPriority() > identificationData.getPriorityMap().get(identificationData.getDeclarationDataAsnuId()).getPriority()) {
                    person.setNeedUpdate(false);
                }
            }

            calculateWeight(identificationData.getNaturalPerson(), personDataList, weightCalculator);

            // ?????????????? ???? ?? ?????????? < ???????????? ????????????????
            List<NaturalPerson> personForRemoveList = new LinkedList<>();
            for (NaturalPerson person : personDataList) {
                if (person.getWeight() <= treshold) {
                    personForRemoveList.add(person);
                }
            }
            personDataList.removeAll(personForRemoveList);

            if (!personDataList.isEmpty()) {
                //?????????? ???? ?????????????????? ?????????????? ?????????? ???????????? ?? ???????????????????????? ???????????????? ???????????????????????? ??????????????????

                Collections.sort(personDataList, new PersonDataComparator());

                IdentityPerson identificatedPerson = personDataList.get(0);

                if (identificatedPerson != null) {
                    DecimalFormat df = new DecimalFormat("0.00");
                    if (identificatedPerson.getWeight() > treshold) {
                        StringBuilder msg = new StringBuilder();
                        //???????? ?????????????? ???????????????????????? ?????????????????? ???????????? > ??????????????????????????, ???? ???????????????????? ???????????? ?????????????????? ???????????? ??????????????????????
                        NaturalPerson declarationDataPerson = identificationData.getNaturalPerson();
                        msg.append("???????????? 1 ????????????")
                                .append(declarationDataPerson.getNum())
                                .append(". ")
                                .append("?????? ?????????????????????? ???????? ")
                                .append(buildFio(declarationDataPerson))
                                .append(", ")
                                .append(!declarationDataPerson.getDocuments().isEmpty() ? declarationDataPerson.getDocuments().get(0).getDocType().getName() : "")
                                .append(" ??? ")
                                .append(!declarationDataPerson.getDocuments().isEmpty() ? declarationDataPerson.getDocuments().get(0).getDocumentNumber() : "");
                        if (personDataList.size() > 1) {
                            msg.append(" ?????????????? ???????????? ?? ?????????????? ????:\n");
                            for (NaturalPerson refBookPerson : personDataList) {
                                msg.append("?????????????????????????? ????: ")
                                        .append(refBookPerson.getRecordId())
                                        .append(", ??????: ")
                                        .append(buildFio(refBookPerson))
                                        .append(" (????????????????: ")
                                        .append(df.format(refBookPerson.getWeight()))
                                        .append(")\n");
                            }
                        }
                        msg.append(" ?????????????? ???????????? ?? ??????????????????????  ?????????????????????????? ????: ")
                                .append(identificatedPerson.getRecordId())
                                .append(", ??????: ")
                                .append(buildFio((identificatedPerson)))
                                .append(" (????????????????: ")
                                .append(df.format(identificatedPerson.getWeight()))
                                .append(")");
                        logger.infoExp(msg.toString(), "", String.format("%s, ??????: %s", buildFio(declarationDataPerson), declarationDataPerson.getPersonIdentifier().getInp()));

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
     * ?????????? ?????????????????? ???????????? ???? ???????????????? ?????????? ???????????????????????????? ???????????????? ????????????
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

        //??????????????
        result.add(new BaseWeightCalculator<IdentityPerson>("??????????????", weightsByCode.get(WEIGHT_LAST_NAME.name())) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareString(a.getLastName(), b.getLastName());
            }
        });

        //??????
        result.add(new BaseWeightCalculator<IdentityPerson>("??????", weightsByCode.get(WEIGHT_FIRST_NAME.name())) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareString(a.getFirstName(), b.getFirstName());
            }
        });

        //????????????????
        result.add(new BaseWeightCalculator<IdentityPerson>("????????????????", weightsByCode.get(WEIGHT_MIDDLE_NAME.name())) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareString(a.getMiddleName(), b.getMiddleName());
            }
        });

        //???????? ????????????????
        result.add(new BaseWeightCalculator<IdentityPerson>("???????? ????????????????", weightsByCode.get(WEIGHT_BIRTHDAY.name())) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareDate(a.getBirthDate(), b.getBirthDate());
            }
        });

        //??????????????????????
        result.add(new BaseWeightCalculator<IdentityPerson>("??????????????????????", weightsByCode.get(WEIGHT_CITIZENSHIP.name())) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareNumber(getIdOrNull(a.getCitizenship()), getIdOrNull(b.getCitizenship()));
            }
        });

        //?????????????????????????? ?????????????? ?????????? ?? ?????? ????????
        result.add(new BaseWeightCalculator<IdentityPerson>("?????????????????????????? ??????????????", weightsByCode.get(WEIGHT_INP.name())) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {

                //???????????? ?????????????????? ????
                NaturalPerson primaryPerson = (NaturalPerson) a;
                //???????????? ?????????????????????? ????????????
                NaturalPerson refBookPerson = (NaturalPerson) b;

                PersonIdentifier primaryPersonId = primaryPerson.getPersonIdentifier();

                if (primaryPersonId != null) {
                    //???????? ???????????????????? ?? ???????????? ??????????????????????????????
                    PersonIdentifier refBookPersonId = findIdentifier(refBookPerson, primaryPersonId.getInp(), primaryPersonId.getAsnu().getId());
                    return (refBookPersonId != null) ? weight : 0D;
                } else {
                    //????????  ?????????????? ?????????????????? ???? ???????????? ???? ?????? ???? ???????????? ???????????????????? ?????? ?????????????????? ???? ??????????????
                    return weight;
                }
            }

        });

        //?????? ?? ????
        result.add(new BaseWeightCalculator<IdentityPerson>("?????? ?? ????", weightsByCode.get(WEIGHT_INN.name())) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareString(a.getInn(), b.getInn());
            }
        });

        //?????? ?? ???????????? ??????????????????????
        result.add(new BaseWeightCalculator<IdentityPerson>("?????? ??????", weightsByCode.get(WEIGHT_INN_FOREIGN.name())) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareString(a.getInnForeign(), b.getInnForeign());
            }
        });

        //??????????
        result.add(new BaseWeightCalculator<IdentityPerson>("??????????", weightsByCode.get(WEIGHT_SNILS.name())) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                return compareString(prepareSnils(a.getSnils()), prepareSnils(b.getSnils()));
            }
        });

        //???????????? ??????????????????????????????????
        result.add(new BaseWeightCalculator<IdentityPerson>("???????????? ??????????????????????????????????", weightsByCode.get(WEIGHT_TAX_PAYER_STATUS.name())) {
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

        //???????????????? ?????? ?????????????????? ?? ??????
        result.add(new BaseWeightCalculator<IdentityPerson>("??????", weightsByCode.get(WEIGHT_DUL.name())) {
            @Override
            public double calc(IdentityPerson a, IdentityPerson b) {
                //???????????? ?????????????????? ????
                NaturalPerson primaryPerson = (NaturalPerson) a;
                //???????????? ?????????????????????? ????????????
                NaturalPerson refBookPerson = (NaturalPerson) b;

                IdDoc primaryPersonDocument = null;
                if (!primaryPerson.getDocuments().isEmpty()) {
                    primaryPersonDocument = primaryPerson.getDocuments().get(0);
                }

                if (primaryPersonDocument != null) {
                    Long docTypeId = primaryPersonDocument.getDocType() != null ? primaryPersonDocument.getDocType().getId() : null;
                    IdDoc personDocument = findDocument(refBookPerson, docTypeId, primaryPersonDocument.getDocumentNumber());
                    return (personDocument != null) ? weight : 0D;
                } else {
                    return weight;
                }
            }


        });

        //?????????? ?? ????
        result.add(new BaseWeightCalculator<IdentityPerson>("?????????? ?? ????", weightsByCode.get(WEIGHT_ADDRESS.name())) {
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

        //?????????? ??????
        result.add(new BaseWeightCalculator<IdentityPerson>("?????????? ??????", weightsByCode.get(WEIGHT_ADDRESS_INO.name())) {
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

    @Override
    public Long findMaxRegistryPersonId(Date currentDate) {
        return refBookPersonDao.findMaxRegistryPersonId(currentDate);
    }

    @Override
    public boolean lockPersonsRegistry(final TAUserInfo userInfo, Long taskDataId) {
        Boolean result = false;
        final String lockKey = baseLockKeyGenerator.generatePersonsRegistryLockKey();
        try {
            LockData lockData = tx.executeInNewTransaction(new TransactionLogic<LockData>() {
                @Override
                public LockData execute() {
                    return lockDataService.lock(lockKey, userInfo.getUser().getId(), "???????????? ???????????????????? ??????");
                }
            });
            if (lockData == null) {
                result = true;
                lockDataService.bindTask(lockKey, taskDataId);
            }
        } catch (Exception e) {
            return false;
        }
        return result;
    }

    @Override
    public List<NaturalPerson> findNewRegistryPersons(Long oldMaxId, Date currentDate, NaturalPersonMapper naturalPersonMapper) {
        List<NaturalPerson> result = refBookPersonDao.findNewRegistryPersons(oldMaxId, currentDate, naturalPersonMapper);
        for (RegistryPerson person : result) {
            person.getPersonIdentityList().addAll(idTaxPayerDao.getByPerson(person));
            person.getDocuments().addAll(idDocDao.getByPerson(person));
            person.getPersonTbList().addAll(personTbDao.getByPerson(person));
        }
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

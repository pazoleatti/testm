package com.aplana.sbrf.taxaccounting.service.impl.component.lock.locker;

import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationFormKind;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.OperationType;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.service.TransactionHelper;
import com.aplana.sbrf.taxaccounting.service.TransactionLogic;
import com.aplana.sbrf.taxaccounting.service.component.lock.LockKeyGenerator;
import com.aplana.sbrf.taxaccounting.service.component.lock.descriptor.DeclarationDataKeyLockDescriptor;
import com.aplana.sbrf.taxaccounting.service.component.lock.locker.DeclarationLocker;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component("declarationLocker")
public class DeclarationLockerImpl implements DeclarationLocker {

    private static final Set<OperationType> SET_IMPORT_EXCEL = ImmutableSet.of(
            OperationType.UPDATE_PERSONS_DATA,
            OperationType.CHECK_DEC, OperationType.ACCEPT_DEC, OperationType.RETURN_DECLARATION,
            OperationType.DELETE_DEC, OperationType.EXCEL_DEC, OperationType.RNU_NDFL_PERSON_DB,
            OperationType.RNU_NDFL_PERSON_ALL_DB, OperationType.EXCEL_TEMPLATE_DEC,
            OperationType.RNU_PAYMENT_REPORT, OperationType.RNU_RATE_REPORT, OperationType.RNU_NDFL_DETAIL_REPORT,
            OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT, OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT,
            OperationType.LOAD_TRANSPORT_FILE, OperationType.IMPORT_DECLARATION_EXCEL, OperationType.IDENTIFY_PERSON,
            OperationType.CONSOLIDATE, OperationType.EDIT);

    private static final Set<OperationType> SET_IDENTIFY = ImmutableSet.of(
            OperationType.UPDATE_PERSONS_DATA,
            OperationType.CHECK_DEC, OperationType.ACCEPT_DEC, OperationType.RETURN_DECLARATION,
            OperationType.DELETE_DEC, OperationType.EXCEL_DEC, OperationType.RNU_NDFL_PERSON_DB,
            OperationType.RNU_NDFL_PERSON_ALL_DB, OperationType.EXCEL_TEMPLATE_DEC,
            OperationType.RNU_PAYMENT_REPORT, OperationType.RNU_RATE_REPORT, OperationType.RNU_NDFL_DETAIL_REPORT,
            OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT, OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT,
            OperationType.LOAD_TRANSPORT_FILE, OperationType.IMPORT_DECLARATION_EXCEL,
            OperationType.CONSOLIDATE, OperationType.EDIT, OperationType.TRANSFER);

    private static final Set<OperationType> SET_IMPORT_TF = ImmutableSet.of(
            OperationType.UPDATE_PERSONS_DATA,
            OperationType.CHECK_DEC, OperationType.ACCEPT_DEC, OperationType.RETURN_DECLARATION,
            OperationType.DELETE_DEC, OperationType.EXCEL_DEC, OperationType.RNU_NDFL_PERSON_DB,
            OperationType.RNU_NDFL_PERSON_ALL_DB, OperationType.EXCEL_TEMPLATE_DEC,
            OperationType.RNU_PAYMENT_REPORT, OperationType.RNU_RATE_REPORT, OperationType.RNU_NDFL_DETAIL_REPORT,
            OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT, OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT,
            OperationType.LOAD_TRANSPORT_FILE, OperationType.IMPORT_DECLARATION_EXCEL, OperationType.IDENTIFY_PERSON);

    private static final Set<OperationType> SET_UPDATE_PERSONS_DATA = ImmutableSet.of(
            OperationType.ACCEPT_DEC, OperationType.CHECK_DEC, OperationType.CONSOLIDATE,
            OperationType.DELETE_DEC, OperationType.EDIT, OperationType.IDENTIFY_PERSON,
            OperationType.LOAD_TRANSPORT_FILE, OperationType.REPORT_KPP_OKTMO, OperationType.RETURN_DECLARATION,
            OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT, OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT,
            OperationType.RNU_NDFL_DETAIL_REPORT, OperationType.RNU_NDFL_PERSON_DB,
            OperationType.RNU_NDFL_PERSON_ALL_DB, OperationType.RNU_PAYMENT_REPORT, OperationType.RNU_RATE_REPORT,
            OperationType.EXCEL_DEC, OperationType.EXCEL_TEMPLATE_DEC, OperationType.DECLARATION_2NDFL1, OperationType.DECLARATION_2NDFL2,
            OperationType.DECLARATION_6NDFL, OperationType.DECLARATION_2NDFL_FL, OperationType.IMPORT_DECLARATION_EXCEL, OperationType.TRANSFER);

    private static final Set<OperationType> SET_CHECK__ACCEPT = ImmutableSet.of(
            OperationType.ACCEPT_DEC, OperationType.CHECK_DEC,
            OperationType.CONSOLIDATE, OperationType.DELETE_DEC, OperationType.EDIT, OperationType.IDENTIFY_PERSON,
            OperationType.LOAD_TRANSPORT_FILE, OperationType.RETURN_DECLARATION,
            OperationType.UPDATE_PERSONS_DATA, OperationType.IMPORT_DECLARATION_EXCEL, OperationType.UPDATE_DOC_STATE);

    private static final Set<OperationType> SET_RETURN_DECLARATION = ImmutableSet.of(
            OperationType.ACCEPT_DEC, OperationType.CHECK_DEC,
            OperationType.CONSOLIDATE, OperationType.DELETE_DEC, OperationType.EDIT, OperationType.IDENTIFY_PERSON,
            OperationType.LOAD_TRANSPORT_FILE, OperationType.RETURN_DECLARATION,
            OperationType.UPDATE_PERSONS_DATA, OperationType.IMPORT_DECLARATION_EXCEL, OperationType.UPDATE_DOC_STATE,
            OperationType.SEND_EDO, OperationType.TRANSFER);


    private static final Set<OperationType> SET_EDIT = ImmutableSet.of(
            OperationType.ACCEPT_DEC, OperationType.CHECK_DEC, OperationType.CONSOLIDATE,
            OperationType.DELETE_DEC, OperationType.REPORT_KPP_OKTMO, OperationType.RETURN_DECLARATION,
            OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT, OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT, OperationType.RNU_NDFL_DETAIL_REPORT,
            OperationType.RNU_NDFL_PERSON_DB, OperationType.RNU_NDFL_PERSON_ALL_DB,
            OperationType.RNU_PAYMENT_REPORT, OperationType.RNU_RATE_REPORT, OperationType.UPDATE_PERSONS_DATA,
            OperationType.EXCEL_DEC, OperationType.DECLARATION_2NDFL1, OperationType.DECLARATION_2NDFL2,
            OperationType.DECLARATION_6NDFL, OperationType.DECLARATION_2NDFL_FL, OperationType.EXCEL_TEMPLATE_DEC, OperationType.IDENTIFY_PERSON,
            OperationType.IMPORT_DECLARATION_EXCEL, OperationType.TRANSFER);


    private static final Set<OperationType> SET_REPORT_KPP_OKTMO__2NDFL1__2NDFL2__6NDFL = ImmutableSet.of(
            OperationType.CONSOLIDATE, OperationType.DELETE_DEC, OperationType.EDIT, OperationType.UPDATE_PERSONS_DATA);

    private static final Set<OperationType> SET_CONSOLIDATE = ImmutableSet.of(
            OperationType.CONSOLIDATE, OperationType.DELETE_DEC, OperationType.EDIT, OperationType.UPDATE_PERSONS_DATA,
            OperationType.IDENTIFY_PERSON, OperationType.ACCEPT_DEC, OperationType.IMPORT_DECLARATION_EXCEL,
            OperationType.RETURN_DECLARATION, OperationType.TRANSFER);

    private static final Set<OperationType> SET_EDIT_FILE__PDF__EXPORT_REPORTS__REPORT_2NDFL1__REPORT_2NDFL2 = ImmutableSet.of(OperationType.DELETE_DEC);

    private static final Set<OperationType> SET_DELETE = ImmutableSet.of(
            OperationType.ACCEPT_DEC, OperationType.CHECK_DEC, OperationType.CONSOLIDATE,
            OperationType.DELETE_DEC, OperationType.EDIT, OperationType.EDIT_FILE,
            OperationType.IDENTIFY_PERSON, OperationType.LOAD_TRANSPORT_FILE, OperationType.PDF_DEC,
            OperationType.REPORT_2NDFL1, OperationType.REPORT_2NDFL2, OperationType.REPORT_KPP_OKTMO,
            OperationType.RETURN_DECLARATION, OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT,
            OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT, OperationType.RNU_NDFL_DETAIL_REPORT,
            OperationType.RNU_NDFL_PERSON_DB, OperationType.RNU_NDFL_PERSON_ALL_DB,
            OperationType.RNU_PAYMENT_REPORT, OperationType.RNU_RATE_REPORT, OperationType.UPDATE_PERSONS_DATA,
            OperationType.EXCEL_DEC, OperationType.DECLARATION_2NDFL1, OperationType.DECLARATION_2NDFL2,
            OperationType.DECLARATION_6NDFL, OperationType.DECLARATION_2NDFL_FL, OperationType.EXCEL_TEMPLATE_DEC, OperationType.EXPORT_REPORTS,
            OperationType.IMPORT_DECLARATION_EXCEL, OperationType.UPDATE_DOC_STATE, OperationType.SEND_EDO,
            OperationType.TRANSFER
    );

    private static final Set<OperationType> SET_XLSX = ImmutableSet.of(
            OperationType.IDENTIFY_PERSON, OperationType.LOAD_TRANSPORT_FILE, OperationType.IMPORT_DECLARATION_EXCEL);

    private static final Set<OperationType> SET_SPEC_REPORT = ImmutableSet.of(
            OperationType.CONSOLIDATE, OperationType.DELETE_DEC,
            OperationType.EDIT, OperationType.IDENTIFY_PERSON, OperationType.LOAD_TRANSPORT_FILE,
            OperationType.UPDATE_PERSONS_DATA, OperationType.IMPORT_DECLARATION_EXCEL);

    private static final Set<OperationType> SET_EXCEL_TEMPLATE = ImmutableSet.of(
            OperationType.DELETE_DEC, OperationType.IDENTIFY_PERSON,
            OperationType.LOAD_TRANSPORT_FILE, OperationType.UPDATE_PERSONS_DATA, OperationType.IMPORT_DECLARATION_EXCEL,
            OperationType.EDIT);

    private static final Set<OperationType> SET_UPDATE_DOC_STATE = ImmutableSet.of(
            OperationType.ACCEPT_DEC, OperationType.CHECK_DEC,
            OperationType.DELETE_DEC, OperationType.RETURN_DECLARATION, OperationType.SEND_EDO);

    private static final Set<OperationType> SET_SEND_EDO = ImmutableSet.of(
            OperationType.DELETE_DEC, OperationType.RETURN_DECLARATION,
            OperationType.UPDATE_DOC_STATE);

    private static final Set<OperationType> SET_TRANSFER = ImmutableSet.of(
            OperationType.DELETE_DEC, OperationType.IDENTIFY_PERSON, OperationType.RETURN_DECLARATION,
            OperationType.UPDATE_PERSONS_DATA, OperationType.CONSOLIDATE);

    // Зависимости
    private final LockKeyGenerator mainLockKeyGenerator;
    private final LockKeyGenerator checkupLockKeyGenerator;
    private final DeclarationDataKeyLockDescriptor declarationDataKeyLockDescriptor;
    private final LockDataDao lockDataDao;
    private final DeclarationDataService declarationDataService;
    private final DeclarationTemplateService declarationTemplateService;
    private final TAUserService taUserService;
    private final TransactionHelper tx;

    @Autowired
    public DeclarationLockerImpl(@Qualifier("mainLockKeyGeneratorImpl") LockKeyGenerator mainLockKeyGenerator,
                                 @Qualifier("checkupLockKeyGeneratorImpl") LockKeyGenerator checkupKeyGenerator,
                                 DeclarationDataKeyLockDescriptor declarationDataKeyLockDescriptor,
                                 LockDataDao lockDataDao,
                                 DeclarationDataService declarationDataService,
                                 DeclarationTemplateService declarationTemplateService,
                                 TAUserService taUserService,
                                 TransactionHelper tx) {
        this.mainLockKeyGenerator = mainLockKeyGenerator;
        this.checkupLockKeyGenerator = checkupKeyGenerator;
        this.declarationDataKeyLockDescriptor = declarationDataKeyLockDescriptor;
        this.lockDataDao = lockDataDao;
        this.declarationDataService = declarationDataService;
        this.declarationTemplateService = declarationTemplateService;
        this.taUserService = taUserService;
        this.tx = tx;
    }

    @Override
    public LockData establishLock(Long declarationDataId, OperationType operationType, TAUserInfo userInfo, Logger logger) {
        List<LockData> lockDataList = establishLock(Collections.singletonList(declarationDataId), operationType, userInfo, logger);
        if (CollectionUtils.isEmpty(lockDataList)) {
            return null;
        }
        return lockDataList.get(0);
    }

    @Override
    public List<LockData> establishLock(List<Long> declarationDataIdList, OperationType operationType, TAUserInfo userInfo, Logger logger) {
        return establishLockWithAdditionalParams(declarationDataIdList, operationType, null, userInfo, logger);
    }

    @Override
    public List<LockData> establishLockWithAdditionalParams(List<Long> declarationDataIdList, OperationType operationType, Map<String, Long> additionalParams, TAUserInfo userInfo, Logger logger) {
        if (operationType.equals(OperationType.LOAD_TRANSPORT_FILE))
            return doCheckAndLock(declarationDataIdList, operationType, SET_IMPORT_TF, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.IMPORT_DECLARATION_EXCEL))
            return doCheckAndLock(declarationDataIdList, operationType, SET_IMPORT_EXCEL, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.IDENTIFY_PERSON))
            return doCheckAndLock(declarationDataIdList, operationType, SET_IDENTIFY, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.UPDATE_PERSONS_DATA))
            return doCheckAndLock(declarationDataIdList, operationType, SET_UPDATE_PERSONS_DATA, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.CHECK_DEC))
            return doCheckAndLock(declarationDataIdList, operationType, SET_CHECK__ACCEPT, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.ACCEPT_DEC))
            return doCheckAndLock(declarationDataIdList, operationType, SET_CHECK__ACCEPT, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.DELETE_DEC))
            return doCheckAndLock(declarationDataIdList, operationType, SET_DELETE, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.CONSOLIDATE))
            return doCheckAndLock(declarationDataIdList, operationType, SET_CONSOLIDATE, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.EXCEL_DEC))
            return doCheckAndLock(declarationDataIdList, operationType, SET_XLSX, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.EXCEL_TEMPLATE_DEC))
            return doCheckAndLock(declarationDataIdList, operationType, SET_EXCEL_TEMPLATE, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.PDF_DEC))
            return doCheckAndLock(declarationDataIdList, operationType, SET_EDIT_FILE__PDF__EXPORT_REPORTS__REPORT_2NDFL1__REPORT_2NDFL2, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.RETURN_DECLARATION))
            return doCheckAndLock(declarationDataIdList, operationType, SET_RETURN_DECLARATION, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.EDIT))
            return doCheckAndLock(declarationDataIdList, operationType, SET_EDIT, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.EDIT_FILE))
            return doCheckAndLock(declarationDataIdList, operationType, SET_EDIT_FILE__PDF__EXPORT_REPORTS__REPORT_2NDFL1__REPORT_2NDFL2, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.RNU_NDFL_PERSON_DB))
            return doCheckAndLock(declarationDataIdList, operationType, SET_SPEC_REPORT, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.RNU_NDFL_PERSON_ALL_DB))
            return doCheckAndLock(declarationDataIdList, operationType, SET_SPEC_REPORT, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.REPORT_KPP_OKTMO))
            return doCheckAndLock(declarationDataIdList, operationType, SET_REPORT_KPP_OKTMO__2NDFL1__2NDFL2__6NDFL, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.RNU_RATE_REPORT))
            return doCheckAndLock(declarationDataIdList, operationType, SET_SPEC_REPORT, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.RNU_PAYMENT_REPORT))
            return doCheckAndLock(declarationDataIdList, operationType, SET_SPEC_REPORT, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.RNU_NDFL_DETAIL_REPORT))
            return doCheckAndLock(declarationDataIdList, operationType, SET_SPEC_REPORT, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT))
            return doCheckAndLock(declarationDataIdList, operationType, SET_SPEC_REPORT, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT))
            return doCheckAndLock(declarationDataIdList, operationType, SET_SPEC_REPORT, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.REPORT_2NDFL1))
            return doCheckAndLock(declarationDataIdList, operationType, SET_EDIT_FILE__PDF__EXPORT_REPORTS__REPORT_2NDFL1__REPORT_2NDFL2, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.REPORT_2NDFL2))
            return doCheckAndLock(declarationDataIdList, operationType, SET_EDIT_FILE__PDF__EXPORT_REPORTS__REPORT_2NDFL1__REPORT_2NDFL2, additionalParams, userInfo, logger);
        else if (operationType.equals(OperationType.DECLARATION_2NDFL1)) {
            return doCheckAndLock(declarationDataIdList, operationType, SET_REPORT_KPP_OKTMO__2NDFL1__2NDFL2__6NDFL, additionalParams, userInfo, logger);
        } else if (operationType.equals(OperationType.DECLARATION_2NDFL2)) {
            return doCheckAndLock(declarationDataIdList, operationType, SET_REPORT_KPP_OKTMO__2NDFL1__2NDFL2__6NDFL, additionalParams, userInfo, logger);
        } else if (operationType.equals(OperationType.DECLARATION_6NDFL)) {
            return doCheckAndLock(declarationDataIdList, operationType, SET_REPORT_KPP_OKTMO__2NDFL1__2NDFL2__6NDFL, additionalParams, userInfo, logger);
        } else if (operationType.equals(OperationType.DECLARATION_2NDFL_FL)) {
            return doCheckAndLock(declarationDataIdList, operationType, SET_REPORT_KPP_OKTMO__2NDFL1__2NDFL2__6NDFL, additionalParams, userInfo, logger);
        } else if (operationType.equals(OperationType.EXPORT_REPORTS)) {
            return doCheckAndLock(declarationDataIdList, operationType, SET_EDIT_FILE__PDF__EXPORT_REPORTS__REPORT_2NDFL1__REPORT_2NDFL2, additionalParams, userInfo, logger);
        } else if (operationType.equals(OperationType.UPDATE_DOC_STATE)) {
            return doCheckAndLock(declarationDataIdList, operationType, SET_UPDATE_DOC_STATE, additionalParams, userInfo, logger);
        } else if (operationType.equals(OperationType.SEND_EDO)) {
            return doCheckAndLock(declarationDataIdList, operationType, SET_SEND_EDO, additionalParams, userInfo, logger);
        } else if (operationType.equals(OperationType.TRANSFER)){
            return doCheckAndLock(declarationDataIdList, operationType, SET_TRANSFER, additionalParams, userInfo, logger);
        } else {
            throw new IllegalArgumentException("Unknown operationType type!");
        }
    }

    private List<LockData> doCheckAndLock(final List<Long> declarationDataIdList, final OperationType currentTask, final Set<OperationType> lockingTasks, Map<String, Long> additionalParams, final TAUserInfo userinfo, final Logger logger) {
        if (declarationDataIdList == null) {
            return new ArrayList<>();
        }
        final Map<String, Long> declarationDataByLockKeys = new HashMap<>();
        final Map<String, Long> declarationDataByCurrentLockKeys = new HashMap<>();
        final Map<String, String> currentLockKeysWithDescription = new HashMap<>();

        for (Long declarationDataId : declarationDataIdList) {
            try {
                Map<String, Long> idTokens = new HashMap<>();
                idTokens.put("declarationDataId", declarationDataId);
                if (additionalParams != null) {
                    idTokens.put("sourceDeclarationId", additionalParams.get("sourceDeclarationId"));
                }
                String currLockKey = mainLockKeyGenerator.generateLockKey(idTokens, currentTask);
                declarationDataByCurrentLockKeys.put(currLockKey, declarationDataId);
                currentLockKeysWithDescription.put(currLockKey, declarationDataKeyLockDescriptor.createKeyLockDescription(declarationDataId, currentTask));
                for (OperationType lockingTask : lockingTasks) {
                    declarationDataByLockKeys.put(checkupLockKeyGenerator.generateLockKey(idTokens, lockingTask), declarationDataId);
                }
            } catch (Exception e) {
                DeclarationData declarationData = declarationDataService.get(Collections.singletonList(declarationDataId)).get(0);
                DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
                final DeclarationType declarationType = declarationTemplate.getType();
                final DeclarationFormKind declarationFormKind = declarationTemplate.getDeclarationFormKind();
                e.printStackTrace();
                logger.error("Не удалось установить блокировку на форму № %s, Вид: \"%s\", Тип: \"%s\". Причина: %s",
                        declarationDataId,
                        declarationType.getName(),
                        declarationFormKind.getName(),
                        e.toString());
                return null;
            }
        }

        return tx.executeInNewTransaction(new TransactionLogic<List<LockData>>() {
            @Override
            public List<LockData> execute() {
                List<LockData> locks = new ArrayList<LockData>(lockDataDao.fetchAllByKeyPrefixSet(declarationDataByLockKeys.keySet()));
                locks.addAll(lockDataDao.fetchAllByKeySet(declarationDataByCurrentLockKeys.keySet()));
                if (!locks.isEmpty()) {
                    LockData lock = locks.get(0);
                    DeclarationData declarationData = null;
                    for (String lockKey : declarationDataByLockKeys.keySet()) {
                        if (lock.getKey().startsWith(lockKey)) {
                            declarationData = declarationDataService.get(Collections.singletonList(declarationDataByLockKeys.get(lockKey))).get(0);
                            break;
                        }
                    }
                    if (declarationData == null) {
                        declarationData = declarationDataService.get(Collections.singletonList(declarationDataByCurrentLockKeys.get(lock.getKey()))).get(0);
                    }
                    DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
                    DeclarationType declarationType = declarationTemplate.getType();
                    DeclarationFormKind declarationFormKind = declarationTemplate.getDeclarationFormKind();
                    if (declarationDataByCurrentLockKeys.keySet().contains(lock.getKey())) {
                        if (lock.getUserId() == userinfo.getUser().getId()) {
                            logger.error("Форма № %s, Вид: \"%s\", Тип: \"%s\" заблокирована вами в рамках операции %s",
                                    declarationData.getId(),
                                    declarationType.getName(),
                                    declarationFormKind.getName(),
                                    currentTask.getName());
                        } else {
                            TAUser user = taUserService.getUser(lock.getUserId());
                            logger.error("Форма № %s, Вид: \"%s\", Тип: \"%s\" заблокирована пользователем: %s (%s) в рамках операции %s",
                                    declarationData.getId(),
                                    declarationType.getName(),
                                    declarationFormKind.getName(),
                                    user.getName(),
                                    user.getLogin(),
                                    currentTask.getName());
                        }
                        return null;
                    } else {
                        logger.error("Форма № %s, Вид: \"%s\", Тип: \"%s\" заблокирована в рамках уже запущенных операций.",
                                declarationData.getId(),
                                declarationType.getName(),
                                declarationFormKind.getName());
                        return null;
                    }
                } else {
                    lockDataDao.lockKeysBatch(currentLockKeysWithDescription, userinfo.getUser().getId());
                    List<LockData> result = lockDataDao.fetchAllByKeySet(currentLockKeysWithDescription.keySet());
                    return result;
                }
            }
        });
    }


    @Override
    public void unlock(final Long declarationDataId, final OperationType operationType, final Map<String, Long> additionalParams, Logger logger) {
        try {
            tx.executeInNewTransaction(new TransactionLogic<Boolean>() {
                @Override
                public Boolean execute() {
                    Map<String, Long> idTokens = new HashMap<>();
                    idTokens.put("declarationDataId", declarationDataId);
                    if (additionalParams != null) {
                        idTokens.put("sourceDeclarationId", additionalParams.get("sourceDeclarationId"));
                    }
                    String lockKey = mainLockKeyGenerator.generateLockKey(idTokens, operationType);
                    lockDataDao.unlock(lockKey);
                    return true;
                }
            });
        } catch (Exception e) {
            logger.error("При удалении блокировки возникла системная ошибка. Удаление блокировки невозможно. Обратитесь за разъяснениями к Администратору.");
        }
    }

    @Override
    public boolean lockExists(Long declarationDataId, OperationType operationType, Map<String, Long> additionalParams, TAUserInfo userInfo) {
        Map<String, Long> idTokens = new HashMap<>();
        idTokens.put("declarationDataId", declarationDataId);
        if (additionalParams != null) {
            idTokens.put("sourceDeclarationId", additionalParams.get("sourceDeclarationId"));
        }
        String lockKey = mainLockKeyGenerator.generateLockKey(idTokens, operationType);
        int userId = userInfo.getUser().getId();
        return lockDataDao.existsByKeyAndUserId(lockKey, userId);
    }
}

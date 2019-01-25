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
import com.aplana.sbrf.taxaccounting.service.component.lock.DeclarationDataLockKeyGenerator;
import com.aplana.sbrf.taxaccounting.service.component.lock.descriptor.DeclarationDataKeyLockDescriptor;
import com.aplana.sbrf.taxaccounting.service.component.lock.locker.DeclarationLocker;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component("declarationLocker")
public class DeclarationLockerImpl implements DeclarationLocker {

    private static final Log LOG = LogFactory.getLog(DeclarationLockerImpl.class);

    private static final Set<OperationType> SET_IMPORT_TF__IMPORT_EXCEL__IDENTIFY = new HashSet<>();
    private static final Set<OperationType> SET_UPDATE_PERSONS_DATA = new HashSet<>();
    private static final Set<OperationType> SET_CHECK__ACCEPT__TOCREATE = new HashSet<>();
    private static final Set<OperationType> SET_EDIT = new HashSet<>();
    private static final Set<OperationType> SET_CONSOLIDATE__REPORT_KPP_OKTMO__2NDFL1__2NDFL2__6NDFL = new HashSet<>();
    private static final Set<OperationType> SET_EDIT_FILE__PDF__EXPORT_REPORTS__REPORT_2NDFL1__REPORT_2NDFL2__DEPT_NOTICE = new HashSet<>();
    private static final Set<OperationType> SET_DELETE = new HashSet<>();
    private static final Set<OperationType> SET_XLSX = new HashSet<>();
    private static final Set<OperationType> SET_SPEC_REPORT = new HashSet<>();
    private static final Set<OperationType> SET_EXCEL_TEMPLATE = new HashSet<>();
    private static final Set<OperationType> SET_REPORT_KPP_OKTMO = new HashSet<>();
    private static final Set<OperationType> SET_UPDATE_DOC_STATE = new HashSet<>();

    static {
        SET_IMPORT_TF__IMPORT_EXCEL__IDENTIFY.addAll(Arrays.asList(OperationType.UPDATE_PERSONS_DATA,
                OperationType.CHECK_DEC, OperationType.ACCEPT_DEC, OperationType.RETURN_DECLARATION,
                OperationType.DELETE_DEC, OperationType.EXCEL_DEC, OperationType.RNU_NDFL_PERSON_DB,
                OperationType.RNU_NDFL_PERSON_ALL_DB, OperationType.EXCEL_TEMPLATE_DEC,
                OperationType.RNU_PAYMENT_REPORT, OperationType.RNU_RATE_REPORT, OperationType.RNU_NDFL_DETAIL_REPORT,
                OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT, OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT));

        SET_UPDATE_PERSONS_DATA.addAll(Arrays.asList(OperationType.ACCEPT_DEC, OperationType.CHECK_DEC, OperationType.CONSOLIDATE,
                OperationType.DELETE_DEC, OperationType.EDIT, OperationType.IDENTIFY_PERSON,
                OperationType.LOAD_TRANSPORT_FILE, OperationType.REPORT_KPP_OKTMO, OperationType.RETURN_DECLARATION,
                OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT, OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT,
                OperationType.RNU_NDFL_DETAIL_REPORT, OperationType.RNU_NDFL_PERSON_DB,
                OperationType.RNU_NDFL_PERSON_ALL_DB, OperationType.RNU_PAYMENT_REPORT, OperationType.RNU_RATE_REPORT,
                OperationType.EXCEL_DEC, OperationType.EXCEL_TEMPLATE_DEC, OperationType.DECLARATION_2NDFL1, OperationType.DECLARATION_2NDFL2,
                OperationType.DECLARATION_6NDFL, OperationType.IMPORT_DECLARATION_EXCEL));

        SET_CHECK__ACCEPT__TOCREATE.addAll(Arrays.asList(OperationType.ACCEPT_DEC, OperationType.CHECK_DEC,
                OperationType.CONSOLIDATE, OperationType.DELETE_DEC, OperationType.EDIT, OperationType.IDENTIFY_PERSON,
                OperationType.LOAD_TRANSPORT_FILE, OperationType.RETURN_DECLARATION,
                OperationType.UPDATE_PERSONS_DATA, OperationType.IMPORT_DECLARATION_EXCEL, OperationType.UPDATE_DOC_STATE));

        SET_EDIT.addAll(Arrays.asList(OperationType.ACCEPT_DEC, OperationType.CHECK_DEC, OperationType.CONSOLIDATE,
                OperationType.DELETE_DEC, OperationType.REPORT_KPP_OKTMO, OperationType.RETURN_DECLARATION,
                OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT, OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT, OperationType.RNU_NDFL_DETAIL_REPORT,
                OperationType.RNU_NDFL_PERSON_DB, OperationType.RNU_NDFL_PERSON_ALL_DB,
                OperationType.RNU_PAYMENT_REPORT, OperationType.RNU_RATE_REPORT, OperationType.UPDATE_PERSONS_DATA,
                OperationType.EXCEL_DEC, OperationType.DECLARATION_2NDFL1, OperationType.DECLARATION_2NDFL2,
                OperationType.DECLARATION_6NDFL));
        SET_CONSOLIDATE__REPORT_KPP_OKTMO__2NDFL1__2NDFL2__6NDFL.addAll(Arrays.asList(OperationType.CONSOLIDATE, OperationType.DELETE_DEC, OperationType.EDIT, OperationType.UPDATE_PERSONS_DATA));
        SET_EDIT_FILE__PDF__EXPORT_REPORTS__REPORT_2NDFL1__REPORT_2NDFL2__DEPT_NOTICE.addAll(Arrays.asList(OperationType.DELETE_DEC));
        SET_DELETE.addAll(Arrays.asList(OperationType.ACCEPT_DEC, OperationType.CHECK_DEC, OperationType.CONSOLIDATE,
                OperationType.DELETE_DEC, OperationType.DEPT_NOTICE_DEC, OperationType.EDIT, OperationType.EDIT_FILE,
                OperationType.IDENTIFY_PERSON, OperationType.LOAD_TRANSPORT_FILE, OperationType.PDF_DEC,
                OperationType.REPORT_2NDFL1, OperationType.REPORT_2NDFL2, OperationType.REPORT_KPP_OKTMO,
                OperationType.RETURN_DECLARATION, OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT,
                OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT, OperationType.RNU_NDFL_DETAIL_REPORT,
                OperationType.RNU_NDFL_PERSON_DB, OperationType.RNU_NDFL_PERSON_ALL_DB,
                OperationType.RNU_PAYMENT_REPORT, OperationType.RNU_RATE_REPORT, OperationType.UPDATE_PERSONS_DATA,
                OperationType.EXCEL_DEC, OperationType.DECLARATION_2NDFL1, OperationType.DECLARATION_2NDFL2,
                OperationType.DECLARATION_6NDFL, OperationType.EXCEL_TEMPLATE_DEC, OperationType.EXPORT_REPORTS,
                OperationType.IMPORT_DECLARATION_EXCEL, OperationType.UPDATE_DOC_STATE));
        SET_XLSX.addAll(Arrays.asList(OperationType.IDENTIFY_PERSON, OperationType.LOAD_TRANSPORT_FILE, OperationType.IMPORT_DECLARATION_EXCEL));
        SET_SPEC_REPORT.addAll(Arrays.asList(OperationType.CONSOLIDATE, OperationType.DELETE_DEC,
                OperationType.EDIT, OperationType.IDENTIFY_PERSON, OperationType.LOAD_TRANSPORT_FILE,
                OperationType.UPDATE_PERSONS_DATA, OperationType.IMPORT_DECLARATION_EXCEL));
        SET_EXCEL_TEMPLATE.addAll(Arrays.asList(OperationType.DELETE_DEC, OperationType.IDENTIFY_PERSON,
                OperationType.LOAD_TRANSPORT_FILE, OperationType.UPDATE_PERSONS_DATA, OperationType.IMPORT_DECLARATION_EXCEL));
        SET_UPDATE_DOC_STATE.addAll(Arrays.asList(OperationType.ACCEPT_DEC, OperationType.CHECK_DEC,
                OperationType.DELETE_DEC, OperationType.RETURN_DECLARATION));
    }

    // Зависимости
    private final DeclarationDataLockKeyGenerator simpleDeclarationDataLockKeyGenerator;
    private final DeclarationDataKeyLockDescriptor declarationDataKeyLockDescriptor;
    private final LockDataDao lockDataDao;
    private final DeclarationDataService declarationDataService;
    private final DeclarationTemplateService declarationTemplateService;
    private final TAUserService taUserService;
    private final TransactionHelper tx;

    @Autowired
    public DeclarationLockerImpl(DeclarationDataLockKeyGenerator simpleDeclarationDataLockKeyGenerator,
                                 DeclarationDataKeyLockDescriptor declarationDataKeyLockDescriptor,
                                 LockDataDao lockDataDao,
                                 DeclarationDataService declarationDataService,
                                 DeclarationTemplateService declarationTemplateService,
                                 TAUserService taUserService,
                                 TransactionHelper tx) {
        this.simpleDeclarationDataLockKeyGenerator = simpleDeclarationDataLockKeyGenerator;
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
        try {
            if (operationType.equals(OperationType.LOAD_TRANSPORT_FILE))
                return doCheckAndLock(declarationDataIdList, operationType, SET_IMPORT_TF__IMPORT_EXCEL__IDENTIFY, userInfo, logger);
            else if (operationType.equals(OperationType.IMPORT_DECLARATION_EXCEL))
                return doCheckAndLock(declarationDataIdList, operationType, SET_IMPORT_TF__IMPORT_EXCEL__IDENTIFY, userInfo, logger);
            else if (operationType.equals(OperationType.IDENTIFY_PERSON))
                return doCheckAndLock(declarationDataIdList, operationType, SET_IMPORT_TF__IMPORT_EXCEL__IDENTIFY, userInfo, logger);
            else if (operationType.equals(OperationType.UPDATE_PERSONS_DATA))
                return doCheckAndLock(declarationDataIdList, operationType, SET_UPDATE_PERSONS_DATA, userInfo, logger);
            else if (operationType.equals(OperationType.CHECK_DEC))
                return doCheckAndLock(declarationDataIdList, operationType, SET_CHECK__ACCEPT__TOCREATE, userInfo, logger);
            else if (operationType.equals(OperationType.ACCEPT_DEC))
                return doCheckAndLock(declarationDataIdList, operationType, SET_CHECK__ACCEPT__TOCREATE, userInfo, logger);
            else if (operationType.equals(OperationType.DELETE_DEC))
                return doCheckAndLock(declarationDataIdList, operationType, SET_DELETE, userInfo, logger);
            else if (operationType.equals(OperationType.CONSOLIDATE))
                return doCheckAndLock(declarationDataIdList, operationType, SET_CONSOLIDATE__REPORT_KPP_OKTMO__2NDFL1__2NDFL2__6NDFL, userInfo, logger);
            else if (operationType.equals(OperationType.EXCEL_DEC))
                return doCheckAndLock(declarationDataIdList, operationType, SET_XLSX, userInfo, logger);
            else if (operationType.equals(OperationType.EXCEL_TEMPLATE_DEC))
                return doCheckAndLock(declarationDataIdList, operationType, SET_EXCEL_TEMPLATE, userInfo, logger);
            else if (operationType.equals(OperationType.PDF_DEC))
                return doCheckAndLock(declarationDataIdList, operationType, SET_EDIT_FILE__PDF__EXPORT_REPORTS__REPORT_2NDFL1__REPORT_2NDFL2__DEPT_NOTICE, userInfo, logger);
            else if (operationType.equals(OperationType.DEPT_NOTICE_DEC))
                return doCheckAndLock(declarationDataIdList, operationType, SET_EDIT_FILE__PDF__EXPORT_REPORTS__REPORT_2NDFL1__REPORT_2NDFL2__DEPT_NOTICE, userInfo, logger);
            else if (operationType.equals(OperationType.RETURN_DECLARATION))
                return doCheckAndLock(declarationDataIdList, operationType, SET_CHECK__ACCEPT__TOCREATE, userInfo, logger);
            else if (operationType.equals(OperationType.EDIT))
                return doCheckAndLock(declarationDataIdList, operationType, SET_EDIT, userInfo, logger);
            else if (operationType.equals(OperationType.EDIT_FILE))
                return doCheckAndLock(declarationDataIdList, operationType, SET_EDIT_FILE__PDF__EXPORT_REPORTS__REPORT_2NDFL1__REPORT_2NDFL2__DEPT_NOTICE, userInfo, logger);
            else if (operationType.equals(OperationType.RNU_NDFL_PERSON_DB))
                return doCheckAndLock(declarationDataIdList, operationType, SET_SPEC_REPORT, userInfo, logger);
            else if (operationType.equals(OperationType.RNU_NDFL_PERSON_ALL_DB))
                return doCheckAndLock(declarationDataIdList, operationType, SET_SPEC_REPORT, userInfo, logger);
            else if (operationType.equals(OperationType.REPORT_KPP_OKTMO))
                return doCheckAndLock(declarationDataIdList, operationType, SET_REPORT_KPP_OKTMO, userInfo, logger);
            else if (operationType.equals(OperationType.RNU_RATE_REPORT))
                return doCheckAndLock(declarationDataIdList, operationType, SET_SPEC_REPORT, userInfo, logger);
            else if (operationType.equals(OperationType.RNU_PAYMENT_REPORT))
                return doCheckAndLock(declarationDataIdList, operationType, SET_SPEC_REPORT, userInfo, logger);
            else if (operationType.equals(OperationType.RNU_NDFL_DETAIL_REPORT))
                return doCheckAndLock(declarationDataIdList, operationType, SET_SPEC_REPORT, userInfo, logger);
            else if (operationType.equals(OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT))
                return doCheckAndLock(declarationDataIdList, operationType, SET_SPEC_REPORT, userInfo, logger);
            else if (operationType.equals(OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT))
                return doCheckAndLock(declarationDataIdList, operationType, SET_SPEC_REPORT, userInfo, logger);
            else if (operationType.equals(OperationType.REPORT_2NDFL1))
                return doCheckAndLock(declarationDataIdList, operationType, SET_SPEC_REPORT, userInfo, logger);
            else if (operationType.equals(OperationType.REPORT_2NDFL2))
                return doCheckAndLock(declarationDataIdList, operationType, SET_SPEC_REPORT, userInfo, logger);
            else if (operationType.equals(OperationType.DECLARATION_2NDFL1)) {
                return doCheckAndLock(declarationDataIdList, operationType, SET_CONSOLIDATE__REPORT_KPP_OKTMO__2NDFL1__2NDFL2__6NDFL, userInfo, logger);
            } else if (operationType.equals(OperationType.DECLARATION_2NDFL2)) {
                return doCheckAndLock(declarationDataIdList, operationType, SET_CONSOLIDATE__REPORT_KPP_OKTMO__2NDFL1__2NDFL2__6NDFL, userInfo, logger);
            } else if (operationType.equals(OperationType.DECLARATION_6NDFL)) {
                return doCheckAndLock(declarationDataIdList, operationType, SET_CONSOLIDATE__REPORT_KPP_OKTMO__2NDFL1__2NDFL2__6NDFL, userInfo, logger);
            } else if (operationType.equals(OperationType.EXPORT_REPORTS)) {
                return doCheckAndLock(declarationDataIdList, operationType, SET_EDIT_FILE__PDF__EXPORT_REPORTS__REPORT_2NDFL1__REPORT_2NDFL2__DEPT_NOTICE, userInfo, logger);
            } else if (operationType.equals(OperationType.UPDATE_DOC_STATE)) {
                return doCheckAndLock(declarationDataIdList, operationType, SET_UPDATE_DOC_STATE, userInfo, logger);
            } else
                throw new IllegalArgumentException("Unknown operationType type!");
        } catch (Exception e) {
            LOG.error(String.format("Выполнение операции невозможно по техническим причинам. Не удалось установить блокировку для выполнения операции \"%s\"", operationType.getName()), e);
            logger.error("Выполнение операции невозможно по техническим причинам. Не удалось установить блокировку для выполнения операции \"%s\"",
                    operationType.getName());
            return null;
        }
    }

    private List<LockData> doCheckAndLock(final List<Long> declarationDataIdList, final OperationType currentTask, Set<OperationType> lockingTasks, final TAUserInfo userinfo, final Logger logger) {
        final Map<String, Long> declarationDataByLockKeys = new HashMap<>();
        final Map<String, Long> declarationDataByCurrentLockKeys = new HashMap<>();
        final Map<String, String> currentLockKeysWithDescription = new HashMap<>();

        lockingTasks.add(currentTask);
        for (Long declarationDataId : declarationDataIdList) {
            String currLockKey = simpleDeclarationDataLockKeyGenerator.generateLockKey(declarationDataId, currentTask);
            declarationDataByCurrentLockKeys.put(currLockKey, declarationDataId);
            currentLockKeysWithDescription.put(currLockKey, declarationDataKeyLockDescriptor.createKeyLockDescription(declarationDataId, currentTask));
            for (OperationType lockingTask : lockingTasks) {
                declarationDataByLockKeys.put(simpleDeclarationDataLockKeyGenerator.generateLockKey(declarationDataId, lockingTask), declarationDataId);
            }
        }
        lockingTasks.remove(currentTask);

        return tx.executeInNewTransaction(new TransactionLogic<List<LockData>>() {
            @Override
            public List<LockData> execute() {
                List<LockData> locks = lockDataDao.fetchAllByKeySet(declarationDataByLockKeys.keySet());
                if (!locks.isEmpty()) {
                    for (LockData lock : locks) {
                        DeclarationData declarationData = declarationDataService.get(Collections.singletonList(declarationDataByLockKeys.get(lock.getKey()))).get(0);
                        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
                        final DeclarationType declarationType = declarationTemplate.getType();
                        final DeclarationFormKind declarationFormKind = declarationTemplate.getDeclarationFormKind();
                        if (declarationDataByCurrentLockKeys.keySet().contains(lock.getKey())) {
                            if (lock.getUserId() == userinfo.getUser().getId()) {

                                logger.error("Данная форма заблокирована. Налоговая форма Вид: \"%s\", Тип: \"%s\" номер формы %s вами уже запущена операция %s",
                                        declarationType.getName(),
                                        declarationFormKind.getName(),
                                        declarationData.getId(),
                                        currentTask.getName());
                            } else {
                                TAUser user = taUserService.getUser(lock.getUserId());
                                logger.error("Данная форма заблокирована. Налоговая форма  Вид: \"%s\", Тип: \"%s\" номер формы %s пользователем: %s (%s)  уже запущена операция %s",
                                        declarationType.getName(),
                                        declarationFormKind.getName(),
                                        declarationData.getId(),
                                        user.getName(),
                                        user.getLogin(),
                                        currentTask.getName());
                            }
                        } else {
                            logger.error("Данная форма заблокирована. Налоговая форма Вид:\"%s\", Тип: \"%s\" номер формы %s уже запущены операции, блокирующие запуск операции %s",
                                    declarationType.getName(),
                                    declarationFormKind.getName(),
                                    declarationData.getId(),
                                    currentTask.getName());
                        }
                    }
                    return null;
                } else {
                    lockDataDao.lockKeysBatch(currentLockKeysWithDescription, userinfo.getUser().getId());
                    List<LockData> result = lockDataDao.fetchAllByKeySet(declarationDataByLockKeys.keySet());
                    if (result == null || result.isEmpty()) {
                        throw new IllegalStateException();
                    }
                    return result;
                }
            }
        });
    }


    @Override
    public void unlock(final Long declarationDataId, final OperationType operationType, Logger logger) {
        try {
            tx.executeInNewTransaction(new TransactionLogic<Boolean>() {
                @Override
                public Boolean execute() {
                    String lockKey = simpleDeclarationDataLockKeyGenerator.generateLockKey(declarationDataId, operationType);
                    lockDataDao.unlock(lockKey);
                    return true;
                }
            });
        } catch (Exception e) {
            logger.error("При удалении блокировки возникла системная ошибка. Удаление блокировки невозможно. Обратитесь за разъяснениями к Администратору.");
        }
    }

    @Override
    public boolean lockExists(Long declarationDataId, OperationType operationType, TAUserInfo userInfo) {
        String lockKey = simpleDeclarationDataLockKeyGenerator.generateLockKey(declarationDataId, operationType);
        int userId = userInfo.getUser().getId();
        return lockDataDao.existsByKeyAndUserId(lockKey, userId);
    }
}

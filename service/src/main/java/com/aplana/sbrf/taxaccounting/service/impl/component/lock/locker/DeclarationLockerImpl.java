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
import com.aplana.sbrf.taxaccounting.service.component.lock.descriptor.DeclarationDataKeyLockDescriptor;
import com.aplana.sbrf.taxaccounting.service.component.lock.locker.DeclarationLocker;
import com.aplana.sbrf.taxaccounting.service.component.lock.DeclarationDataLockKeyGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DeclarationLockerImpl implements DeclarationLocker {

    private static final Log LOG = LogFactory.getLog(DeclarationLockerImpl.class);

    @Autowired
    private DeclarationDataLockKeyGenerator simpleDeclarationDataLockKeyGenerator;
    @Autowired
    private DeclarationDataKeyLockDescriptor declarationDataKeyLockDescriptor;
    @Autowired
    private LockDataDao lockDataDao;
    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private TAUserService taUserService;
    @Autowired
    private TransactionHelper tx;

    private final Set<OperationType> SET_IMPORT_TF__IMPORT_EXCEL__IDENTIFY = new HashSet<>();
    private final Set<OperationType> SET_UPDATE_PERSONS_DATA = new HashSet<>();
    private final Set<OperationType> SET_CHECK__ACCEPT__TOCREATE = new HashSet<>();
    private final Set<OperationType> SET_EDIT = new HashSet<>();
    private final Set<OperationType> SET_CONSOLIDATE__REPORT_KPP_OKTMO__2NDFL1__2NDFL2__6NDFL = new HashSet<>();
    private final Set<OperationType> SET_EDIT_FILE__PDF__EXPORT_REPORTS__REPORT_2NDFL1__REPORT_2NDFL2__DEPT_NOTICE = new HashSet<>();
    private final Set<OperationType> SET_DELETE = new HashSet<>();
    private final Set<OperationType> SET_XLSX = new HashSet<>();
    private final Set<OperationType> SET_SPEC_REPORT = new HashSet<>();
    private final Set<OperationType> SET_EXCEL_TEMPLATE = new HashSet<>();
    private final Set<OperationType> SET_REPORT_KPP_OKTMO = new HashSet<>();

    public DeclarationLockerImpl() {
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
                OperationType.UPDATE_PERSONS_DATA, OperationType.IMPORT_DECLARATION_EXCEL));

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
                OperationType.IMPORT_DECLARATION_EXCEL));
        SET_XLSX.addAll(Arrays.asList(OperationType.IDENTIFY_PERSON, OperationType.LOAD_TRANSPORT_FILE, OperationType.IMPORT_DECLARATION_EXCEL));
        SET_SPEC_REPORT.addAll(Arrays.asList(OperationType.CONSOLIDATE, OperationType.DELETE_DEC,
                OperationType.EDIT, OperationType.IDENTIFY_PERSON, OperationType.LOAD_TRANSPORT_FILE,
                OperationType.UPDATE_PERSONS_DATA, OperationType.IMPORT_DECLARATION_EXCEL));
        SET_EXCEL_TEMPLATE.addAll(Arrays.asList(OperationType.DELETE_DEC, OperationType.IDENTIFY_PERSON,
                OperationType.LOAD_TRANSPORT_FILE, OperationType.UPDATE_PERSONS_DATA, OperationType.IMPORT_DECLARATION_EXCEL));

    }

    @Override
    public LockData establishLock(Long declarationDataId, OperationType operationType, TAUserInfo userInfo, Logger logger) {
        if (operationType.equals(OperationType.LOAD_TRANSPORT_FILE))
            return doCheckAndLock(declarationDataId, operationType, SET_IMPORT_TF__IMPORT_EXCEL__IDENTIFY, userInfo, logger);
        else if (operationType.equals(OperationType.IMPORT_DECLARATION_EXCEL))
            return doCheckAndLock(declarationDataId, operationType, SET_IMPORT_TF__IMPORT_EXCEL__IDENTIFY, userInfo, logger);
        else if (operationType.equals(OperationType.IDENTIFY_PERSON))
            return doCheckAndLock(declarationDataId, operationType, SET_IMPORT_TF__IMPORT_EXCEL__IDENTIFY, userInfo, logger);
        else if (operationType.equals(OperationType.UPDATE_PERSONS_DATA))
            return doCheckAndLock(declarationDataId, operationType, SET_UPDATE_PERSONS_DATA, userInfo, logger);
        else if (operationType.equals(OperationType.CHECK_DEC))
            return doCheckAndLock(declarationDataId, operationType, SET_CHECK__ACCEPT__TOCREATE, userInfo, logger);
        else if (operationType.equals(OperationType.ACCEPT_DEC))
            return doCheckAndLock(declarationDataId, operationType, SET_CHECK__ACCEPT__TOCREATE, userInfo, logger);
        else if (operationType.equals(OperationType.DELETE_DEC))
            return doCheckAndLock(declarationDataId, operationType, SET_DELETE, userInfo, logger);
        else if (operationType.equals(OperationType.CONSOLIDATE))
            return doCheckAndLock(declarationDataId, operationType, SET_CONSOLIDATE__REPORT_KPP_OKTMO__2NDFL1__2NDFL2__6NDFL, userInfo, logger);
        else if (operationType.equals(OperationType.EXCEL_DEC))
            return doCheckAndLock(declarationDataId, operationType, SET_XLSX, userInfo, logger);
        else if (operationType.equals(OperationType.EXCEL_TEMPLATE_DEC))
            return doCheckAndLock(declarationDataId, operationType, SET_EXCEL_TEMPLATE, userInfo, logger);
        else if (operationType.equals(OperationType.PDF_DEC))
            return doCheckAndLock(declarationDataId, operationType, SET_EDIT_FILE__PDF__EXPORT_REPORTS__REPORT_2NDFL1__REPORT_2NDFL2__DEPT_NOTICE, userInfo, logger);
        else if (operationType.equals(OperationType.DEPT_NOTICE_DEC))
            return doCheckAndLock(declarationDataId, operationType, SET_EDIT_FILE__PDF__EXPORT_REPORTS__REPORT_2NDFL1__REPORT_2NDFL2__DEPT_NOTICE, userInfo, logger);
        else if (operationType.equals(OperationType.RETURN_DECLARATION))
            return doCheckAndLock(declarationDataId, operationType, SET_CHECK__ACCEPT__TOCREATE, userInfo, logger);
        else if (operationType.equals(OperationType.EDIT))
            return doCheckAndLock(declarationDataId, operationType, SET_EDIT, userInfo, logger);
        else if (operationType.equals(OperationType.EDIT_FILE))
            return doCheckAndLock(declarationDataId, operationType, SET_EDIT_FILE__PDF__EXPORT_REPORTS__REPORT_2NDFL1__REPORT_2NDFL2__DEPT_NOTICE, userInfo, logger);
        else if (operationType.equals(OperationType.RNU_NDFL_PERSON_DB))
            return doCheckAndLock(declarationDataId, operationType, SET_SPEC_REPORT, userInfo, logger);
        else if (operationType.equals(OperationType.RNU_NDFL_PERSON_ALL_DB))
            return doCheckAndLock(declarationDataId, operationType, SET_SPEC_REPORT, userInfo, logger);
        else if (operationType.equals(OperationType.REPORT_KPP_OKTMO))
            return doCheckAndLock(declarationDataId, operationType, SET_REPORT_KPP_OKTMO, userInfo, logger);
        else if (operationType.equals(OperationType.RNU_RATE_REPORT))
            return doCheckAndLock(declarationDataId, operationType, SET_SPEC_REPORT, userInfo, logger);
        else if (operationType.equals(OperationType.RNU_PAYMENT_REPORT))
            return doCheckAndLock(declarationDataId, operationType, SET_SPEC_REPORT, userInfo, logger);
        else if (operationType.equals(OperationType.RNU_NDFL_DETAIL_REPORT))
            return doCheckAndLock(declarationDataId, operationType, SET_SPEC_REPORT, userInfo, logger);
        else if (operationType.equals(OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT))
            return doCheckAndLock(declarationDataId, operationType, SET_SPEC_REPORT, userInfo, logger);
        else if (operationType.equals(OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT))
            return doCheckAndLock(declarationDataId, operationType, SET_SPEC_REPORT, userInfo, logger);
        else if (operationType.equals(OperationType.REPORT_2NDFL1))
            return doCheckAndLock(declarationDataId, operationType, SET_SPEC_REPORT, userInfo, logger);
        else if (operationType.equals(OperationType.REPORT_2NDFL2))
            return doCheckAndLock(declarationDataId, operationType, SET_SPEC_REPORT, userInfo, logger);
        else
            throw new IllegalArgumentException("Unknown operationType type!");
    }

    private LockData doCheckAndLock(final Long declarationDataId, final OperationType currentTask, Set<OperationType> lockingTasks, final TAUserInfo userinfo, final Logger logger) {
        lockingTasks.add(currentTask);
        final Set<String> lockKeys = new HashSet<>();
        final String currTaskKey = simpleDeclarationDataLockKeyGenerator.generateLockKey(declarationDataId, currentTask);
        for (OperationType lockingTask : lockingTasks) {
            lockKeys.add(simpleDeclarationDataLockKeyGenerator.generateLockKey(declarationDataId, lockingTask));
        }

        DeclarationData declarationData = declarationDataService.get(Collections.singletonList(declarationDataId)).get(0);
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        final DeclarationType declarationType = declarationTemplate.getType();
        final DeclarationFormKind declarationFormKind = declarationTemplate.getDeclarationFormKind();

        return tx.executeInNewTransaction(new TransactionLogic<LockData>() {
            @Override
            public LockData execute() {
                List<LockData> locks = lockDataDao.fetchAllByKeySet(lockKeys);
                if (!locks.isEmpty()) {
                    LOG.info(String.format("Найдены запущенные задачи, по которым требуется удалить блокировку для задачи с ключом %s", currTaskKey));
                    for (LockData lock : locks) {
                        if (lock.getKey().equals(currTaskKey)) {
                            if (lock.getUserId() == userinfo.getUser().getId()) {
                                logger.error("Данная форма заблокирована. Налоговая форма Вид: \"%s\", Тип: \"%s\" номер формы %s вами уже запущена операция %s",
                                        declarationType.getName(),
                                        declarationFormKind.getName(),
                                        declarationDataId,
                                        currentTask.getName());
                            } else {
                                TAUser user = taUserService.getUser(lock.getUserId());
                                logger.error("Данная форма заблокирована. Налоговая форма  Вид: \"%s\", Тип: \"%s\" номер формы %s пользователем: %s (%s)  уже запущена операция %s",
                                        declarationType.getName(),
                                        declarationFormKind.getName(),
                                        declarationDataId,
                                        user.getName(),
                                        user.getLogin(),
                                        currentTask.getName());
                            }
                        } else {
                            logger.error("Данная форма заблокирована. Налоговая форма Вид:\"%s\", Тип: \"%s\" номер формы %s уже запущены операции, блокирующие запуск операции %s",
                                    declarationType.getName(),
                                    declarationFormKind.getName(),
                                    declarationDataId,
                                    currentTask.getName());
                        }
                    }
                    return null;
                } else {
                    LOG.info(String.format("Создание блокировки для задачи с ключом %s", currTaskKey));
                    LockData result = null;
                    try {
                        lockDataDao.lock(currTaskKey, userinfo.getUser().getId(), declarationDataKeyLockDescriptor.createKeyLockDescription(declarationDataId, currentTask));
                        result = lockDataDao.get(currTaskKey, false);
                        if (result == null) {
                            throw new IllegalStateException();
                        }
                    } catch (Exception e) {
                        logger.error("Выполнение операции не возможно по техническим причинам. Не удалось установить блокировку для выполнения операции %s налоговая форма Вид:\"%s\", Тип: \"%s\" номер формы %s",
                                currentTask.getName(),
                                declarationType.getName(),
                                declarationFormKind.getName(),
                                declarationDataId);
                    }
                    return result;
                }
            }
        });
    }
}

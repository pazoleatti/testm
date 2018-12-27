package com.aplana.sbrf.taxaccounting.service.impl.component;

import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.OperationType;
import com.aplana.sbrf.taxaccounting.model.LockTaskType;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.component.DeclarationProhibitiveLockExistsVerifier;
import com.aplana.sbrf.taxaccounting.service.component.SimpleDeclarationDataLockKeyGenerator;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class DeclarationProhibitiveLockExistsVerifierImpl implements DeclarationProhibitiveLockExistsVerifier {

    private SimpleDeclarationDataLockKeyGenerator simpleDeclarationDataLockKeyGenerator;
    private LockDataService lockDataService;
    private DeclarationDataService declarationDataService;

    public DeclarationProhibitiveLockExistsVerifierImpl(SimpleDeclarationDataLockKeyGenerator simpleDeclarationDataLockKeyGenerator, LockDataService lockDataService, DeclarationDataService declarationDataService) {
        this.simpleDeclarationDataLockKeyGenerator = simpleDeclarationDataLockKeyGenerator;
        this.lockDataService = lockDataService;
        this.declarationDataService = declarationDataService;
    }

    private final Set<LockTaskType> IMPORT_TF__IMPORT_EXCEL__IDENTIFY = new HashSet<>();
    private final Set<LockTaskType> SET_UPDATE_PERSONS_DATA = new HashSet<>();
    private final Set<LockTaskType> SET_CHECK__ACCEPT__TOCREATE = new HashSet<>();
    private final Set<LockTaskType> SET_EDIT = new HashSet<>();
    private final Set<LockTaskType> SET_CONSOLIDATE = new HashSet<>();
    private final Set<LockTaskType> SET_TODO = new HashSet<>();

    public DeclarationProhibitiveLockExistsVerifierImpl() {
        IMPORT_TF__IMPORT_EXCEL__IDENTIFY.addAll(Arrays.asList(AsyncTaskType.IMPORT_DECLARATION_EXCEL, AsyncTaskType.IDENTIFY_PERSON, AsyncTaskType.IMPORT_TF_DEC,
                AsyncTaskType.UPDATE_PERSONS_DATA, AsyncTaskType.CHECK_DEC, AsyncTaskType.ACCEPT_DEC,
                OperationType.RETURN_DECLARATION, AsyncTaskType.DELETE_DEC, AsyncTaskType.EXCEL_DEC,
                OperationType.RNU_NDFL_PERSON_DB, OperationType.RNU_NDFL_PERSON_ALL_DB,
                AsyncTaskType.EXCEL_TEMPLATE_DEC, OperationType.RNU_PAYMENT_REPORT, OperationType.RNU_RATE_REPORT,
                OperationType.RNU_NDFL_DETAIL_REPORT, OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT,
                OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT));

        SET_UPDATE_PERSONS_DATA.addAll(Arrays.asList(AsyncTaskType.ACCEPT_DEC, AsyncTaskType.CHECK_DEC, AsyncTaskType.CONSOLIDATE,
                AsyncTaskType.DELETE_DEC, OperationType.EDIT, AsyncTaskType.IDENTIFY_PERSON,
                AsyncTaskType.IMPORT_TF_DEC, OperationType.REPORT_KPP_OKTMO, OperationType.RETURN_DECLARATION,
                OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT, OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT,
                OperationType.RNU_NDFL_DETAIL_REPORT, OperationType.RNU_NDFL_PERSON_DB,
                OperationType.RNU_NDFL_PERSON_ALL_DB, OperationType.RNU_PAYMENT_REPORT, OperationType.RNU_RATE_REPORT,
                AsyncTaskType.EXCEL_DEC, AsyncTaskType.EXCEL_TEMPLATE_DEC, AsyncTaskType.IMPORT_DECLARATION_EXCEL,
                AsyncTaskType.UPDATE_PERSONS_DATA));

        SET_CHECK__ACCEPT__TOCREATE.addAll(Arrays.asList(AsyncTaskType.ACCEPT_DEC, AsyncTaskType.CHECK_DEC, AsyncTaskType.CONSOLIDATE, AsyncTaskType.DELETE_DEC,
                OperationType.EDIT, AsyncTaskType.IDENTIFY_PERSON, AsyncTaskType.IMPORT_TF_DEC, OperationType.RETURN_DECLARATION,
                AsyncTaskType.UPDATE_PERSONS_DATA, AsyncTaskType.IMPORT_DECLARATION_EXCEL));

        SET_EDIT.addAll(Arrays.asList(AsyncTaskType.ACCEPT_DEC, AsyncTaskType.CHECK_DEC, AsyncTaskType.CONSOLIDATE,
                AsyncTaskType.DELETE_DEC, OperationType.EDIT, OperationType.REPORT_KPP_OKTMO, OperationType.RETURN_DECLARATION,
                OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT, OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT, OperationType.RNU_NDFL_DETAIL_REPORT,
                OperationType.RNU_NDFL_PERSON_DB, OperationType.RNU_NDFL_PERSON_ALL_DB,
                OperationType.RNU_PAYMENT_REPORT, OperationType.RNU_RATE_REPORT, AsyncTaskType.UPDATE_PERSONS_DATA,
                AsyncTaskType.EXCEL_DEC));
        SET_CONSOLIDATE.addAll(Arrays.asList(AsyncTaskType.CONSOLIDATE, AsyncTaskType.DELETE_DEC, OperationType.EDIT, AsyncTaskType.UPDATE_PERSONS_DATA));
    }

    @Override
    public boolean verify(Long declarationDataId, LockTaskType task, Logger logger) {
        if (task.equals(AsyncTaskType.LOAD_TRANSPORT_FILE))
            return executeCheck(declarationDataId, SET_TODO, logger); //TODO
        else if (task.equals(AsyncTaskType.IMPORT_DECLARATION_EXCEL))
            return executeCheck(declarationDataId, IMPORT_TF__IMPORT_EXCEL__IDENTIFY, logger);
        else if (task.equals(AsyncTaskType.IDENTIFY_PERSON))
            return executeCheck(declarationDataId, IMPORT_TF__IMPORT_EXCEL__IDENTIFY, logger);
        else if (task.equals(AsyncTaskType.UPDATE_PERSONS_DATA))
            return executeCheck(declarationDataId, SET_UPDATE_PERSONS_DATA, logger);
        else if (task.equals(AsyncTaskType.CHECK_DEC))
            return executeCheck(declarationDataId, SET_CHECK__ACCEPT__TOCREATE, logger);
        else if (task.equals(AsyncTaskType.ACCEPT_DEC))
            return executeCheck(declarationDataId, SET_CHECK__ACCEPT__TOCREATE, logger);
        else if (task.equals(AsyncTaskType.DELETE_DEC))
            return executeCheck(declarationDataId, SET_TODO, logger); //TODO
        else if (task.equals(AsyncTaskType.CONSOLIDATE))
            return executeCheck(declarationDataId, SET_CONSOLIDATE, logger);
        else if (task.equals(AsyncTaskType.EXCEL_DEC))
            return executeCheck(declarationDataId, SET_TODO, logger); //TODO
        else if (task.equals(AsyncTaskType.EXCEL_TEMPLATE_DEC))
            return executeCheck(declarationDataId, SET_TODO, logger); //TODO
        else if (task.equals(AsyncTaskType.PDF_DEC)) return executeCheck(declarationDataId, SET_TODO, logger); //TODO
        else if (task.equals(AsyncTaskType.DEPT_NOTICE_DEC))
            return executeCheck(declarationDataId, SET_TODO, logger); //TODO
        else if (task.equals(OperationType.RETURN_DECLARATION))
            return executeCheck(declarationDataId, SET_TODO, logger); //TODO
        else if (task.equals(OperationType.EDIT)) return executeCheck(declarationDataId, SET_EDIT, logger);
        else if (task.equals(OperationType.EDIT_FILE))
            return executeCheck(declarationDataId, SET_TODO, logger); //TODO
        else if (task.equals(OperationType.RNU_NDFL_PERSON_DB))
            return executeCheck(declarationDataId, SET_TODO, logger); //TODO
        else if (task.equals(OperationType.RNU_NDFL_PERSON_ALL_DB))
            return executeCheck(declarationDataId, SET_TODO, logger); //TODO
        else if (task.equals(OperationType.REPORT_KPP_OKTMO))
            return executeCheck(declarationDataId, SET_TODO, logger); //TODO
        else if (task.equals(OperationType.RNU_RATE_REPORT))
            return executeCheck(declarationDataId, SET_TODO, logger); //TODO
        else if (task.equals(OperationType.RNU_PAYMENT_REPORT))
            return executeCheck(declarationDataId, SET_TODO, logger); //TODO
        else if (task.equals(OperationType.RNU_NDFL_DETAIL_REPORT))
            return executeCheck(declarationDataId, SET_TODO, logger); //TODO
        else if (task.equals(OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT))
            return executeCheck(declarationDataId, SET_TODO, logger); //TODO
        else if (task.equals(OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT))
            return executeCheck(declarationDataId, SET_TODO, logger); //TODO
        else if (task.equals(OperationType.REPORT_2NDFL1))
            return executeCheck(declarationDataId, SET_TODO, logger); //TODO
        else if (task.equals(OperationType.REPORT_2NDFL2))
            return executeCheck(declarationDataId, SET_TODO, logger); //TODO
        else
            throw new IllegalArgumentException("Unknown task type!");
    }

    private boolean executeCheck(Long declarationDataId, Set<LockTaskType> lockingTasks, Logger logger) {
        for (LockTaskType lockingTask : lockingTasks ) {
            String lockKey = simpleDeclarationDataLockKeyGenerator.generateLockKey(declarationDataId, lockingTask);
            if (lockDataService.isLockExists(lockKey, false)) {
                return false;
            }
        }
        return true;
    }
}

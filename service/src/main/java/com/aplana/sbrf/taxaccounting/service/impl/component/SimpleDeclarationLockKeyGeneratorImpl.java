package com.aplana.sbrf.taxaccounting.service.impl.component;

import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.OperationType;
import com.aplana.sbrf.taxaccounting.model.LockTaskType;
import com.aplana.sbrf.taxaccounting.service.component.SimpleDeclarationDataLockKeyGenerator;
import org.springframework.stereotype.Component;

@Component
public class SimpleDeclarationLockKeyGeneratorImpl implements SimpleDeclarationDataLockKeyGenerator {

    @Override
    public String generateLockKey(Long declarationDataId, LockTaskType task) {
        if (task.equals(AsyncTaskType.LOAD_TRANSPORT_FILE))
            return String.format("DECLARATION_DATA_%s_IMPORT_TF_DECLARATION", declarationDataId);
        else if (task.equals(AsyncTaskType.IMPORT_DECLARATION_EXCEL))
            return String.format("IMPORT_DECLARATION_EXCEL_%s", declarationDataId);
        else if (task.equals(AsyncTaskType.IDENTIFY_PERSON))
            return String.format("DECLARATION_DATA_%s_IDENTIFY_PERSON", declarationDataId);
        else if (task.equals(AsyncTaskType.UPDATE_PERSONS_DATA))
            return String.format("DECLARATION_DATA_%s_UPDATE_PERSONS_DATA", declarationDataId);
        else if (task.equals(AsyncTaskType.CHECK_DEC))
            return String.format("DECLARATION_DATA_%s_CHECK_DECLARATION", declarationDataId);
        else if (task.equals(AsyncTaskType.ACCEPT_DEC))
            return String.format("DECLARATION_DATA_%s_ACCEPT_DECLARATION", declarationDataId);
        else if (task.equals(AsyncTaskType.DELETE_DEC))
            return String.format("DECLARATION_DATA_%s_DELETE_DECLARATION", declarationDataId);
        else if (task.equals(AsyncTaskType.CONSOLIDATE))
            return String.format("DECLARATION_DATA_%s_CONSOLIDATE", declarationDataId);
        else if (task.equals(AsyncTaskType.EXCEL_DEC))
            return String.format("DECLARATION_DATA_%s_XLSX", declarationDataId);
        else if (task.equals(AsyncTaskType.EXCEL_TEMPLATE_DEC))
            return String.format("EXCEL_TEMPLATE_DECLARATION_%s", declarationDataId);
        else if (task.equals(AsyncTaskType.PDF_DEC)) return String.format("DECLARATION_DATA_%s_PDF", declarationDataId);
        else if (task.equals(AsyncTaskType.DEPT_NOTICE_DEC))
            return String.format("DECLARATION_DATA_%s_DEPT_NOTICE", declarationDataId);
        else if (task.equals(OperationType.RETURN_DECLARATION))
            return String.format("DECLARATION_DATA_%s_RETURN_DECLARATION", declarationDataId);
        else if (task.equals(OperationType.EDIT)) return String.format("DECLARATION_DATA_%s_EDIT", declarationDataId);
        else if (task.equals(OperationType.EDIT_FILE))
            return String.format("DECLARATION_DATA_%s_EDIT_FILE", declarationDataId);
        else if (task.equals(OperationType.RNU_NDFL_PERSON_DB))
            return String.format("DECLARATION_DATA_%s_RNU_NDFL_PERSON_DB", declarationDataId);
        else if (task.equals(OperationType.RNU_NDFL_PERSON_ALL_DB))
            return String.format("DECLARATION_DATA_%s_RNU_NDFL_PERSON_ALL_DB", declarationDataId);
        else if (task.equals(OperationType.REPORT_KPP_OKTMO))
            return String.format("DECLARATION_DATA_%s_REPORT_KPP_OKTMO", declarationDataId);
        else if (task.equals(OperationType.RNU_RATE_REPORT))
            return String.format("DECLARATION_DATA_%s_RNU_RATE_REPORT", declarationDataId);
        else if (task.equals(OperationType.RNU_PAYMENT_REPORT))
            return String.format("DECLARATION_DATA_%s_RNU_PAYMENT_REPORT", declarationDataId);
        else if (task.equals(OperationType.RNU_NDFL_DETAIL_REPORT))
            return String.format("DECLARATION_DATA_%s_RNU_NDFL_DETAIL_REPORT", declarationDataId);
        else if (task.equals(OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT))
            return String.format("DECLARATION_DATA_%s_RNU_NDFL_2_6_DATA_XLSX_REPORT", declarationDataId);
        else if (task.equals(OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT))
            return String.format("DECLARATION_DATA_%s_RNU_NDFL_2_6_DATA_TXT_REPORT", declarationDataId);
        else if (task.equals(OperationType.REPORT_2NDFL1))
            return String.format("DECLARATION_DATA_%s_REPORT_2NDFL1", declarationDataId);
        else if (task.equals(OperationType.REPORT_2NDFL2))
            return String.format("DECLARATION_DATA_%s_REPORT_2NDFL2", declarationDataId);
        else
            throw new IllegalArgumentException("Unknown task type!");
    }
}

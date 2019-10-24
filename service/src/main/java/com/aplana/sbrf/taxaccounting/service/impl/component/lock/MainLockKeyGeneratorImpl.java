package com.aplana.sbrf.taxaccounting.service.impl.component.lock;

import com.aplana.sbrf.taxaccounting.model.OperationType;
import com.aplana.sbrf.taxaccounting.service.component.lock.LockKeyGenerator;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("mainLockKeyGeneratorImpl")
public class MainLockKeyGeneratorImpl implements LockKeyGenerator {

    @Override
    public String generateLockKey(Map<String, Long> idTokens, OperationType operationType) throws IllegalArgumentException {
        Long declarationDataId = idTokens.get("declarationDataId");
        Long sourceDeclarationId = idTokens.get("sourceDeclarationId");
        if (operationType.equals(OperationType.LOAD_TRANSPORT_FILE))
            return String.format("DECLARATION_DATA_%s_IMPORT_TF_DECLARATION", declarationDataId);
        else if (operationType.equals(OperationType.IMPORT_DECLARATION_EXCEL))
            return String.format("IMPORT_DECLARATION_EXCEL_%s", declarationDataId);
        else if (operationType.equals(OperationType.IDENTIFY_PERSON))
            return String.format("DECLARATION_DATA_%s_IDENTIFY_PERSON", declarationDataId);
        else if (operationType.equals(OperationType.UPDATE_PERSONS_DATA))
            return String.format("DECLARATION_DATA_%s_UPDATE_PERSONS_DATA", declarationDataId);
        else if (operationType.equals(OperationType.CHECK_DEC))
            return String.format("DECLARATION_DATA_%s_CHECK_DECLARATION", declarationDataId);
        else if (operationType.equals(OperationType.ACCEPT_DEC))
            return String.format("DECLARATION_DATA_%s_ACCEPT_DECLARATION", declarationDataId);
        else if (operationType.equals(OperationType.DELETE_DEC))
            return String.format("DECLARATION_DATA_%s_DELETE_DECLARATION", declarationDataId);
        else if (operationType.equals(OperationType.CONSOLIDATE))
            return String.format("DECLARATION_DATA_%s_CONSOLIDATE", declarationDataId);
        else if (operationType.equals(OperationType.EXCEL_DEC))
            return String.format("DECLARATION_DATA_%s_XLSX", declarationDataId);
        else if (operationType.equals(OperationType.EXCEL_TEMPLATE_DEC))
            return String.format("EXCEL_TEMPLATE_DECLARATION_%s", declarationDataId);
        else if (operationType.equals(OperationType.PDF_DEC))
            return String.format("DECLARATION_DATA_%s_PDF", declarationDataId);
        else if (operationType.equals(OperationType.RETURN_DECLARATION))
            return String.format("DECLARATION_DATA_%s_RETURN_DECLARATION", declarationDataId);
        else if (operationType.equals(OperationType.EDIT))
            return String.format("DECLARATION_DATA_%s_EDIT", declarationDataId);
        else if (operationType.equals(OperationType.EDIT_FILE))
            return String.format("DECLARATION_DATA_%s_EDIT_FILE", declarationDataId);
        else if (operationType.equals(OperationType.RNU_NDFL_PERSON_DB))
            return String.format("DECLARATION_DATA_%s_RNU_NDFL_PERSON_DB", declarationDataId);
        else if (operationType.equals(OperationType.RNU_NDFL_PERSON_ALL_DB))
            return String.format("DECLARATION_DATA_%s_RNU_NDFL_PERSON_ALL_DB", declarationDataId);
        else if (operationType.equals(OperationType.REPORT_KPP_OKTMO))
            return String.format("DECLARATION_DATA_%s_REPORT_KPP_OKTMO", declarationDataId);
        else if (operationType.equals(OperationType.RNU_RATE_REPORT))
            return String.format("DECLARATION_DATA_%s_RNU_RATE_REPORT", declarationDataId);
        else if (operationType.equals(OperationType.RNU_PAYMENT_REPORT))
            return String.format("DECLARATION_DATA_%s_RNU_PAYMENT_REPORT", declarationDataId);
        else if (operationType.equals(OperationType.RNU_NDFL_DETAIL_REPORT))
            return String.format("DECLARATION_DATA_%s_RNU_NDFL_DETAIL_REPORT", declarationDataId);
        else if (operationType.equals(OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT))
            return String.format("DECLARATION_DATA_%s_RNU_NDFL_2_6_DATA_XLSX_REPORT", declarationDataId);
        else if (operationType.equals(OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT))
            return String.format("DECLARATION_DATA_%s_RNU_NDFL_2_6_DATA_TXT_REPORT", declarationDataId);
        else if (operationType.equals(OperationType.REPORT_2NDFL1))
            return String.format("DECLARATION_DATA_%s_REPORT_2NDFL1", declarationDataId);
        else if (operationType.equals(OperationType.REPORT_2NDFL2))
            return String.format("DECLARATION_DATA_%s_REPORT_2NDFL2", declarationDataId);
        else if (operationType.equals(OperationType.DECLARATION_2NDFL1))
            return String.format("DECLARATION_TEMPLATE_%s_2NDFL1", declarationDataId);
        else if (operationType.equals(OperationType.DECLARATION_2NDFL2))
            return String.format("DECLARATION_TEMPLATE_%s_2NDFL2", declarationDataId);
        else if (operationType.equals(OperationType.DECLARATION_6NDFL))
            return String.format("DECLARATION_TEMPLATE_%s_6NDFL", declarationDataId);
        else if (operationType.equals(OperationType.DECLARATION_2NDFL_FL))
            return String.format("DECLARATION_TEMPLATE_%s_2NDFL_FL", declarationDataId);
        else if (operationType.equals(OperationType.EXPORT_REPORTS))
            return String.format("EXPORT_REPORTS_%s", declarationDataId);
        else if (operationType.equals(OperationType.UPDATE_DOC_STATE))
            return String.format("DECLARATION_DATA_%s_CHANGE_STATUS", declarationDataId);
        else if (operationType.equals(OperationType.SEND_EDO))
            return String.format("DECLARATION_DATA_%s_SEND_EDO", declarationDataId);
        else if (operationType.equals(OperationType.EXCEL_UNLOAD_LIST))
            return String.format("DECLARATION_DATA_%s_REPORT_LINK_DECLARATION", declarationDataId);
        else if (operationType.equals(OperationType.TRANSFER)) {
            return String.format("DECLARATION_DATA_%s_TRANSFER_%s", declarationDataId, sourceDeclarationId);
        } else if (operationType.equals(OperationType.DELETE_DEC_ROWS)) {
            return String.format("DECLARATION_DATA_%s_DELETE_DECLARATION_ROWS_%s", declarationDataId, sourceDeclarationId);
        }
            throw new IllegalArgumentException("Unknown operationType type!");
    }
}

package com.aplana.sbrf.taxaccounting.service.impl.component.lock.descript;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.OperationType;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.component.lock.descriptor.DeclarationDataKeyLockDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class DeclarationDataKeyLockDescriptorImpl implements DeclarationDataKeyLockDescriptor {

    @Autowired
    private DeclarationDataService declarationDataService;

    @Override
    public String createKeyLockDescription(Long declarationDataId, OperationType operationType) {
        switch (operationType) {
            case LOAD_TRANSPORT_FILE:
            case IMPORT_DECLARATION_EXCEL:
            case IDENTIFY_PERSON:
            case UPDATE_PERSONS_DATA:
            case CHECK_DEC:
            case ACCEPT_DEC:
            case DELETE_DEC:
            case CONSOLIDATE:
            case EXCEL_DEC:
            case EXCEL_TEMPLATE_DEC:
            case RETURN_DECLARATION:
            case EDIT:
            case EDIT_FILE:
            case RNU_NDFL_PERSON_DB:
            case RNU_NDFL_PERSON_ALL_DB:
            case REPORT_KPP_OKTMO:
            case RNU_RATE_REPORT:
            case RNU_PAYMENT_REPORT:
            case RNU_NDFL_DETAIL_REPORT:
            case RNU_NDFL_2_6_DATA_XLSX_REPORT:
            case RNU_NDFL_2_6_DATA_TXT_REPORT:
            case REPORT_2NDFL1:
            case REPORT_2NDFL2:
            case DECLARATION_2NDFL1:
            case DECLARATION_2NDFL2:
            case DECLARATION_6NDFL:
            case DECLARATION_2NDFL_FL:
            case DECLARATION_APP2:
            case EXPORT_REPORTS:
            case TRANSFER:
            case EXCEL_UNLOAD_LIST:
            case DELETE_DEC_ROWS:
                return createBaseDescription(declarationDataId);
            case PDF_DEC:
            case UPDATE_DOC_STATE:
            case SEND_EDO:
                return createExtendDescription(declarationDataId);
            default:
                throw new IllegalArgumentException("Unknown operationType type!");
        }
    }

    private String createBaseDescription(Long declarationDataId) {
        return "Налоговая форма: " + declarationDataService.getFullDeclarationDescription(declarationDataId);
    }

    private String createExtendDescription(Long declarationDataId) {
        DeclarationData declarationData = declarationDataService.get(Collections.singletonList(declarationDataId)).get(0);
        return String.format("Налоговая форма: %s, Налоговый орган: \"%s\", КПП: \"%s\", ОКТМО: \"%s\"",
                declarationDataService.getFullDeclarationDescription(declarationDataId),
                declarationData.getTaxOrganCode(),
                declarationData.getKpp(),
                declarationData.getOktmo());
    }

}

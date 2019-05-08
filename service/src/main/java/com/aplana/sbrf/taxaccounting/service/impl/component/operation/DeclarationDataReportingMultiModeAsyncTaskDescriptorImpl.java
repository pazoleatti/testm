package com.aplana.sbrf.taxaccounting.service.impl.component.operation;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.component.operation.DeclarationDataReportingMultiModeAsyncTaskDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DeclarationDataReportingMultiModeAsyncTaskDescriptorImpl implements DeclarationDataReportingMultiModeAsyncTaskDescriptor {

    private DeclarationDataService declarationDataService;
    private DeclarationTemplateService declarationTemplateService;

    public DeclarationDataReportingMultiModeAsyncTaskDescriptorImpl(DeclarationDataService declarationDataService, DeclarationTemplateService declarationTemplateService) {
        this.declarationDataService = declarationDataService;
        this.declarationTemplateService = declarationTemplateService;
    }

    @Override
    public String createDescription(List<Long> declarationDataIds, String name) {
        Set<String> declarationDataTypes = new HashSet<>();
        for (Long declarationDataId : declarationDataIds) {
            List<DeclarationData> declarationDataList = declarationDataService.get(Collections.singletonList(declarationDataId));
            if (!declarationDataList.isEmpty()) {
                DeclarationData declarationData = declarationDataList.get(0);
                if (declarationData != null) {
                    DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
                    if (declarationTemplate != null && declarationTemplate.getType() != null && declarationTemplate.getType().getName() != null) {
                        declarationDataTypes.add(declarationTemplate.getType().getName());
                    }
                }
            }
        }
        String description;
        if (declarationDataTypes.isEmpty()) {
            description = name;
        }
        else {
            description = String.format("%s Виды форм: %s",
                    name,
                    StringUtils.join(declarationDataTypes, ", "));
        }
        return description;
    }
}

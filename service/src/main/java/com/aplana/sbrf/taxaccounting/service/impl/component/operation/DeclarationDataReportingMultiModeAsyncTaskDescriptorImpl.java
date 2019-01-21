package com.aplana.sbrf.taxaccounting.service.impl.component.operation;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
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
    private TAUserService taUserService;

    public DeclarationDataReportingMultiModeAsyncTaskDescriptorImpl(DeclarationDataService declarationDataService, DeclarationTemplateService declarationTemplateService, TAUserService taUserService) {
        this.declarationDataService = declarationDataService;
        this.declarationTemplateService = declarationTemplateService;
        this.taUserService = taUserService;
    }

    @Override
    public String createDescription(List<Long> declarationDataIds, String name) {
        Set<String> declarationDataTypes = new HashSet<>();
        for (Long declarationDataId : declarationDataIds) {
            DeclarationData declarationData = declarationDataService.get(Collections.singletonList(declarationDataId)).get(0);
            DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
            declarationDataTypes.add(declarationTemplate.getType().getName());
        }
        return String.format("%s Виды форм: %s  запущена пользователем %s",
                name,
                StringUtils.join(declarationDataTypes, ", "),
                taUserService.getCurrentUser().getName());
    }
}

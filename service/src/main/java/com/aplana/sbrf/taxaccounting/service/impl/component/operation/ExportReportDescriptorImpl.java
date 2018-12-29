package com.aplana.sbrf.taxaccounting.service.impl.component.operation;

import com.aplana.sbrf.taxaccounting.service.component.operation.ExportReportDescriptor;
import org.springframework.stereotype.Component;

@Component
public class ExportReportDescriptorImpl implements ExportReportDescriptor{
    @Override
    public String createDescription() {
        return "Выгрузка отчетности";
    }
}

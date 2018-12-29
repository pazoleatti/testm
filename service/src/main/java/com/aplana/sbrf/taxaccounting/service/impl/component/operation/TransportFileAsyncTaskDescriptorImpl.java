package com.aplana.sbrf.taxaccounting.service.impl.component.operation;

import com.aplana.sbrf.taxaccounting.service.component.operation.TransportFileAsyncTaskDescriptor;
import org.springframework.stereotype.Component;

@Component
public class TransportFileAsyncTaskDescriptorImpl implements TransportFileAsyncTaskDescriptor {

    @Override
    public String createDescription(String fileName) {
        return String.format("Загрузка файла %s", fileName);
    }
}

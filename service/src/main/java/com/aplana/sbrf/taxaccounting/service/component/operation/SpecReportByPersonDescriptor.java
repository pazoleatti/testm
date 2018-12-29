package com.aplana.sbrf.taxaccounting.service.component.operation;

import java.util.Map;

public interface SpecReportByPersonDescriptor {
    /**
     * Создать описание асинхронной задачи в зависимости от типа задачи
     * @param declarationDataId     идентификатор НФ
     * @param subreportParamValues  параметры для формирования спецотчета
     * @return  строку описания
     */
    String createDescription(Long declarationDataId, Map<String, Object> subreportParamValues, String name);
}

package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.service.script.FormTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис-обертка для работы из скриптов
 *
 * @author @author Dmitriy Levykin
 */
@Transactional(readOnly = true)
@Component("formTemplateService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FormTemplateServiceImpl implements FormTemplateService {

    @Autowired
    private com.aplana.sbrf.taxaccounting.service.FormTemplateService formTemplateService;

    @Override
    public FormTemplate get(int formTemplateId) {
        return formTemplateService.getFullFormTemplate(formTemplateId);
    }
}

package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.service.script.FormTypeService;
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
@Component("formTypeService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FormTypeServiceImpl implements FormTypeService {

    @Autowired
    private com.aplana.sbrf.taxaccounting.service.FormTypeService formTypeService;

    @Override
    public FormType get(int formTypeId) {
        return formTypeService.get(formTypeId);
    }
}

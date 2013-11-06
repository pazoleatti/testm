package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Сервис-обертка для работы из скриптов
 *
 * @author @author Dmitriy Levykin
 */
@ScriptExposed
public interface FormTemplateService {
    /**
     * Шаблон налоговой формы
     * @param formTemplateId идентификатор макета
     * @return объект, представляющий описание налоговой формы
     */
    FormTemplate get(int formTemplateId);
}

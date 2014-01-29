package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Сервис-обертка для работы из скриптов
 *
 * @author @author Dmitriy Levykin
 */
@ScriptExposed
public interface FormTypeService {
    /**
     * Вид налоговой формы
     *
     * @param formTypeId идентификатор формы
     * @return объект, представляющий описание вида налоговой формы
     */
    FormType get(int formTypeId);
}

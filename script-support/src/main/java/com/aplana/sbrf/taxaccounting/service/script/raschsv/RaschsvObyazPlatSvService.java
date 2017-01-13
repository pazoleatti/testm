package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvObyazPlatSv;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Сервис для работы с "Сводные данные об обязательствах плательщика страховых взносов"
 */
@ScriptExposed
public interface RaschsvObyazPlatSvService {

    /**
     * Сохранение "Сводные данные об обязательствах плательщика страховых взносов"
     * @param raschsvObyazPlatSv - Сводные данные об обязательствах плательщика страховых взносов
     * @return
     */
    Long insertObyazPlatSv(RaschsvObyazPlatSv raschsvObyazPlatSv);
}

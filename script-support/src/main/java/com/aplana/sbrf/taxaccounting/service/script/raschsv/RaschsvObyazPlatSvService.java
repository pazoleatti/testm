package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvObyazPlatSv;

/**
 * Сервис для работы с "Сводные данные об обязательствах плательщика страховых взносов"
 */
public interface RaschsvObyazPlatSvService {

    /**
     * Сохранение "Сводные данные об обязательствах плательщика страховых взносов"
     * @param raschsvObyazPlatSv - Сводные данные об обязательствах плательщика страховых взносов
     * @return
     */
    Long insertObyazPlatSv(RaschsvObyazPlatSv raschsvObyazPlatSv);
}

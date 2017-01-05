package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPravTarif71427;

/**
 * DAO-интерфейс для работы с таблицей "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 7 пункта 1 статьи 427"
 */
public interface RaschsvPravTarif71427Dao {

    /**
     * Сохранение "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 7 пункта 1 статьи 427"
     * @param raschsvPravTarif71427
     * @return
     */
    Long insertRaschsvPravTarif71427(RaschsvPravTarif71427 raschsvPravTarif71427);
}

package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPravTarif31427;

/**
 * DAO-интерфейс для работы с таблицей "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 3 пункта 1 статьи 427"
 */
public interface RaschsvPravTarif31427Dao {

    /**
     * Сохранение "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 3 пункта 1 статьи 427"
     * @param raschsvPravTarif31427
     * @return
     */
    Long insertRaschsvPravTarif31427(RaschsvPravTarif31427 raschsvPravTarif31427);
}

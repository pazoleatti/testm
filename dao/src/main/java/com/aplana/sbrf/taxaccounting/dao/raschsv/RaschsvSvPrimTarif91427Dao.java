package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvPrimTarif91427;

/**
 * DAO-интерфейс для работы с таблицей "Сведения, необходимые для применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 9 пункта 1 статьи 427"
 */
public interface RaschsvSvPrimTarif91427Dao {

    /**
     * Сохранение "Сведения, необходимые для применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 9 пункта 1 статьи 427"
     * @param raschsvSvPrimTarif91427
     * @return
     */
    Long insertRaschsvSvPrimTarif91427(RaschsvSvPrimTarif91427 raschsvSvPrimTarif91427);
}

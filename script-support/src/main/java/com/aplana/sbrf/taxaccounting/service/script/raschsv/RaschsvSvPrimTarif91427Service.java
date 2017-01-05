package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvPrimTarif91427;

/**
 * Сервис для работы с "Сведения, необходимые для применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 9 пункта 1 статьи 427"
 */
public interface RaschsvSvPrimTarif91427Service {

    /**
     * Сохранение "Сведения, необходимые для применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 9 пункта 1 статьи 427"
     * @param raschsvSvPrimTarif91427
     * @return
     */
    Long insertRaschsvSvPrimTarif91427(RaschsvSvPrimTarif91427 raschsvSvPrimTarif91427);
}

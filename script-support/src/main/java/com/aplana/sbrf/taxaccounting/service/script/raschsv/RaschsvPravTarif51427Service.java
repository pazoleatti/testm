package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPravTarif51427;

/**
 * Сервис для работы с "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 5 пункта 1 статьи 427"
 */
public interface RaschsvPravTarif51427Service {

    /**
     * Сохранение "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 5 пункта 1 статьи 427"
     * @param raschsvPravTarif51427
     * @return
     */
    Long insertRaschsvPravTarif51427(RaschsvPravTarif51427 raschsvPravTarif51427);
}

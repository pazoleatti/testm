package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPravTarif51427;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Сервис для работы с ПравТариф5.1.427
 */
@ScriptExposed
public interface RaschsvPravTarif51427Service {

    /**
     * Сохранение ПравТариф5.1.427
     * @param raschsvPravTarif51427
     * @return
     */
    Long insertRaschsvPravTarif51427(RaschsvPravTarif51427 raschsvPravTarif51427);
}

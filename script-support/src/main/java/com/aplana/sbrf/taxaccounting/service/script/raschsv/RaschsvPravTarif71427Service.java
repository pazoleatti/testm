package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPravTarif71427;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Сервис для работы с ПравТариф7.1.427
 */
@ScriptExposed
public interface RaschsvPravTarif71427Service {

    /**
     * Сохранение ПравТариф7.1.427
     * @param raschsvPravTarif71427
     * @return
     */
    Long insertRaschsvPravTarif71427(RaschsvPravTarif71427 raschsvPravTarif71427);
}

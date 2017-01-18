package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPravTarif31427;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Сервис для работы с ПравТариф3.1.427
 */
@ScriptExposed
public interface RaschsvPravTarif31427Service {

    /**
     * Сохранение ПравТариф3.1.427
     * @param raschsvPravTarif31427
     * @return
     */
    Long insertRaschsvPravTarif31427(RaschsvPravTarif31427 raschsvPravTarif31427);
}
